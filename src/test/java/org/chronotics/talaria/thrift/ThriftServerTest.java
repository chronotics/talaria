package org.chronotics.talaria.thrift;

import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.taskexecutor.BypathExecutor;
import org.chronotics.talaria.common.taskexecutor.EmptyExecutor;
import org.chronotics.talaria.common.thriftservice.ThriftServiceWithMessageQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ThriftServerProperties.class})
public class ThriftServerTest {
	@Autowired
	private ThriftServerProperties properties;
	
	@Test 
	public void getSeverProperties() {
		System.out.println(properties.getIp());
		assertEquals("localhost", properties.getIp());
		assertEquals("9091", properties.getPort());
	}
	
	@Test
	public void getSecureProperties() {
		assertEquals("9092", properties.getSecurePort());
		assertEquals("~/.keystore", properties.getSecureKeyStore());
		assertEquals("thrift", properties.getSecureKeyPass());
	}

	@Test
	public void startStopThriftServer() {
		TaskExecutor<Object> executorToWrite = new BypathExecutor<>();
		TaskExecutor<Object> executorToRead = new EmptyExecutor<>();
		ThriftServiceExecutor thriftServiceExecutor = new ThriftServiceExecutor(executorToRead,executorToWrite);
		ThriftService thriftServiceHandler = new ThriftServiceWithMessageQueue(thriftServiceExecutor);
		ThriftServer thriftServer =
				new ThriftServer(
						thriftServiceHandler, properties);
		thriftServer.start();
		assertEquals(true,thriftServer.isRunning());

		thriftServer.stop();
		assertEquals(false,thriftServer.isRunning());

		thriftServer.start();
		assertEquals(true,thriftServer.isRunning());

		thriftServer.stop();
		assertEquals(false,thriftServer.isRunning());
	}
}
