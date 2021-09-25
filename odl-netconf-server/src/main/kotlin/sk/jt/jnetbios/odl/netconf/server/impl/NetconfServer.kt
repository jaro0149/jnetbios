package sk.jt.jnetbios.odl.netconf.server.impl

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.DefaultEventLoop
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.HashedWheelTimer
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import org.opendaylight.controller.config.threadpool.ScheduledThreadPool
import org.opendaylight.mdsal.dom.api.DOMDataBroker
import org.opendaylight.mdsal.dom.api.DOMRpcService
import org.opendaylight.mdsal.dom.api.DOMSchemaService
import org.opendaylight.netconf.api.monitoring.NetconfMonitoringService
import org.opendaylight.netconf.impl.NetconfServerDispatcherImpl
import org.opendaylight.netconf.impl.NetconfServerSessionNegotiatorFactory
import org.opendaylight.netconf.impl.NetconfServerSessionNegotiatorFactoryBuilder
import org.opendaylight.netconf.impl.ServerChannelInitializer
import org.opendaylight.netconf.impl.SessionIdProvider
import org.opendaylight.netconf.impl.osgi.AggregatedNetconfOperationServiceFactory
import org.opendaylight.netconf.impl.osgi.NetconfMonitoringServiceImpl
import org.opendaylight.netconf.mapping.api.NetconfOperationServiceFactory
import org.opendaylight.netconf.mdsal.connector.MdsalNetconfOperationServiceFactory
import org.opendaylight.netconf.ssh.NetconfNorthboundSshServer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import sk.jt.jnetbios.odl.netconf.server.config.NetconfMonitoringConfig
import sk.jt.jnetbios.odl.netconf.server.config.NetconfServerConfig

/**
 * Facade for the ODL NETCONF server implementation. It is used for starting of NETCONF server with provided
 * configuration.
 *
 * @property netconfMonitoringConfig settings of NETCONF monitoring feature
 * @property netconfServerConfig settings of NETCONF server
 * @property schemaService schema-context provider
 * @property dataBroker binding-independent data-stores broker
 * @property rpcService RPC service
 */
@Component
internal class NetconfServer(
    private val netconfMonitoringConfig: NetconfMonitoringConfig,
    private val netconfServerConfig: NetconfServerConfig,
    private val schemaService: DOMSchemaService,
    private val dataBroker: DOMDataBroker,
    private val rpcService: DOMRpcService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(NetconfServer::class.java)
    }

    private val closeableServices: ArrayDeque<AutoCloseable> = ArrayDeque()

    private lateinit var netconfOperationServiceFactory: NetconfOperationServiceFactory
    private lateinit var monitoringService: NetconfMonitoringService

    /**
     * Starting NETCONF operations service-factory, monitoring service, and server.
     */
    @PostConstruct
    internal fun init() {
        netconfOperationServiceFactory = createNetconfOperationServiceFactory()
        monitoringService = createMonitoringService()
        createNetconfServer()
    }

    /**
     * Closing NETCONF server and associated services.
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

    private fun createNetconfOperationServiceFactory() = MdsalNetconfOperationServiceFactory(
        schemaService,
        AggregatedNetconfOperationServiceFactory()
            .also(closeableServices::addFirst),
        dataBroker,
        rpcService
    ).also(closeableServices::addFirst)

    private fun createMonitoringService(): NetconfMonitoringService {
        val netconfMonitoringExecutor = ScheduledThreadPoolExecutor(
            netconfMonitoringConfig.corePoolSize,
            ThreadFactoryBuilder()
                .setNameFormat("netconf-monitoring-%d")
                .build(),
            AbortPolicy()
        ).also { closeableServices.addFirst(AutoCloseable(it::shutdown)) }
        return NetconfMonitoringServiceImpl(
            netconfOperationServiceFactory,
            object : ScheduledThreadPool {
                override fun getExecutor() = netconfMonitoringExecutor
                override fun getMaxThreadCount() = netconfMonitoringConfig.maxThreadCount
            },
            netconfMonitoringConfig.updateInterval
        ).also(closeableServices::addFirst)
    }

    private fun createNetconfServer() {
        val negotiatorFactory = NetconfServerSessionNegotiatorFactoryBuilder()
            .setAggregatedOpService(netconfOperationServiceFactory)
            .setIdProvider(SessionIdProvider())
            .setMonitoringService(monitoringService)
            .setBaseCapabilities(NetconfServerSessionNegotiatorFactory.DEFAULT_BASE_CAPABILITIES)
            .setConnectionTimeoutMillis(netconfServerConfig.connectionTimeout)
            .setTimer(HashedWheelTimer())
            .build()
        val serverChannelInitializer = ServerChannelInitializer(negotiatorFactory)
        val netconfNioEventLoopGroup = NioEventLoopGroup(
            netconfServerConfig.maxNioThreads,
            ThreadFactoryBuilder()
                .setNameFormat("netconf-nio-%s")
                .build()
        ).also { closeableServices.addFirst(AutoCloseable(it::shutdownGracefully)) }
        val netconfServerDispatcher = NetconfServerDispatcherImpl(
            serverChannelInitializer,
            netconfNioEventLoopGroup,
            netconfNioEventLoopGroup
        )
        NetconfNorthboundSshServer(
            netconfServerDispatcher,
            netconfNioEventLoopGroup,
            DefaultEventLoop(netconfNioEventLoopGroup)
                .also { closeableServices.addFirst(AutoCloseable(it::shutdownGracefully)) },
            netconfServerConfig.host,
            netconfServerConfig.port.toString()
        ) { username, password -> netconfServerConfig.username == username && netconfServerConfig.password == password }
            .also { closeableServices.addFirst(AutoCloseable(it::close)) }
    }
}