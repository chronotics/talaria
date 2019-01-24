package org.chronotics.talaria.thrift;

import org.chronotics.talaria.thrift.gen.ThriftRWService;

public abstract class ThriftServiceHandler implements ThriftRWService.Iface {

    private ThriftServiceExecutor executor = null;

    public void setExecutor(ThriftServiceExecutor _executor) {
        executor = _executor;
    }

    public ThriftServiceExecutor getExecutor() {
        return executor;
    }
}
