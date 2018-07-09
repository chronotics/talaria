package org.chronotics.talaria.thrift;

import org.chronotics.talaria.thrift.gen.TransferService;

//public interface ThriftService extends TransferService.Iface {
public abstract class ThriftService implements TransferService.Iface {
    private ThriftServiceExecutor executor = null;

    public void setExecutor(ThriftServiceExecutor _executor) {
        executor = _executor;
    }

    public ThriftServiceExecutor getExecutor() {
        return executor;
    }
}
