package org.chronotics.talaria.common;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestMessageQueue {
    private static final Logger logger =
            LoggerFactory.getLogger(TestMessageQueue.class);

    private static String queId = "testQueue";

    private static List<String> msgList = null;
    private static int msgListSize = 10000000;
    private static long insertionTime = 1000;
    private static int queSize = 10;

    @BeforeClass
    public synchronized static void setup() {
        msgList = new ArrayList<>();
        for(int i=0; i<msgListSize; i++) {
            msgList.add(String.valueOf(i));
        }

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        mqMap.clear();
        MessageQueue<String> mq =
                new MessageQueue<>(
                        String.class,
                        queSize,
                        MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
        mqMap.put(queId, mq);

        insertionTime = Math.max(insertionTime, msgListSize / 1000);
    }

    @AfterClass
    public synchronized static void teardown() {
        MessageQueue<String> mq =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(queId);
        if(mq!=null) {
            mq.clear();
        }
    }

    @Test
    public void addToMessageQueue() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MessageQueueMap mqMap = MessageQueueMap.getInstance();
                MessageQueue<String> mq =
                        (MessageQueue<String>) MessageQueueMap.getInstance().get(queId);
                for(String msg: msgList) {
                    mq.addLast(msg);
                }
                assertEquals(queSize, mq.size());
            }
        });
    }

    @Test
    public void addMessageToConcurrentLinkedDeque() throws InterruptedException {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ConcurrentLinkedDeque<String> queue =
                        new ConcurrentLinkedDeque<>();
                long startTime = System.currentTimeMillis();
                for(String msg: msgList) {
                    queue.add(msg);
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                logger.info("Elapsed time to add elements to Queue : {} ms", elapsedTime);
                assertTrue(elapsedTime < insertionTime);
            }
        });
        Thread.sleep(insertionTime);
    }

    @Test
    public void addMessage() throws InterruptedException {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MessageQueue<String> mq =
                        (MessageQueue<String>) MessageQueueMap.getInstance().get(queId);
                long startTime = System.currentTimeMillis();
                for(String msg: msgList) {
                    mq.addLast(msg);
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                logger.info("Elapsed time to add elements to Queue : {} ms", elapsedTime);
                assertTrue(elapsedTime < insertionTime);
            }
        });

        Thread.sleep(insertionTime);
        MessageQueue<String> mq =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(queId);
//        assertEquals(msgList.size(), mq.size());
        assertEquals(queSize, mq.size());
        mq.clear();
        assertEquals(0, mq.size());
    }

    @Test
    public void testAddandPoll() {
        MessageQueue<String> mq =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(queId);
//        mq.removeFirst();
        assertThrows(NoSuchElementException.class, () -> mq.removeFirst());
        assertEquals(0, mq.size());

        mq.addLast("1");
        assertEquals(1,mq.size());
        mq.removeFirst();
        assertEquals(0,mq.size());
    }

    @Test
    public void testRemove() {
        MessageQueue<String> mq =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(queId);
        assertThrows(NoSuchElementException.class, () -> mq.remove("no value"));
        assertEquals(queSize,mq.size());
        mq.clear();
        assertEquals(0, mq.size());

        mq.addLast("new value");
        assertEquals(1,mq.size());
        assertTrue(mq.remove("new value"));
        assertEquals(0,mq.size());
    }
}
