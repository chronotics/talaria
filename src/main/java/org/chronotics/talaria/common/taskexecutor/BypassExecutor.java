package org.chronotics.talaria.common.taskexecutor;

import org.chronotics.talaria.common.TaskExecutor;

public class BypassExecutor <T> extends TaskExecutor<T> {
    @Override
    public int getFutureTimeout() {
        return 100;
    }

    @Override
    public T call() throws Exception {
        return super.getValue();
    }
}
