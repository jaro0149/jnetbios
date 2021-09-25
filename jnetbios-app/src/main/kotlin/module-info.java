/**
 * Module which contains logic for spinning the whole application using Spring Boot.
 */
module jnetbios.app {
    requires sk.jt.jnetbios.odl.netconf.server;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.core;
    requires spring.context;
    requires spring.beans;
    requires kotlin.stdlib;

    opens sk.jt.jnetbios.app;
}