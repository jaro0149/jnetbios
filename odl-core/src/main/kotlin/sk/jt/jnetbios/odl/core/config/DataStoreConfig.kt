package sk.jt.jnetbios.odl.core.config

import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Settings related to the data-store executor.
 *
 * @property maxDataChangeExecutorPoolSize the maximum thread pool size for the data change notification executor
 * @property maxDataChangeExecutorQueueSize the maximum queue size for the data change notification executor
 * @property maxDataChangeListenerQueueSize the maximum queue size for the data change listeners
 * @property maxDataStoreExecutorQueueSize the maximum queue size for the data store executor
 */
@ConfigurationProperties(prefix = "data-store")
internal data class DataStoreConfig(
    @Positive var maxDataChangeExecutorPoolSize: Int = 4,
    @PositiveOrZero var maxDataChangeExecutorQueueSize: Int = 2,
    @PositiveOrZero var maxDataChangeListenerQueueSize: Int = 8,
    @PositiveOrZero var maxDataStoreExecutorQueueSize: Int = 10
)