package org.chronotics.talaria.common.taskexecutor;

import org.chronotics.talaria.common.TaskExecutor;

public class WriteBinaryFileExecutor<T> extends TaskExecutor<T>  {

	protected WriteBinaryFileExecutor(PROPAGATION_RULE _propagationRule, TaskExecutor<T> _nextExecutor) {
		super(_propagationRule, _nextExecutor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public T call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFutureTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

}
