package org.chronotics.talaria.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author SG Lee
 * @since 3/20/2015
 * @description
 * This Class is to execute your own task with the input <T>
 * You can register multiple Executors and they will run sequentially.
 * A registered task is executed with thread based on ExecutorService.
 */

public abstract class TaskExecutor<T> implements Callable<T> {
	
	private static final Logger logger = 
			LoggerFactory.getLogger(TaskExecutor.class);
	
	public static class Builder<T> {
		private TaskExecutor<T> executor = null;
		
		public Builder() {}
		
		public TaskExecutor<T> build() {
			return executor;
		}
		
		public Builder<T> setExecutor(
				TaskExecutor<T> _executor,
				PROPAGATION_RULE _propagationRule) {
			assert(executor == null);
			if(executor != null) {
				logger.error("You can not define Executor before calling setExecutor");
			}
			executor = _executor;
			executor.addNextExecutor(_propagationRule, null);
			return this;
		}
		
		public Builder<T> addExecutor(
				TaskExecutor<T> _executor) throws Exception {
			assert(executor != null && executor != _executor);
			if(executor == null) {
				logger.error("You have to call setExecutor before addExecutor");
				throw new Exception("You have to call setExecutor before addExecutor");
			}
			if(executor == _executor) {
				logger.error("You can not add existing TaskExecutor");
				throw new Exception("You can not add existing TaskExecutor");
			}
			
			TaskExecutor<T> finalExecutor = executor;
			while(finalExecutor.getNextExecutor() != null ) {
				finalExecutor=finalExecutor.getNextExecutor();
				assert(finalExecutor != _executor);
				if(finalExecutor == _executor) {
					throw new Exception("You can not add existing TaskExecutor");
				}
			}
			finalExecutor.addNextExecutor(finalExecutor.getProgatationRule(), _executor);
			return this;
		}
	}
			
	// for executorService.awaitTermination()
	public static int terminationTimeout = 2000; // ms
	
	/**
	 * @PROPAGATION_RULE
	 * enumerator
	 * @NOT_DEFINED
	 * not defined value
	 * @SIMULTANEOUSLY 
	 * execute registered executor simultaneously
	 * the value for each executor is the argument that the first Executor received
	 * @STEP_BY_STEP_REGENERATED_ARG
	 * execute registered executor step by step
	 * the value for each executor is the argument that the previous Executor regenerated
	 * @STEP_BY_STEP_ORIGINAL_ARG
	 * execute registered executor step by step
	 * the value for each executor is the argument that the first Executor received
	 * @author sglee
	 *
	 */
	public static enum PROPAGATION_RULE {
		NOT_DEFINED,
		SIMULTANEOUSLY,
		STEP_BY_STEP_REGENERATED_ARG,
		STEP_BY_STEP_ORIGINAL_ARG
	}

	protected ExecutorService executorService = null;
	
	protected TaskExecutor<T> nextExecutor = null;
	
	protected PROPAGATION_RULE propagationRule = PROPAGATION_RULE.NOT_DEFINED;
	
	protected T value;
	
	@SuppressWarnings("unused")
	private TaskExecutor() {}
	
	protected TaskExecutor(PROPAGATION_RULE _propagationRule, TaskExecutor<T> _nextExecutor) {
		addNextExecutor(_propagationRule, _nextExecutor);
	}
	
	public int getChildrenExecutorCount() {
		TaskExecutor<T> finalExecutor = this;
		int count = 0;
		while(finalExecutor.getNextExecutor() != null) {
			finalExecutor = finalExecutor.getNextExecutor();
			count++;
		}
		return count;
	}

	private void setPropagationRule(PROPAGATION_RULE _propagationRule) {
		propagationRule = _propagationRule;
	}
	
	public PROPAGATION_RULE getProgatationRule() {
		return propagationRule;
	}
	
	private void setValue(T _value) {
		value = _value;
	}
	
	protected T getValue() {
		return value;
	}

