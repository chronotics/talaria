package org.chronotics.talaria.thrift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.taskexecutor.ThriftServiceWithMessageQueue;
import org.chronotics.talaria.thrift.gen.Message;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ThriftServerProperties.class})
public class MessageTransferThroughThriftServer {
	@Autowired
	private ThriftServerProperties thriftServerProperties;
	
	public ThriftServerProperties getProperties() { return thriftServerProperties; }

//	private static ThriftService thriftServiceHandler = new ThriftServiceWithMessageQueue(null);
	private static ThriftServer thriftServer = new ThriftServer();
	
	private static List<String> keyList = null;
	private static int keySize = 100;
	private static String message = "{\n" + 
			"  \"timestamp\" : 1528804317,\n" + 
			"  \"sender_id\": \"sender\",\n" + 
			"  \"receiver_id\": \"receiver\",\n" + 
			"  \"command\": \"execute training\"\n" + 
			"}\n" + 
			"";
	private long delay = 5000;
	
	public synchronized void startServer() {
		
	}
	
	@BeforeClass
	public static void setup() {
		keyList = new ArrayList<String>();
		for(int i=0; i<keySize; i++) {
			keyList.add("id"+String.valueOf(i));
		}
		
//		MessageTransferThroughThriftServer temp = new MessageTransferThroughThriftServer();
		ThriftServerProperties thriftServerProperties = new ThriftServerProperties();
		thriftServerProperties.setIp("192.168.0.41");
		thriftServerProperties.setPort("9091");
		thriftServerProperties.setSecureServer("false");
		
		System.out.println(thriftServerProperties.toString());
////		if(thriftServer.isRunning()) {
////			return;
////		}
//		thriftServer.start(thriftServiceHandler,properties);
		
		ThriftService thriftServiceHandler = new ThriftServiceWithMessageQueue(null);
		thriftServer = new ThriftServer();
		thriftServer.start(thriftServiceHandler,thriftServerProperties);
		
	}
	
	@AfterClass
	public static void teardown() {
		thriftServer.stop();
	}
	
	@Test
	public void getQueueIdWithMessageQueueInsertion() {
		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		for(int i=0; i<keySize; i++) {
			// register message queue
			MessageQueue<String> mq = 
					new MessageQueue<String>(
							String.class,
							MessageQueue.default_maxQueueSize,
							MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
			boolean rt = mqMap.put(keyList.get(i), mq);
			assertTrue(rt==true);
		}
		for(int i=0; i<keySize; i++) {
			MessageQueue<String> mq = 
					(MessageQueue<String>) 
					mqMap.get(keyList.get(i));
			assertTrue(mq!=null);
		}
		
	}
	
	@Test
	public void getQueueIdWithoutMessageQueueInsertion() {
		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		for(int i=0; i<keySize; i++) {
			MessageQueue<String> mq = 
					(MessageQueue<String>) 
					mqMap.get(keyList.get(i));
			assertTrue(mq==null);
		}
	}
	
	@Test
	public void insertMessage() {
		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		for(int i=0; i<keySize; i++) {
			MessageQueue<Message> mq = 
					(MessageQueue<Message>) 
					mqMap.get(keyList.get(i));
			if(mq==null) {
				mq = new MessageQueue<Message>(
						Message.class,
							MessageQueue.default_maxQueueSize,
							MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
				mqMap.put(keyList.get(i), mq);
			}
		}
		
		int count = 0;
		while(true) {
			if(count >= 50) {
				break;
			}
			for(int i=0; i<keySize; i++) {
				MessageQueue<Message> mq = 
						(MessageQueue<Message>) 
						mqMap.get(keyList.get(i));
				Message message = new Message();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String _payload = timestamp.toString();
				message.set_timestamp(timestamp.toString());
				message.set_sender_id(String.valueOf(i));
				message.set_payload(_payload);
				
				mq.add(message);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
		}
		
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0; i<keySize; i++) {
			MessageQueue<String> mq = 
					(MessageQueue<String>) 
					mqMap.get(keyList.get(i));
			assertEquals(0, mq.size());
		}
	}
	
	@Test
	public void checkMessage() {
//		MessageQueueMap mqMap = MessageQueueMap.getInstance();
//		for(int i=0; i<keySize; i++) {
//			MessageQueue<String> mq = 
//					(MessageQueue<String>) 
//					mqMap.get(keyList.get(i));
//			if(mq==null) {
//				mq = new MessageQueue<String>(
//							String.class,
//							MessageQueue.default_maxQueueSize,
//							MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
//				mqMap.put(keyList.get(i), mq);
//			}
//		}
//		for(int i=0; i<keySize; i++) {
//			MessageQueue<String> mq = 
//					(MessageQueue<String>) 
//					mqMap.get(keyList.get(i));
//			mq.add(String.valueOf(i) 
//					+ ", " 
//					+ new Timestamp(System.currentTimeMillis()).toString());
//		}
//		
//		try {
//			Thread.sleep(delay);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		assertTrue(true);
//		
//		for(int i=0; i<keySize; i++) {
//			MessageQueue<String> mq = 
//					(MessageQueue<String>) 
//					mqMap.get(keyList.get(i));
//			assertEquals(0, mq.size());
//		}
	}
}
