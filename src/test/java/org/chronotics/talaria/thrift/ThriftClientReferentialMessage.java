package org.chronotics.talaria.thrift;

import org.apache.thrift.TException;
import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.taskexecutor.BypassExecutor;
import org.chronotics.talaria.common.taskexecutor.NullReturnExecutor;
import org.chronotics.talaria.thrift.thriftservicehandler.ThriftServiceWithMessageQueue;
import org.chronotics.talaria.thrift.gen.InvalidOperationException;
import org.chronotics.talaria.thrift.gen.ThriftMessage;
import org.chronotics.talaria.thrift.gen.ThriftRWService;
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
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ThriftClientProperties.class})
public class ThriftClientReferentialMessage {

	private static final Logger logger =
			LoggerFactory.getLogger(ThriftClientReferentialMessage.class);

	@Autowired
	private ThriftClientProperties clientProperties;

	private static ThriftServer thriftServer = null;

	private static String messageQueueId = "d0";

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
				new BypassExecutor<>();
//				new NullReturnExecutor<>();
        TaskExecutor<Object> executorToWrite =
                new NullReturnExecutor<>();
        ThriftServiceExecutor thriftServiceExecutor =
                new ThriftServiceExecutor(executorToRead, executorToWrite);
		ThriftServiceHandler thriftServiceHandler =
                new ThriftServiceWithMessageQueue(thriftServiceExecutor);
		thriftServer = new ThriftServer(thriftServiceHandler, serverProperties);
		thriftServer.start();
	}

	@AfterClass
	public static void teardown() {
		thriftServer.stop();
		MessageQueueMap.getInstance().remove(messageQueueId);
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

		ThriftRWService.Client clientService = client.getService();
		logger.info("hello client");

		// create the message queue
		try {
			boolean ret = clientService.writeId(messageQueueId);
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
			String ret = null;
			try {
				ret = clientService.writeString(messageQueueId, String.valueOf(i));
				tempList.add(String.valueOf(i));
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info(e.toString());
			}
			assertEquals("null",ret);
            assertEquals(i+1,mq.size());
		}
		logger.info("thrift write is done");

		assertEquals(count, mq.size());

		for(int i=0; i<count; i++) {
			String value = null;
			try {
				value = null;
				value = clientService.readString(messageQueueId);
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
			assertEquals(String.valueOf(i), value);
		}
		logger.info("thrift read is done");

        assertEquals(0, mq.size());

		client.stop();
        mq.clear();
	}

	@Test
	public void testSendReferentialMessage() {
		ThriftClient client = new ThriftClient(clientProperties);
		client.start();

		ThriftRWService.Client clientService = client.getService();
		logger.info("hello client");

		// create the message queue
		try {
			boolean ret = clientService.writeId(messageQueueId);
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

		MessageQueue mq = MessageQueueMap.getInstance().get(messageQueueId);

		String strTime = String.valueOf(System.currentTimeMillis()/1000L);
		for(int i=0; i<count; i++) {
			ThriftMessage childMsg = new ThriftMessage();
			childMsg.set_sender_id("child");
			childMsg.set_timestamp(strTime);
			childMsg.set_list_i32(new ArrayList<>());
			childMsg.get_list_i32().add(i);
			childMsg.set_payload("payload");
			List<ThriftMessage> messageList = new ArrayList<>();
			messageList.add(childMsg);
			ThriftMessage rootMsg = new ThriftMessage();
			rootMsg.set_sender_id(messageQueueId);
			rootMsg.set_timestamp(strTime);
			rootMsg.set_list_double(new ArrayList<>());
			rootMsg.set_list_message(messageList);

			int mqSize = mq.size();
			String result = null;
			long startingTime = System.currentTimeMillis();
			try {
				result = clientService.writeThriftMessage(rootMsg);
			} catch (InvalidOperationException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			}
			assertEquals("null",result);
			assertEquals(mqSize+1, mq.size());

			long endingTime = System.currentTimeMillis();
			long elapsedTime = endingTime - startingTime;
			System.out.format("elapsed Time : %d \n", elapsedTime);

			ThriftMessage message = null;
			ThriftMessage childMessage = null;
			int value = 0;
			String id = "";
			try {
				message = clientService.readThriftMessage(messageQueueId);
				childMessage = message.get_list_message().get(0);
				value = childMessage.get_list_i32().get(0);
				id = message.get_sender_id();
			} catch (TException e) {
				e.printStackTrace();
			}
			assertNotNull(childMessage);
			assertEquals(strTime, message.get_timestamp());
			assertEquals(strTime, childMessage.get_timestamp());
			assertEquals("payload",childMessage.get_payload());
			assertEquals(i, value);
			assertEquals(messageQueueId,id);
		}

		client.stop();

		assertEquals(0, mq.size());
		mq.clear();
	}

}
