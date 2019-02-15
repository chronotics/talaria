package org.chronotics.talaria.common.callableexecutor;

import org.chronotics.talaria.common.CallableExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePrintExecutor<T> extends CallableExecutor<T> {

    private static final Logger logger =
            LoggerFactory.getLogger(SimplePrintExecutor.class);

    public SimplePrintExecutor(
            PROPAGATION_RULE _propagationRule,
            CallableExecutor<T> _nextExecutor) {
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
