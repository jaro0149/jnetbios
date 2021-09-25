package sk.jt.jnetbios.odl.core.config

import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Settings related to the data broker executor.
 *
 * @property maxQueueSize the maximum number of queued tasks
 * @property corePoolSize the number of threads to keep in the pool, even if they are idle
 * @property maxPoolSize the maximum number of threads to allow in the pool
 * @property keepaliveTime when the number of threads is greater than the core, this is the maximum time that excess
 *     idle threads will wait for new tasks before terminating
 */
@ConfigurationProperties(prefix = "data-broker")
internal data class DataBrokerConfig(
    @Positive var maxQueueSize: Int = 1000,
    @PositiveOrZero var corePoolSize: Int = 10,
    @Positive var maxPoolSize: Int = 20,
    @PositiveOrZero var keepaliveTime: Long = 60
)