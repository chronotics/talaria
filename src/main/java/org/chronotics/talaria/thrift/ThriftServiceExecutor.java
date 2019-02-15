package org.chronotics.talaria.thrift;

import java.util.concurrent.Future;

import org.chronotics.talaria.common.CallableExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftServiceExecutor {
	
		private static final Logger logger = 
				LoggerFactory.getLogger(ThriftServiceExecutor.class);
		
		protected CallableExecutor<Object> executorToRead = null;
		protected CallableExecutor<Object> executorToWrite = null;
		
		public ThriftServiceExecutor(
				CallableExecutor<Object> _executorToRead,
				CallableExecutor<Object> _executorToWrite) {
			this.setExecutorToRead(_executorToRead);
			this.setExecutorToWrite(_executorToWrite);
		}
		
		protected void setExecutorToRead(CallableExecutor<Object> _executor) {
			executorToRead = _executor;
		}

		protected void setExecutorToWrite(CallableExecutor<Object> _executor) {
			executorToWrite = _executor;
		}
		
		public Future<Object> executeToRead(Object _arg) throws Exception {
			if(executorToRead == null) {
				return null;
			}
			return executorToRead.execute(_arg);
		}
		
		public Future<Object> executeToWrite(Object _arg) throws Exception {
			if(executorToWrite == null) {
				return null;
			}
			return executorToWrite.execute(_arg);
		}
}
