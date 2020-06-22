package com.zfx.websocket;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {

	@RequestMapping("/websocket/get")
	public String webSocketSession(){
		return null;
	}
}
