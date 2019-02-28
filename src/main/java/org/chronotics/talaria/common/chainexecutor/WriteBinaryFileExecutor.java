package org.chronotics.talaria.common.chainexecutor;

import org.chronotics.talaria.common.ChainExecutor;

public class WriteBinaryFileExecutor<T> extends ChainExecutor<T> {

	protected WriteBinaryFileExecutor(PROPAGATION_RULE _propagationRule, ChainExecutor<T> _nextExecutor) {
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
