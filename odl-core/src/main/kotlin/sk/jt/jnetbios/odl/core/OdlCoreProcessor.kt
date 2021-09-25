package sk.jt.jnetbios.odl.core

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import sk.jt.jnetbios.odl.core.api.OdlCoreServices
import sk.jt.jnetbios.odl.core.config.DataBrokerConfig
import sk.jt.jnetbios.odl.core.config.DataStoreConfig

/**
 * Spring configuration of [OdlCoreServices] component - it starts core ODL services and exposes services as beans.
 * Configuration of these services is modelled by properties: [DataStoreConfig] and [DataBrokerConfig].
 */
@Configuration
@ComponentScan
@ConfigurationPropertiesScan("sk.jt.jnetbios.odl.core.config")
open class OdlCoreProcessor