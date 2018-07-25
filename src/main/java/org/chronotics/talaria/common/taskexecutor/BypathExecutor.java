package org.chronotics.talaria.common.taskexecutor;

import org.chronotics.talaria.common.TaskExecutor;

public class BypathExecutor <T> extends TaskExecutor<T> {
    @Override
    public int getFutureTimeout() {
        return 0;
    }

    @Override
    public T call() throws Exception {
        return super.getValue();
    }
}
