package sk.jt.jnetbios.odl.core.config

import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Settings related to the data-store executor.
 *
 * @property maxDataChangeExecutorPoolSize the maximum thread pool size for the data change notification executor
 * @property maxDataChangeExecutorQueueSize the maximum queue size for the data change notification executor
 * @property maxDataChangeListenerQueueSize the maximum queue size for the data change listeners
 * @property maxDataStoreExecutorQueueSize the maximum queue size for the data store executor
 */
@Validated
@ConfigurationProperties(prefix = "data-store")
internal data class DataStoreConfig(
    @field:Positive var maxDataChangeExecutorPoolSize: Int = 4,
    @field:PositiveOrZero var maxDataChangeExecutorQueueSize: Int = 2,
    @field:PositiveOrZero var maxDataChangeListenerQueueSize: Int = 8,
    @field:PositiveOrZero var maxDataStoreExecutorQueueSize: Int = 10
)