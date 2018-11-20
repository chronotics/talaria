package org.chronotics.talaria.common;

import java.nio.BufferOverflowException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Observable;
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
	
	public static String REMOVALMESSAGE = "rm";
	
	private static final Logger logger = 
			LoggerFactory.getLogger(MessageQueue.class);
	
	public static int default_maxQueueSize = 100000;
	public enum OVERFLOW_STRATEGY {
		NO_INSERTION,
		DELETE_FIRST,
		RUNTIME_EXCEPTION;
	}
	private ConcurrentLinkedDeque<E> queue =
			new ConcurrentLinkedDeque<E>();
	private int queueSize = 0;
	
	private int maxQueueSize = 0;
	private OVERFLOW_STRATEGY overflowStrategy;
	private Class<E> type;
	private boolean notifyRemoval = false;
	private boolean stop = false;

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
		queueSize = 0;
		notifyRemoval = false;
		stop = false;
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

	public synchronized void setNotifyRemoval(boolean _v) {
		notifyRemoval = _v;
	}

	public synchronized void setStop(boolean _b) {
		stop = _b;
	}

	public void addLast(E _e) {
		if(stop) {
			return;
		}
		if(queueSize() >= maxQueueSize) {
			switch (overflowStrategy) {
                case NO_INSERTION:
                    logger.info("MessageQueue is overflowed, element is not inserted");
                    return;
                case DELETE_FIRST:
                    logger.info("MessageQueue is overflowed, first element is removed");
                    this.removeFirst();
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
		synchronized (this) {
        	if(countObservers()!=0) {
				setChanged();
				notifyObservers(_e);
			}
        }
	}

	/**
	 *
	 * @param _c
	 * @return
	 */
	public boolean addAll(Collection<? extends E> _c) {
		if(stop) {
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
				if(countObservers()!=0) {
					setChanged();
					notifyObservers(_c);
				}
			}
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
		if(stop) {
			return null;
		}
		return queue.getFirst();
	}

	public E getLast() {
		if(stop) {
			return null;
		}
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
		if(stop) {
			return false;
		}
        if (queue.remove(o)) {
            decreaseQueueSize();
			synchronized (this) {
            	if (countObservers() != 0 && notifyRemoval == true) {
					setChanged();
					notifyObservers(this.REMOVALMESSAGE);
				}
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
		if(stop) {
			return null;
		}
        E ret = queue.removeFirst();
        if (ret != null) {
			decreaseQueueSize();
        	synchronized (this) {
				if (countObservers() != 0 && notifyRemoval == true) {
					setChanged();
					notifyObservers(this.REMOVALMESSAGE);
				}
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
		if(stop) {
			return null;
		}
        E ret = queue.removeLast();
        if (ret != null) {
            decreaseQueueSize();
            synchronized (this) {
				if (countObservers() != 0 && notifyRemoval == true) {
					setChanged();
					notifyObservers(this.REMOVALMESSAGE);
				}
			}
        }
        return ret;
	}

	public synchronized int size() {
		if(stop) {
			return 0;
		}
		assert(queueSize == queue.size());
		if(queueSize != queue.size()) {
			logger.error("queueSize: {}, queue.size(): {}", queueSize, queue.size());
		}

		// Use queueSize instead of queue.size();
		// Time complexity of ConcurrentQueue.size() is not O(1) but O(n)
		return queueSize;
	}
	
	public Object[] toArray() {
		if(stop) {
			return null;
		}
		return queue.toArray();
	}
	
	public <E> E[] toArray(E[] a) {
		if(stop) {
			return null;
		}
		return queue.toArray(a);
	}

	public void clear() {
		if(stop) {
			return;
		}
		queue.clear();
		queueSize = 0;
	}
}
