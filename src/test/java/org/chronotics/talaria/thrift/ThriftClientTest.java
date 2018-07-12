package org.chronotics.talaria.thrift;

import org.apache.thrift.TException;
import org.chronotics.talaria.common.TaskExecutor;
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

	@BeforeClass
	public static void setup() {
		ThriftServerProperties serverProperties =
                new ThriftServerProperties();
		serverProperties.setIp("localhost");
		serverProperties.setPort("9091");
		serverProperties.setServerType("simple");
		TaskExecutor<Object> executorToWrite =
                new SimplePrintExecutor<Object>(
                        TaskExecutor.PROPAGATION_RULE.STEP_BY_STEP_ORIGINAL_ARG,
                        null);
        ThriftServiceExecutor thriftServiceExecutor =
                new ThriftServiceExecutor(null, executorToWrite);
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
			boolean ret = service.writeId("thrift");
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

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<String> tempList =
				new ArrayList<String>();

		int count = 100;
		for(int i=0; i<count; i++) {
			try {
				String ret =
                        service.writeString("thrift", String.valueOf(i));
				tempList.add(String.valueOf(i));
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info(e.toString());
			}
		}

		logger.info("thrift write is done");

		for(int i=0; i<count; i++) {
			try {
				String value = null;
				value = service.readString("thrift");
				if(value != null) {
					tempList.remove(value);
				} else {
					break;
				}
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info(e.toString());
			}
		}

		logger.info("thrift read is done");
		logger.info(String.valueOf(tempList.size()));
		assertEquals(0,tempList.size());
	}
}
