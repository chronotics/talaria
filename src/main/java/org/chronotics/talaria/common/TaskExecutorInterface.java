package org.chronotics.talaria.common;

// temporary class

public interface TaskExecutorInterface<T> {
    enum PROPAGATION_RULE {
        NOT_DEFINED,
        SIMULTANEOUSLY,
        STEP_BY_STEP_REGENERATED_ARG,
        STEP_BY_STEP_ORIGINAL_ARG
    }

    class Builder<T> {
        private TaskExecutorInterface<T> executor = null;
        Builder setExecutor(TaskExecutorInterface<T> _executor) {
            executor = _executor;
            return this;
        }
    }


    T call();

    default TaskExecutorInterface<T> nextExecutor() {
        return null;
    }

    default TaskExecutorInterface.PROPAGATION_RULE propagationRule() {
        return null;
    }

    default void execute(
            PROPAGATION_RULE _propergationRule,
            TaskExecutorInterface<T> _nextExecutor,
            T ..._values) {
        this.call();
        if(_propergationRule == PROPAGATION_RULE.SIMULTANEOUSLY) {

        } else {

        }

        if(_nextExecutor!=null) {
            _nextExecutor.execute(
                    _nextExecutor.propagationRule(),
                    _nextExecutor.nextExecutor(),
                    _values);
        }
    }


}
