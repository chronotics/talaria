package org.chronotics.talaria.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author SG Lee
 * @since 3/20/2015
 * @description
 * This Class is wrapper class of ConcurrentLinkedQueue
 * You can manage the size of queue and the action when the queue is overflowed
 */

public class MessageQueue<E> extends Observable {
	
	public static String notifyingMessageRemove = "element is removed";
	
	private static final Logger logger = 
			LoggerFactory.getLogger(MessageQueue.class);
	
	public static int default_maxQueueSize = 1000;
	public enum OVERFLOW_STRATEGY {
		NO_INSERTION,
		DELETE_FIRST;
	}
	private ConcurrentLinkedQueue<E> queue = 
			new ConcurrentLinkedQueue<E>();
	
	private int maxQueueSize;
	private OVERFLOW_STRATEGY overflowStrategy;
	private Class<E> type;

	@SuppressWarnings("unused")
	private MessageQueue(Class<E> cls) {
		type = cls;
	}
	public MessageQueue(
			Class<E> cls,
			int _maxQueueSize, 
			OVERFLOW_STRATEGY _overflowStrategy) {
		type = cls;
		maxQueueSize = _maxQueueSize;
		overflowStrategy = _overflowStrategy;
	}
	
	public Class<E> getElementClass() {
		return type;
	}
	
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public boolean add(E _e) {
		if(queue.size() >= maxQueueSize) {
			switch (overflowStrategy) {
			case NO_INSERTION:
				logger.info("queue overflow, element is not inserted");
				return false;
			case DELETE_FIRST:
				logger.info("queue overflow, first element is removed");
				this.poll();
			default:
				logger.info("queue overflow, nothing is changed");
				return false;
			}
		}
		
		boolean rt = true;
		try {
			rt = queue.add(_e);
		} catch(IllegalStateException e) {
			logger.error(e.toString());;
			return false;
		}
		if(rt) {
			setChanged();
			notifyObservers(_e);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean addAll(Collection<? extends E> _c) {
		if(queue.size() + _c.size() >= maxQueueSize) {
			switch (overflowStrategy) {
			case NO_INSERTION:
				logger.info("queue overflow, element is not inserted");
				return false;
			case DELETE_FIRST:
				for(E e:_c) {
					if(queue.size() >= maxQueueSize) {
						logger.info("queue overflow, first element is removed");
						this.poll();
					}
					this.add(e);
				}
				return true;
			default:
				return false;
			}
		} 
		
		boolean rt = true;
		try {
			rt = queue.addAll(_c);
		} catch(Exception e) {
			logger.error(e.toString());;
			return false;
		}
		if(rt) {
			setChanged();
			notifyObservers(_c);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public Iterator<E> iterator() {
		return queue.iterator();
	}
	
	public boolean offer(E e) {
		if(queue.offer(e)) {
			setChanged();
			notifyObservers(e);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Description copied from interface: Queue
	 * Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.
	 * @return
	 * the head of this queue, or null if this queue is empty
	 */
	public E peek() {
		return queue.peek();
	}
	
	/**
	 * Description copied from interface: Queue
	 * Retrieves and removes the head of this queue, or returns null if this queue is empty.
	 * @return
	 * the head of this queue, or null if this queue is empty
	 */
	public E poll() {
		setChanged();
		notifyObservers(this.notifyingMessageRemove);
		return queue.poll(); 
	}
	
	public boolean remove(Object o) {
		if(queue.remove(o)) {
			setChanged();
			notifyObservers(this.notifyingMessageRemove);
			return true;
		} else {
			return false;
		}
	}
	
	public int size() {
		return queue.size();
	}
	
	public Object[] toArray() {
		return queue.toArray();
	}
	
	public <E> E[] toArray(E[] a) {
		return queue.toArray(a);
	}
}
