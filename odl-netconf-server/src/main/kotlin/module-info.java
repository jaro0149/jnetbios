/**
 * Module used for configuration and starting of ODL NETCONF server.
 */
module sk.jt.jnetbios.odl.netconf.server {
    requires sk.jt.jnetbios.odl.core;

    requires mdsal.netconf.connector;
    requires mdsal.netconf.ssh;
    requires netconf.mapping.api;
    requires netconf.api;
    requires netconf.impl;
    requires netconf.auth;
    requires threadpool.config.api;
    requires rfc6991.ietf.inet.types;

    requires spring.boot;
    requires spring.context;
    requires org.slf4j;
    requires kotlin.stdlib;
    requires java.annotation;
    requires java.validation;
    requires io.netty.common;
    requires io.netty.transport;

    opens sk.jt.jnetbios.odl.netconf.server;
    opens sk.jt.jnetbios.odl.netconf.server.config;
    opens sk.jt.jnetbios.odl.netconf.server.impl;

    exports sk.jt.jnetbios.odl.netconf.server;
}