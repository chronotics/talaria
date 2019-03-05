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

//	protected ExecutorService executorService = null;
//	protected Callable<Object> executorToRead = null;
//	protected Callable<Object> executorToWrite = null;

    public ThriftServiceExecutor(
            TaskExecutor<Object> _executorToRead,
            TaskExecutor<Object> _executorToWrite) {
//			Callable<Object> _executorToRead,
//			Callable<Object> _executorToWrite) {
        this.setExecutorToRead(_executorToRead);
        this.setExecutorToWrite(_executorToWrite);
//        executorService =
//                Executors.newSingleThreadExecutor();
////					Executors.newFixedThreadPool(2 + getChildrenExecutorCount()*2);
    }

    protected void setExecutorToRead(TaskExecutor<Object> _executor) {
        executorToRead = _executor;
    }

    protected void setExecutorToWrite(TaskExecutor<Object> _executor) {
        executorToWrite = _executor;
    }

//    protected void setExecutorToRead(Callable<Object> _executor) {
//        executorToRead = _executor;
//    }
//
//    protected void setExecutorToWrite(Callable<Object> _executor) {
//        executorToWrite = _executor;
//    }

    public Future<Object> executeToRead(Object _arg) throws Exception {
        if (executorToRead == null) {
            return null;
        }
		return executorToRead.execute(_arg);
//        return executorService.submit(executorToRead);
    }

    public Future<Object> executeToWrite(Object _arg) throws Exception {
        if (executorToWrite == null) {
            return null;
        }
		return executorToWrite.execute(_arg);
//        return executorService.submit(executorToWrite);
    }
}
