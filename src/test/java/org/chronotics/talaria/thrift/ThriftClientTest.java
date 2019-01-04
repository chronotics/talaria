package org.chronotics.talaria.thrift;

import org.apache.thrift.TException;
import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.taskexecutor.EmptyExecutor;
import org.chronotics.talaria.common.taskexecutor.SimplePrintExecutor;
import org.chronotics.talaria.common.thriftservice.ThriftServiceWithMessageQueue;
import org.chronotics.talaria.thrift.gen.TransferService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ThriftClientProperties.class})
public class ThriftClientTest {

	private static final Logger logger =
			LoggerFactory.getLogger(ThriftClientTest.class);

	@Autowired
	private ThriftClientProperties clientProperties;

	private static ThriftServer thriftServer = null;

	private String messageQueueId = "thrift";

    private char[] v10k = new char[5000]; // char: 2byte X 5,000 = 10,000 byte
	private char[] v100k = new char[50000]; // char: 2byte X 50,000 = 100,000 byte
	private char[] v200k = new char[100000];
	private char[] v300k = new char[150000];
	private char[] v400k = new char[200000];
	private char[] v500k = new char[250000];
    private char[] v600k = new char[300000];
    private char[] v700k = new char[350000];
    private char[] v800k = new char[400000];
    private char[] v900k = new char[450000];
    private char[] v1000k = new char[500000];

	private static int countForAverage = 10;
	private static int count = 100;

	@BeforeClass
	public static void setup() {
	    if(thriftServer != null) {
	        return;
        }
		ThriftServerProperties serverProperties =
                new ThriftServerProperties();
		serverProperties.setIp("localhost");
		serverProperties.setPort("9091");
		serverProperties.setServerType("simple");
		TaskExecutor<Object> executorToRead =
				new EmptyExecutor<>();
//		TaskExecutor<Object> executorToWrite =
//                new SimplePrintExecutor<Object>(
//                        TaskExecutor.PROPAGATION_RULE.STEP_BY_STEP_ORIGINAL_ARG,
//                        null);
        TaskExecutor<Object> executorToWrite =
                new EmptyExecutor<>();
        ThriftServiceExecutor thriftServiceExecutor =
                new ThriftServiceExecutor(executorToRead, executorToWrite);
		ThriftService thriftServiceHandler =
                new ThriftServiceWithMessageQueue(thriftServiceExecutor);
		thriftServer = new ThriftServer(thriftServiceHandler, serverProperties);
		thriftServer.start();
	}

	@AfterClass
	public static void teardown() {
		thriftServer.stop();
	}


	@Test
	public void getProperties() {
		assertEquals("localhost", clientProperties.getIp());
		assertEquals("9091", clientProperties.getPort());
	}

	@Test
	public void startStopThriftClient() {
		ThriftClient client = new ThriftClient(clientProperties);
		client.start();

		TransferService.Client service = client.getService();
		logger.info("hello client");

		// create the message queue
		try {
			boolean ret = service.writeId(messageQueueId);
			if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
			    logger.info("Id \"thrift\" is not inserted");
            }
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info(e.toString());
		}

		List<String> tempList =
				new ArrayList<String>();

        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
		for(int i=0; i<count; i++) {
			try {
				String ret =
                        service.writeString(messageQueueId, String.valueOf(i));
				tempList.add(String.valueOf(i));
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info(e.toString());
			}
            assertEquals(i+1,mq.size());
		}
		logger.info("thrift write is done");

		assertEquals(count, mq.size());

		for(int i=0; i<count; i++) {
			try {
				String value = null;
				value = service.readString(messageQueueId);
				if(value != null) {
					tempList.remove(value);
				} else {
					break;
				}
                assertEquals(count-(i+1),mq.size());
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info(e.toString());
			}
		}
		logger.info("thrift read is done");

        assertEquals(0, mq.size());

		client.stop();
        mq.clear();
	}

	@Test
	public void Test100k() {
		ThriftClient client = new ThriftClient(clientProperties);
		client.start();

		TransferService.Client service = client.getService();

		// create the message queue
		try {
			boolean ret = service.writeId(messageQueueId);
			if(ret) {
				logger.info("Id \"thrift\" is inserted");
			} else {
				logger.info("Id \"thrift\" is not inserted");
			}
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info(e.toString());
		}

		String msg = new String(v100k);
		System.out.println(msg.length()*2);

		float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
		for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
            System.out.format("elapsed Time : %d \n", elapsedTime);
        }

		System.out.format("100k, average elapsed Time : %f \n", avrElapsedTime);

		client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
	}

    @Test
    public void Test200k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v200k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }

    @Test
    public void Test300k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v300k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }

    @Test
    public void Test400k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v400k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }

    @Test
    public void Test500k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v500k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }

    @Test
    public void Test600k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v600k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }

    @Test
    public void Test700k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v700k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }

    @Test
    public void Test800k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v800k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }

    @Test
    public void Test900k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v900k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }

    @Test
    public void Test1000k() {
        ThriftClient client = new ThriftClient(clientProperties);
        client.start();

        TransferService.Client service = client.getService();

        // create the message queue
        try {
            boolean ret = service.writeId(messageQueueId);
            if(ret) {
                logger.info("Id \"thrift\" is inserted");
            } else {
                logger.info("Id \"thrift\" is not inserted");
            }
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info(e.toString());
        }

        String msg = new String(v1000k);

        float avrElapsedTime = 0;
        MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);
        mq.clear();
        for(int c=0; c<countForAverage; c++) {
            long startingTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                try {
                    service.writeString(messageQueueId, msg);
                } catch (TException e) {
                    e.printStackTrace();
                    logger.info(e.toString());
                }
                assertEquals(i+1+c*count,mq.size());
            }
            long endingTime = System.currentTimeMillis();
            long elapsedTime = endingTime - startingTime;
            if(avrElapsedTime == 0) {
                avrElapsedTime = elapsedTime;
            } else {
                avrElapsedTime = (float) ((avrElapsedTime + (float)elapsedTime) / 2.0);
            }
        }

        System.out.format("200k, elapsed Time : %f \n", avrElapsedTime);

        client.stop();

        assertEquals(count*countForAverage, mq.size());
        mq.clear();
    }
}
