package org.chronotics.talaria.common.thriftservice;

import org.apache.thrift.TException;
import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.thrift.ThriftServiceHandler;
import org.chronotics.talaria.thrift.ThriftServiceExecutor;
import org.chronotics.talaria.thrift.gen.InvalidOperationException;
import org.chronotics.talaria.thrift.gen.ThriftMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Future;

///////////////////////////////////////!!!!!!!!!!!!!1
// default executor => EmptyExecutor
///////////////////////////////////////!!!!!!!!!!!!!1

public class ThriftServiceWithMessageQueue extends ThriftServiceHandler {

	private static final Logger logger = 
			LoggerFactory.getLogger(MessageQueue.class);
	
	public ThriftServiceWithMessageQueue(
			ThriftServiceExecutor _executor) {
		if(_executor != null) {
			super.setExecutor(_executor);
		}
	}

	private String writeFunc(ThriftServiceExecutor executor, Object _v) {
		if(executor == null) {
			return null;
		}
		Object rt = null;
		try {
			Future<Object> future = executor.executeToWrite(_v);
			rt = future.get();
		} catch (Exception e) {
			e.printStackTrace();
		}

//		return (rt != null) ? rt.toString() : null;
		return (rt != null) ? rt.toString() : "null";
	}

	private Object readFunc(ThriftServiceExecutor executor, Object _v)
			throws InvalidOperationException {
		if(executor == null) {
			return null;
		}
		Object rt = null;
		try {
			Future<Object> future = executor.executeToRead(_v);
			rt = future.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(rt == null) {
			throw new InvalidOperationException(-1,"null return");
		}
		return rt;
	}

	@Override
	public boolean ping() throws InvalidOperationException, TException {
		return true;
	}

	@Override
	public String writeThriftMessage(ThriftMessage _v) throws TException {
		String id = _v.get_sender_id();
		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		MessageQueue<ThriftMessage> mq =
				(MessageQueue<ThriftMessage>) mqMap.get(id);
		if(mq == null) {
			mq = new MessageQueue<ThriftMessage>(
					ThriftMessage.class,
					MessageQueue.default_maxQueueSize,
					MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
			mqMap.put(id, mq);
		}
		mq.addLast(_v);
//		logger.info("===== The size of MQ with a key of {} is {}", id, mq.size());

		return writeFunc(getExecutor(), _v);
	}

	@Override
	public String writeBool(String _id, boolean _v) throws TException {
		MessageQueue<Boolean> mq = 
				(MessageQueue<Boolean>) 
				MessageQueueMap.getInstance()
				.get(_id);
		assert(mq != null);
		if(mq != null) {
			mq.addLast(_v);
		}

		return writeFunc(getExecutor(), _v);
	}

	@Override
	public String writeI16(String _id, short _v) throws TException {
		MessageQueue<Short> mq = 
				(MessageQueue<Short>) 
				MessageQueueMap.getInstance()
				.get(_id);
		assert(mq != null);
		if(mq != null) {
			mq.addLast(_v);
		}

		return writeFunc(getExecutor(), _v);
	}

	@Override
	public String writeI32(String _id, int _v) throws TException {
		MessageQueue<Integer> mq = 
				(MessageQueue<Integer>) 
				MessageQueueMap.getInstance()
				.get(_id);
		assert(mq != null);
		if(mq != null) {
			mq.addLast(_v);
		}

		return writeFunc(getExecutor(), _v);
	}

	@Override
	public String writeI64(String _id, long _v) throws TException {
		MessageQueue<Long> mq = 
				(MessageQueue<Long>) 
				MessageQueueMap.getInstance()
				.get(_id);
		assert(mq != null);
		if(mq != null) {
			mq.addLast(_v);
		}

		return writeFunc(getExecutor(), _v);
	}

	@Override
	public String writeDouble(String _id, double _v) throws TException {
		MessageQueue<Double> mq = 
				(MessageQueue<Double>) 
				MessageQueueMap.getInstance()
				.get(_id);
		assert(mq != null);
		if(mq != null) {
			mq.addLast(_v);
		}

		return writeFunc(getExecutor(), _v);
	}

	@Override
	public String writeString(String _id, String _v) throws TException {
		MessageQueue<String> mq =
				(MessageQueue<String>) 
				MessageQueueMap.getInstance()
				.get(_id);
		assert(mq != null);
		if(mq != null) {
			mq.addLast(_v);
		}

		return writeFunc(getExecutor(), _v);
	}

	@Override
	public ThriftMessage readThriftMessage(String _id) throws TException {
		MessageQueue<ThriftMessage> mq =
				(MessageQueue<ThriftMessage>)
				MessageQueueMap.getInstance()
				.get(_id);
		if( mq == null) {
			logger.info("There is no matching queue with id");
			return null;
		}

		ThriftMessage value = mq.removeFirst();
		if(value == null) {
			logger.info("Queue is empty");
			return null;
		} else {
			return (ThriftMessage)readFunc(getExecutor(),value);
		}
	}

	@Override
	public boolean readBool(String _id) throws TException {
		MessageQueue<Boolean> mq = 
				(MessageQueue<Boolean>) 
				MessageQueueMap.getInstance()
				.get(_id);
		if( mq == null) {
			logger.info("There is no matching queue with id");
			throw new TException("There is no matching queue with id");
		}

		Boolean value = mq.removeFirst();
		if(value == null) {
			logger.info("Queue is empty");
			throw new TException("Queue is empty");
		} else {
			return (boolean)readFunc(getExecutor(),value);
		}
	}

	@Override
	public short readI16(String _id) throws TException {
		MessageQueue<Short> mq = 
				(MessageQueue<Short>) 
				MessageQueueMap.getInstance()
				.get(_id);
		if( mq == null) {
			logger.info("There is no matching queue with id");
			throw new TException("There is no matching queue with id");
		}
		
		Short value = mq.removeFirst();
		if(value == null) {
			logger.info("Queue is empty");
			throw new TException("Queue is empty");
		} else {
			return (short)readFunc(getExecutor(),value);
		}
	}

	@Override
	public int readI32(String _id) throws TException {
		MessageQueue<Integer> mq = 
				(MessageQueue<Integer>) 
				MessageQueueMap.getInstance()
				.get(_id);
		if( mq == null) {
			logger.info("There is no matching queue with id");
			throw new TException("There is no matching queue with id");
		}
		Integer value = mq.removeFirst();
		if(value == null) {
			logger.info("Queue is empty");
			throw new TException("Queue is empty");
		} else {
			return (int)readFunc(getExecutor(),value);
		}
	}

	@Override
	public long readI64(String _id) throws TException {
		MessageQueue<Long> mq = 
				(MessageQueue<Long>) 
				MessageQueueMap.getInstance()
				.get(_id);
		if( mq == null) {
			logger.info("There is no matching queue with id");
			throw new TException("There is no matching queue with id");
		}
		Long value = mq.removeFirst();
		if(value == null) {
			logger.info("Queue is empty");
			throw new TException("Queue is empty");
		} else {
			return (long)readFunc(getExecutor(),value);
		}
	}

	@Override
	public double readDouble(String _id) throws TException {
		MessageQueue<Double> mq = 
				(MessageQueue<Double>) 
				MessageQueueMap.getInstance()
				.get(_id);
		if( mq == null) {
			logger.info("There is no matching queue with id");
			throw new TException("There is no matching queue with id");
		}
		Double value = mq.removeFirst();
		if(value == null) {
			logger.info("Queue is empty");
			throw new TException("Queue is empty");
		} else {
			return (double)readFunc(getExecutor(),value);
		}
	}

	@Override
	public String readString(String _id) throws TException {
		MessageQueue<String> mq = 
				(MessageQueue<String>) 
				MessageQueueMap.getInstance()
				.get(_id);
		if( mq == null) {
			logger.info("There is no matching queue with id");
			throw new TException("There is no matching queue with id");
		}
		String value = mq.removeFirst();
		if(value == null) {
			logger.info("Queue is empty");
			throw new TException("Queue is empty");
		} else {
			return (String)readFunc(getExecutor(),value);
		}
	}

	@Override
	public boolean writeId(String _id) throws TException {
		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		if(mqMap.containsKey(_id)) {
			return false;
		}
		
		MessageQueue<String> mq = 
				new MessageQueue<String>(
					String.class,
					MessageQueue.default_maxQueueSize,
					MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
		return mqMap.put(_id, mq);
	}

	@Override
	public List<String> readId() throws TException {
		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		List<String> rt = new ArrayList<String>();
		for(Entry<Object, MessageQueue<?>> entry : mqMap.entrySet()) {
			rt.add((String) entry.getKey());
		}
		return rt;
	}
}
