package org.chronotics.talaria.common.chainexecutor;

import org.chronotics.talaria.common.ChainExecutor;

public class NullReturnExecutor<T> extends ChainExecutor<T> {
    @Override
    public int getFutureTimeout() {
        return 100;
    }

    @Override
    public T call() throws Exception {
        return null;
    }
}
