/**
 * Core ODL services - schema service, data-stores, data broker, RPC router, and MD-SAL codecs.
 */
module sk.jt.jnetbios.odl.core {
    requires transitive org.opendaylight.mdsal.dom.api;
    requires transitive org.opendaylight.mdsal.binding.dom.codec.spi;
    requires transitive org.opendaylight.mdsal.binding.api;

    requires org.opendaylight.mdsal.binding.runtime.spi;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.mdsal.binding.generator.impl;
    requires org.opendaylight.mdsal.dom.spi;
    requires mdsal.binding.dom.adapter;
    requires mdsal.dom.inmemory.datastore;
    requires mdsal.binding.dom.codec;
    requires mdsal.dom.broker;
    requires org.opendaylight.yangtools.yang.parser.impl;

    requires spring.context;
    requires spring.boot;
    requires org.slf4j;
    requires kotlin.stdlib;
    requires java.annotation;
    requires java.validation;
    requires spring.boot.autoconfigure;

    opens sk.jt.jnetbios.odl.core;
    opens sk.jt.jnetbios.odl.core.config;
    opens sk.jt.jnetbios.odl.core.impl;

    exports sk.jt.jnetbios.odl.core;
    exports sk.jt.jnetbios.odl.core.api;
}