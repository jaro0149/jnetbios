package sk.jt.jnetbios.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import sk.jt.jnetbios.odl.netconf.server.NetconfServerProcessor

/**
 * Spring configuration of the main application. It starts netconf-server that is handled by [NetconfServerProcessor].
 */
@SpringBootApplication
@Import(NetconfServerProcessor::class)
open class Application

/**
 * Starting Spring Boot application.
 *
 * @param args program arguments
 */
fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}