package org.chronotics.talaria.thrift;

import java.util.concurrent.Future;

import org.chronotics.talaria.common.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftServiceExecutor {
	
		private static final Logger logger = 
				LoggerFactory.getLogger(ThriftServiceExecutor.class);
		
		protected TaskExecutor<Object> executorToRead = null;
		protected TaskExecutor<Object> executorToWrite = null;
		
		public ThriftServiceExecutor(
				TaskExecutor<Object> _executorToRead,
				TaskExecutor<Object> _executorToWrite) {
			this.setExecutorToRead(_executorToRead);
			this.setExecutorToWrite(_executorToWrite);
		}
		
		protected void setExecutorToRead(TaskExecutor<Object> _executor) {
			executorToRead = _executor;
		}

		protected void setExecutorToWrite(TaskExecutor<Object> _executor) {
			executorToWrite = _executor;
		}
		
		public Object executeToRead(Object _arg) {
			try {
				Future<Object> future = executorToRead.execute(_arg);
				return future.get();
			} catch (Exception e) {
				logger.error("exception in executeToRead");
				e.printStackTrace();
			}
			return null;
		}
		
		public Object executeToWrite(Object _arg) {
			try {
				Future<Object> future = executorToWrite.execute(_arg);
				return future.get();
			} catch (Exception e) {
				logger.error("exception in executeToWrite");
				e.printStackTrace();
			}
			return null;
		}
}
