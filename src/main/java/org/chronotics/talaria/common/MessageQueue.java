package org.chronotics.talaria.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;

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
	
	public final static String REMOVAL_NOTIFICATION = "rm";
	
	private static final Logger logger = 
			LoggerFactory.getLogger(MessageQueue.class);
	
	public static int default_maxQueueSize = 100000;
	public enum OVERFLOW_STRATEGY {
		NO_INSERTION,
		DELETE_FIRST,
		RUNTIME_EXCEPTION;
	}
	private ConcurrentLinkedDeque<E> queue = null;
	private int queueSize = 0;
	
	private int maxQueueSize = 0;
	private OVERFLOW_STRATEGY overflowStrategy;
	private Class<E> type;
	private boolean removalNotification = false;
	private boolean stopAdd = false;

	@SuppressWarnings("unused")
	private MessageQueue(Class<E> cls) {
		type = cls;
	}
	public MessageQueue(
			Class<E> cls,
			int _maxQueueSize, 
			OVERFLOW_STRATEGY _overflowStrategy) {
		queue = new ConcurrentLinkedDeque<E>();
		type = cls;
		maxQueueSize = _maxQueueSize;
		overflowStrategy = _overflowStrategy;
		queueSize = 0;
		removalNotification = false;
		stopAdd = false;
	}
	
	public Class<E> getElementClass() {
		return type;
	}
	
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	private synchronized void increaseQueueSize() {
		queueSize++;
	}

	private synchronized void decreaseQueueSize() {
		queueSize--;
	}

	private synchronized int queueSize() {
		return queueSize;
	}

	public synchronized void setRemovalNotification(boolean _v) {
		removalNotification = _v;
	}

	public synchronized void stopAdd(boolean _b) {
		stopAdd = _b;
	}

	public void addLast(E _e) {
		if(stopAdd) {
			return;
		}
		if(queueSize() >= maxQueueSize) {
			switch (overflowStrategy) {
                case NO_INSERTION:
                    logger.info("MessageQueue is overflowed, element is not inserted");
                    return;
                case DELETE_FIRST:
                    logger.info("MessageQueue is overflowed, first element is removed");
                    E ret = this.removeFirst();
                    ret = null;
                    break;
                case RUNTIME_EXCEPTION:
                    logger.info("MessageQueue is overflowed, nothing is changed");
                    throw(new RuntimeException("MessageQueue is overflowed"));
                default:
                    logger.error("undefined strategy type");
                    return;
			}
		}
		// ConcurrentDeque -> synchronized is guaranteed
		queue.addLast(_e);
        increaseQueueSize();
        notifyObservers(_e);
	}

	/**
	 *
	 * @param _c
	 * @return
	 */
	public boolean addAll(Collection<? extends E> _c) {
		if(stopAdd) {
			return false;
		}
		if(queueSize() + _c.size() >= maxQueueSize) {
			switch (overflowStrategy) {
                case NO_INSERTION:
                    logger.info("MessageQueue is overflowed, element is not inserted");
                    return false;
                case DELETE_FIRST:
                    for(E e:_c) {
                        if(queueSize() >= maxQueueSize) {
                            logger.info("MessageQueue is overflowed, first element is removed");
                            this.removeFirst();
                        }
                        this.addLast(e);
                    }
                    return true;
				case RUNTIME_EXCEPTION:
					logger.info("MessageQueue is overflowed, nothing is changed");
					throw(new RuntimeException("MessageQueue is overflowed"));
				default:
					logger.error("undefined strategy type");
					return false;
			}
		} 
		
		boolean rt = true;
		rt = queue.addAll(_c);
		if(rt) {
			synchronized (this) {
				queueSize += _c.size();
			}
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
	
	public E getFirst() {
		return queue.getFirst();
	}

	public E getLast() {
		return queue.getLast();
	}

	/**
	 * @param o - element to be removed from this deque, if present
	 * @return
	 * true if the deque contained the specified element
	 * @throw
	 * NullPointerException - if the specified element is null
	 */
	public boolean remove(Object o) {
        if (queue.remove(o)) {
            decreaseQueueSize();
           	if (removalNotification == true) {
				notifyObservers(this.REMOVAL_NOTIFICATION);
			}
            return true;
        } else {
        	throw(new NoSuchElementException());
        }
	}

	/**
	 * @return
	 * the head of this deque
	 * @throw
	 * NoSuchElementException - if this deque is empty
	 */
	public E removeFirst() {
        E ret = queue.removeFirst();
        if (ret != null) {
			decreaseQueueSize();
			if (removalNotification == true) {
				notifyObservers(this.REMOVAL_NOTIFICATION);
			}
        }
        return ret;
	}

	/**
	 * @return
	 * the tail of this deque
	 * @throw
	 * NoSuchElementException - if this deque is empty
	 */
	public E removeLast() {
        E ret = queue.removeLast();
        if (ret != null) {
            decreaseQueueSize();
			if (removalNotification == true) {
				notifyObservers(this.REMOVAL_NOTIFICATION);
			}
        }
        return ret;
	}

	public synchronized int size() {
		assert(queueSize == queue.size());
		if(queueSize != queue.size()) {
			logger.error("queueSize: {}, queue.size(): {}", queueSize, queue.size());
		}

		// Use queueSize instead of queue.size();
		// Time complexity of ConcurrentQueue.size() is not O(1) but O(n)
		return queueSize;
	}
	
	public Object[] toArray() {
		return queue.toArray();
	}
	
	public <E> E[] toArray(E[] a) {
		return queue.toArray(a);
	}

	public synchronized void clear() {
		queue.clear();
		queueSize = 0;
	}
}