	// You can execute many tasks with added executors
	// The code will be run like 
	// this.call() => nextExecutor.call() => nextExecutor.call()
	public void addNextExecutor(PROPAGATION_RULE _propagationRule, TaskExecutor<T> _nextExecutor) {
		this.setPropagationRule(_propagationRule);
		
		nextExecutor = _nextExecutor;

		if(nextExecutor != null) {
			if(nextExecutor.getProgatationRule() == PROPAGATION_RULE.NOT_DEFINED) {
				nextExecutor.setPropagationRule(this.getProgatationRule());
			}
			
			if(nextExecutor.getProgatationRule() != this.getProgatationRule()) {
				throw new IllegalStateException(
						"Propagation rules should be the same"
						);
			}
		}

	}
	
	public TaskExecutor<T> getNextExecutor() {
		return nextExecutor;
	}
	
	/**
	 * 
	 * @param _arg
	 * task to execute
	 * @return
	 * Future<T> 
	 * @throws Exception
	 * There are three rules to execute Tasks with children executors
	 * Children executors are defines with chain rule
	 * Execution rules are
	 * SIMULTANEOUSLY : simultaneous execution of children tasks
	 * STEP_BY_STEP_* : sequential execution
	 * STEP_BY_STEP__ORIGINAL_ARG : sequential execution, all executors use same argument
	 * STEP_BY_STEP__REGENERATED_ARG : sequential execution, the next executor use the argument of the previous one
	 */
	public final Future<T> execute(T _arg) throws Exception {
		if(executorService == null) {
			executorService = 
					Executors.newSingleThreadExecutor();
					//Executors.newFixedThreadPool(this.getExecutorCount() + (int)(this.getExecutorCount()+0.5));
		}
	
		this.setValue(_arg);
		Future<T> future = executorService.submit(this);

		if(future == null) {
			throw(new NullPointerException());
		}
	
		if(nextExecutor == null) {
			return future;
		}
		
		if(propagationRule == PROPAGATION_RULE.SIMULTANEOUSLY) {
			nextExecutor.setValue(_arg);
			return nextExecutor.execute(_arg);
		} else {
			T prevResult = null;
			try{
				prevResult = future.get(getFutureTimeout(), TimeUnit.MILLISECONDS);
			} catch(Exception e) {
				
			}
			if(propagationRule == PROPAGATION_RULE.STEP_BY_STEP_ORIGINAL_ARG) {
				nextExecutor.setValue(_arg);
				return nextExecutor.execute(_arg);
			} else if(propagationRule == PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG) {
				nextExecutor.setValue(prevResult);
				return nextExecutor.execute(prevResult);
			} else {
				throw(new NoSuchMethodException());
			}
		}
	}

	// You can register attributes whose type is Object
	// with which you execute your task in derived executeImp()
	protected Map<String,Object> properties = new HashMap<String,Object>();

	public void putProperty(String _key, Object _value) {
		properties.put(_key, _value);
	}
	
	public Object getProperty(String _key) {
		return properties.get(_key);
	}
	
	public Set<Entry<String,Object>> getSetOfProperty() {
		return properties.entrySet();
	}
	
	public void setProperty(TaskExecutor<?> _executor) {
		if(!properties.isEmpty()) {
			properties.clear();
		}
		Set<Entry<String,Object>> properties = _executor.getSetOfProperty();
		for(Entry<String,Object> entry : properties) {
			this.putProperty(entry.getKey(), entry.getValue());
		}
	}
	
	public void quitExecutor() {
		executorService.shutdown();
		if(nextExecutor != null) {
			nextExecutor.quitExecutor();
		}
		
        try {
        	if(executorService.awaitTermination(terminationTimeout, TimeUnit.MILLISECONDS)) {
        		logger.info("executorService termination succeeded");
        	} else {
        		logger.info("executorService termination failed");
        	}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
	
	// timeout for future(), time unit should be TimeUnit.MILLISECONDS
	public abstract int getFutureTimeout();
}
