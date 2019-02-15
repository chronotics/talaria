package org.chronotics.talaria.common.callableexecutor;

import org.chronotics.talaria.common.CallableExecutor;

public class BypassExecutor <T> extends CallableExecutor<T> {
    @Override
    public int getFutureTimeout() {
        return 100;
    }

    @Override
    public T call() throws Exception {
        return super.getValue();
    }
}
