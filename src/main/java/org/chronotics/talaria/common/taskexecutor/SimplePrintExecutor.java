package org.chronotics.talaria.common.taskexecutor;

import org.chronotics.talaria.common.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePrintExecutor<T> extends TaskExecutor<T> {

    private static final Logger logger =
            LoggerFactory.getLogger(SimplePrintExecutor.class);

    public SimplePrintExecutor(
            PROPAGATION_RULE _propagationRule,
            TaskExecutor<T> _nextExecutor) {
       super(_propagationRule,_nextExecutor);
    }

    @Override
    public int getFutureTimeout() {
        return 100;
    }

    @Override
    public T call() throws Exception {
        T v = super.getValue();
        logger.info("value: {}",v);
        return v;
    }
}
