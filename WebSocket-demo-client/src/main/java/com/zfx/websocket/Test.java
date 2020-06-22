package com.zfx.websocket;

public class Test {
	
    public static void main(String[] args){
    	
    	ClientByNetty.connectWebSocket();
        //给服务端发送的内容，如果客户端与服务端连接成功后，可以多次掉用这个方法发送消息
    	while (true) {
    		ClientByNetty.sengMessage("aaaaaaaaaaa");
    		try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//    	ClientByNetty.closeWebSocket();
    }
}
