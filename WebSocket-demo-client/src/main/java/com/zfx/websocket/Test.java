package com.zfx.websocket;

public class Test {
	
    public static void main(String[] args){
    	
//    	ClientByNetty.connWebSocket();
        //给服务端发送的内容，如果客户端与服务端连接成功后，可以多次掉用这个方法发送消息
    	for (int i = 0; i < 20; i++) {
    		ClientByNetty.sengMessage("aaaaaaaaaaa"+i);
		}
    	ClientByNetty.sengMessage("aaaaaaaaaaa");
    }
}
