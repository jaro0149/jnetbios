package sk.jt.jnetbios.odl.core.impl

import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy
import java.util.concurrent.TimeUnit.SECONDS
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import org.opendaylight.mdsal.binding.api.DataBroker
import org.opendaylight.mdsal.binding.api.RpcProviderService
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMDataBrokerAdapter
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMRpcProviderServiceAdapter
import org.opendaylight.mdsal.binding.dom.adapter.ConstantAdapterContext
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext
import org.opendaylight.mdsal.binding.runtime.api.DefaultBindingRuntimeContext
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot
import org.opendaylight.mdsal.binding.runtime.spi.ModuleInfoSnapshotBuilder
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections
import org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION
import org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL
import org.opendaylight.mdsal.dom.api.DOMDataBroker
import org.opendaylight.mdsal.dom.api.DOMSchemaService
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker
import org.opendaylight.mdsal.dom.spi.FixedDOMSchemaService
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStoreConfigPropertiesBuilder
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStoreFactory
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import sk.jt.jnetbios.odl.core.api.OdlCoreServices
import sk.jt.jnetbios.odl.core.config.DataBrokerConfig
import sk.jt.jnetbios.odl.core.config.DataStoreConfig

/**
 * Facade that creates required core ODL services and initializes them.
 *
 * @property dataBrokerConfig data broker settings
 * @property dataStoreConfig data-store settings (applied to both [CONFIGURATION] and [OPERATIONAL] data-storees)
 */
@Component
internal class OdlCoreServicesImpl(
    private val dataBrokerConfig: DataBrokerConfig,
    private val dataStoreConfig: DataStoreConfig
) : OdlCoreServices {

    companion object {
        private val LOG = LoggerFactory.getLogger(OdlCoreServicesImpl::class.java)
    }

    private val closeableServices: ArrayDeque<AutoCloseable> = ArrayDeque()

    private lateinit var schemaService: DOMSchemaService
    private lateinit var bindingDomCodec: BindingDOMCodecServices
    private lateinit var dataBroker: DOMDataBroker
    private lateinit var bindingDataBroker: DataBroker
    private lateinit var rpcRouter: DOMRpcRouter
    private lateinit var bindingRpcProvider: RpcProviderService

    /**
     * Starting schema service, md-sal codecs, data broker, and RPC router services.
     */
    @PostConstruct
    internal fun init() {
        val modulesInfoSnapshot = createModulesInfoSnapshot()
        val bindingRuntimeContext = createBindingRuntimeContext(modulesInfoSnapshot)
        schemaService = createSchemaService(bindingRuntimeContext, modulesInfoSnapshot)
        bindingDomCodec = createBindingDomCodec(bindingRuntimeContext)
        val bindingAdapterContext = createBindingAdapterContext(bindingDomCodec)
        dataBroker = createDataBroker(schemaService)
        bindingDataBroker = createBindingDataBroker(bindingAdapterContext, dataBroker)
        rpcRouter = createRpcRouter(schemaService)
        bindingRpcProvider = createBindingRpcProvider(rpcRouter, bindingAdapterContext)
    }

    /**
     * Closing of all started ODL services.
     */
    @PreDestroy
    internal fun close() {
        closeableServices.forEach {
            try {
                it.close()
            } catch (e: Exception) {
                LOG.debug("Failed to close {}", it, e)
            }
        }
        closeableServices.clear()
    }

    private fun createModulesInfoSnapshot() = ModuleInfoSnapshotBuilder(YangParserFactoryImpl())
        .add(BindingReflections.loadModuleInfos())
        .build()

    private fun createBindingRuntimeContext(modulesInfo: ModuleInfoSnapshot) = DefaultBindingRuntimeContext(
        DefaultBindingRuntimeGenerator().generateTypeMapping(modulesInfo.effectiveModelContext),
        modulesInfo
    )

    private fun createSchemaService(runtimeContext: BindingRuntimeContext, modulesInfo: ModuleInfoSnapshot) =
        FixedDOMSchemaService.of(
            runtimeContext,
            modulesInfo
        )

    private fun createBindingDomCodec(runtimeContext: BindingRuntimeContext) = BindingCodecContext(runtimeContext)

    private fun createBindingAdapterContext(bindingDomCodec: BindingDOMCodecServices) =
        ConstantAdapterContext(bindingDomCodec)

    private fun createDataBroker(schemaService: DOMSchemaService): DOMDataBroker {
        val datastoreSettings = InMemoryDOMDataStoreConfigPropertiesBuilder()
            .maxDataChangeExecutorPoolSize(dataStoreConfig.maxDataChangeExecutorPoolSize)
            .maxDataChangeExecutorQueueSize(dataStoreConfig.maxDataChangeExecutorQueueSize)
            .maxDataChangeListenerQueueSize(dataStoreConfig.maxDataChangeListenerQueueSize)
            .maxDataStoreExecutorQueueSize(dataStoreConfig.maxDataStoreExecutorQueueSize)
            .build()
        val configDatastore = InMemoryDOMDataStoreFactory.create(
            "CONFIG-DS",
            datastoreSettings,
            schemaService
        ).also(closeableServices::addFirst)
        val operDatastore = InMemoryDOMDataStoreFactory.create(
            "OPER-DS",
            datastoreSettings,
            schemaService
        ).also(closeableServices::addFirst)

        val dataBrokerExecutor = ThreadPoolExecutor(
            dataBrokerConfig.corePoolSize,
            dataBrokerConfig.maxPoolSize,
            dataBrokerConfig.keepaliveTime,
            SECONDS,
            ArrayBlockingQueue(dataBrokerConfig.maxQueueSize),
            ThreadFactoryBuilder()
                .setNameFormat("data-broker-%d")
                .build(),
            AbortPolicy()
        ).also { closeableServices.addFirst(AutoCloseable(it::shutdown)) }
        return SerializedDOMDataBroker(
            mapOf(
                CONFIGURATION to configDatastore,
                OPERATIONAL to operDatastore
            ),
            MoreExecutors.listeningDecorator(dataBrokerExecutor)
        ).also(closeableServices::addFirst)
    }

    private fun createBindingDataBroker(adapterContext: AdapterContext, domDataBroker: DOMDataBroker) =
        BindingDOMDataBrokerAdapter(
            adapterContext,
            domDataBroker
        )

    private fun createRpcRouter(schemaService: DOMSchemaService) = DOMRpcRouter.newInstance(schemaService)
        .also(closeableServices::addFirst)

    private fun createBindingRpcProvider(rpcRouter: DOMRpcRouter, adapterContext: AdapterContext) =
        BindingDOMRpcProviderServiceAdapter(
            adapterContext,
            rpcRouter.rpcProviderService
        )

    @Bean
    override fun schemaService() = schemaService

    @Bean
    override fun bindingDomCodec() = bindingDomCodec

    @Bean
    override fun dataBroker() = dataBroker

    @Bean
    override fun bindingDataBroker() = bindingDataBroker

    @Bean
    override fun rpcService() = rpcRouter.rpcService

    @Bean
    override fun rpcProvider() = rpcRouter.rpcProviderService

    @Bean
    override fun bindingRpcProvider() = bindingRpcProvider
}