package org.chronotics.talaria.thrift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.thriftservice.ThriftServiceWithMessageQueue;
import org.chronotics.talaria.thrift.gen.ThriftMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ThriftServerProperties.class})
public class MessageTransferThroughThriftServer {

	private static ThriftServer thriftServer = null;
	
	private static List<String> keyList = null;
	private static int keySize = 100;
	private static int insertionSize = 50;
	private static String message = "{\n" + 
			"  \"timestamp\" : 1528804317,\n" + 
			"  \"sender_id\": \"sender\",\n" + 
			"  \"receiver_id\": \"receiver\",\n" + 
			"  \"command\": \"execute training\"\n" + 
			"}\n" + 
			"";
	private long delay = 1000;
	
	@BeforeClass
	public static void setup() {
		keyList = new ArrayList<String>();
		for(int i=0; i<keySize; i++) {
			keyList.add("id"+String.valueOf(i));
		}
		
		ThriftServerProperties thriftServerProperties = new ThriftServerProperties();
		thriftServerProperties.setIp("localhost");
		thriftServerProperties.setPort("9091");
		thriftServerProperties.setServerType("simple");
		thriftServerProperties.setSecureServer("false");
		
		System.out.println(thriftServerProperties.toString());

		ThriftServiceHandler thriftServiceHandler = new ThriftServiceWithMessageQueue(null);
		thriftServer = new ThriftServer(thriftServiceHandler, thriftServerProperties);
		thriftServer.start();
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
			assertEquals(0 ,mqMap.get(keyList.get(i)).size());
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
			MessageQueue<ThriftMessage> mq =
					(MessageQueue<ThriftMessage>)
					mqMap.get(keyList.get(i));
			if(mq==null) {
				mq = new MessageQueue<ThriftMessage>(
						ThriftMessage.class,
							MessageQueue.default_maxQueueSize,
							MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
				mqMap.put(keyList.get(i), mq);
			}
		}
		
		int count = 0;
		while(true) {
			if(count >= insertionSize) {
				break;
			}
			for(int i=0; i<keySize; i++) {
				MessageQueue<ThriftMessage> mq =
						(MessageQueue<ThriftMessage>)
						mqMap.get(keyList.get(i));
				ThriftMessage message = new ThriftMessage();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String _payload = timestamp.toString();
				message.set_timestamp(timestamp.toString());
				message.set_sender_id(String.valueOf(i));
				message.set_payload(_payload);
				
				mq.addLast(message);
			}
			count++;
		}
		
		for(int i=0; i<keySize; i++) {
			MessageQueue<String> mq = 
					(MessageQueue<String>) 
					mqMap.get(keyList.get(i));
			assertEquals(insertionSize, mq.size());
		}
	}
	
	@Test
	public void checkMessage() {
		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		for(int i=0; i<keySize; i++) {
			MessageQueue<String> mq =
					(MessageQueue<String>)
					mqMap.get(keyList.get(i));
			if(mq==null) {
				mq = new MessageQueue<String>(
							String.class,
							MessageQueue.default_maxQueueSize,
							MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
				mqMap.put(keyList.get(i), mq);
			} else {
				mq.clear();
			}
		}
		Map<Integer, String> tempMap = new HashMap<Integer, String>();
		for(int i=0; i<keySize; i++) {
			MessageQueue<String> mq =
					(MessageQueue<String>)
					mqMap.get(keyList.get(i));
			String message = (String.valueOf(i)
							+ ", "
							+ new Timestamp(System.currentTimeMillis()).toString());
			mq.addLast(message);
			tempMap.put(i, message);
		}

		for(int i=0; i<keySize; i++) {
			MessageQueue<String> mq =
					(MessageQueue<String>)
					mqMap.get(keyList.get(i));
			assertEquals(1, mq.size());
			String message = mq.getFirst();
			assertEquals(tempMap.get(i), message);
		}
	}
}
