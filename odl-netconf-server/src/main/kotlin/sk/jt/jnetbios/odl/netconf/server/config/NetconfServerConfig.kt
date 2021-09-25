package sk.jt.jnetbios.odl.netconf.server.config

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration related to NETCONF server.
 *
 * @property host hostname/IP address on which NETCONF server is listening to incoming SSH connections
 *     (0.0.0.0 can be used for 'any' address)
 * @property port listening SSH port, 0 can be used for auto-allocation of ephemeral port
 * @property username username used for authentication into backing SSH server
 * @property password password used for authentication into backing SSH server
 * @property connectionTimeout the maximum time before NETCONF session must be established (milliseconds)
 * @property maxNioThreads the maximum number of NIO threads used for processing of incoming and outgoing requests
 */
// todo: @ConstructorBinding doesn't work and also validation doesn't work
@ConfigurationProperties(prefix = "netconf-server")
internal data class NetconfServerConfig(
    @NotBlank var host: String = "127.0.0.1",
    @Min(0) @Max(65535) var port: Int = 2022,
    @NotBlank var username: String = "netbios",
    @NotBlank var password: String = "netbios",
    @Positive var connectionTimeout: Long = 5000,
    @Positive var maxNioThreads: Int = 10
)