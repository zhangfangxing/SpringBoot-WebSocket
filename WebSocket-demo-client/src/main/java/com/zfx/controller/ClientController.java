package com.zfx.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.tags.Param;

import com.zfx.websocket.ClientByNetty;

@RestController
public class ClientController {
	
	static {
		ClientByNetty.connectWebSocket();
	}
	
	//http://localhost:8080/clientCmd/{"cmd":"13","hbbyte":"-127"}
	
	@RequestMapping(value = "/clientCmd",method = RequestMethod.POST)
	public void myCmd(@RequestBody String cmd){
		ClientByNetty.sengMessage(cmd);
	}
}
