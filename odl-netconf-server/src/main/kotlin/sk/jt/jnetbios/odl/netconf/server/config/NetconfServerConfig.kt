package sk.jt.jnetbios.odl.netconf.server.config

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

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
@Validated
@ConfigurationProperties(prefix = "netconf-server")
internal data class NetconfServerConfig constructor(
    @field:NotBlank var host: String = "127.0.0.1",
    @field:Min(0) @field:Max(65535) var port: Int = 2022,
    @field:NotBlank var username: String = "netbios",
    @field:NotBlank var password: String = "netbios",
    @field:Positive var connectionTimeout: Long = 5000,
    @field:Positive var maxNioThreads: Int = 10
)