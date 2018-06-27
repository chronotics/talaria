package org.chronotics.talaria.websocket.springstompserver;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {
 
	@MessageMapping("/execute")
	@SendTo("/topic/reply")
	public String reply(String message) throws Exception {
		Thread.sleep(100); // simulated delay
		return new String("Hi" + message);
	}
}
