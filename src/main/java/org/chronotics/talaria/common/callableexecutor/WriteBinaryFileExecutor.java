package org.chronotics.talaria.common.callableexecutor;

import org.chronotics.talaria.common.CallableExecutor;

public class WriteBinaryFileExecutor<T> extends CallableExecutor<T> {

	protected WriteBinaryFileExecutor(PROPAGATION_RULE _propagationRule, CallableExecutor<T> _nextExecutor) {
		super(_propagationRule, _nextExecutor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public T call() throws Exception {
		// TODO Auto-generated method stub
		return super.getValue();
	}

	@Override
	public int getFutureTimeout() {
		// TODO Auto-generated method stub
		return 100;
	}

}
