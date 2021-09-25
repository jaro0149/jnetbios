package sk.jt.jnetbios.odl.netconf.server

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import sk.jt.jnetbios.odl.core.OdlCoreProcessor
import sk.jt.jnetbios.odl.netconf.server.config.NetconfMonitoringConfig
import sk.jt.jnetbios.odl.netconf.server.config.NetconfServerConfig
import sk.jt.jnetbios.odl.netconf.server.impl.NetconfServer

/**
 * Spring configuration of NETCONF server. It is responsible for starting [NetconfServer] component and loading
 * configuration mapped to [NetconfServerConfig] and [NetconfMonitoringConfig].
 */
@Configuration
@ComponentScan
@ConfigurationPropertiesScan("sk.jt.jnetbios.odl.netconf.server.config")
@Import(OdlCoreProcessor::class)
open class NetconfServerProcessor