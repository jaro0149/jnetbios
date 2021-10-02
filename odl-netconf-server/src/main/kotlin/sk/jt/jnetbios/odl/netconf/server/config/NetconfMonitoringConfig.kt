package sk.jt.jnetbios.odl.netconf.server.config

import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Configuration related to NETCONF monitoring feature.
 *
 * @property updateInterval interval between updating of NETCONF state, 0 can be used for turning off monitoring (sec)
 * @property corePoolSize the number of threads to keep in the pool, even if they are idle
 * @property maxThreadCount the maximum number of threads to allow in the pool
 */
@Validated
@ConfigurationProperties(prefix = "netconf-monitoring")
internal data class NetconfMonitoringConfig(
    @field:PositiveOrZero var updateInterval: Long = 10,
    @field:PositiveOrZero var corePoolSize: Int = 10,
    @field:Positive var maxThreadCount: Int = 20
)