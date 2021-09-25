package sk.jt.jnetbios.odl.core.api

import org.opendaylight.mdsal.binding.api.DataBroker
import org.opendaylight.mdsal.binding.api.RpcProviderService
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices
import org.opendaylight.mdsal.dom.api.DOMDataBroker
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService
import org.opendaylight.mdsal.dom.api.DOMRpcService
import org.opendaylight.mdsal.dom.api.DOMSchemaService

/**
 * Grouped ODL core services.
 */
interface OdlCoreServices {

    /**
     * Schema service that is holding all compiled system YANG files.
     *
     * @return [DOMSchemaService]
     */
    fun schemaService(): DOMSchemaService

    /**
     * Codec used for conversion of paths and data between binding-aware (YANG elements are modelled
     * by generated classes) and binding-independent representations (YANG elements are represented by generic classes).
     *
     * @return [BindingDOMCodecServices]
     */
    fun bindingDomCodec(): BindingDOMCodecServices

    /**
     * Data broker for accessing of **CONFIGURATION** and **OPERATIONAL** data-stores using transactions.
     *
     * @return [DOMDataBroker]
     */
    fun dataBroker(): DOMDataBroker

    /**
     * Binding-aware representation of [dataBroker].
     *
     * @return [DataBroker]
     */
    fun bindingDataBroker(): DataBroker

    /**
     * Service used for invocation of RPC implementations.
     *
     * @return [DOMRpcService]
     */
    fun rpcService(): DOMRpcService

    /**
     * RPC register.
     *
     * @return [DOMRpcProviderService]
     */
    fun rpcProvider(): DOMRpcProviderService

    /**
     * Binding-aware representation of [rpcProvider].
     *
     * @return [RpcProviderService]
     */
    fun bindingRpcProvider(): RpcProviderService
}