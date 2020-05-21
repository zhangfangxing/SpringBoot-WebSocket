package com.zfx.websocket;

import java.net.URI;
import java.net.URISyntaxException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 基于websocket的netty客户端
 *
 */
public class ClientByNetty {
	
	private static String url = "ws://localhost:8080/websocket/123213321";
	
//	@Value()//解决static赋值的问题
	public static void setUrl(String url) {
		ClientByNetty.url = url;
	}

	static Channel channel = null;
	static {
		//netty基本操作，线程组
        EventLoopGroup group = new NioEventLoopGroup();
        //netty基本操作，启动类
        Bootstrap boot = new Bootstrap();
        boot.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .group(group)
                .handler(new LoggingHandler(LogLevel.INFO))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("http-codec",new HttpClientCodec());
                        pipeline.addLast("aggregator",new HttpObjectAggregator(1024*1024*10));
                        pipeline.addLast("hookedHandler", new WebSocketClientHandler());
                    }
                });
        //websocke连接的地址，/hello是因为在服务端的websockethandler设置的
        URI websocketURI = null;
		try {
//			websocketURI = new URI("ws://localhost:8080/websocket/123213321");
			websocketURI = new URI(url);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        //进行握手
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String) null, true, httpHeaders);
       //客户端与服务端连接的通道，final修饰表示只会有一个
        try {
			channel = boot.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("hookedHandler");
        handler.setHandshaker(handshaker);
        handshaker.handshake(channel);
        //阻塞等待是否握手成功
        try {
			handler.handshakeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        System.out.println("握手成功");
	}

//	public static void connWebSocket(){
//		//netty基本操作，线程组
//        EventLoopGroup group = new NioEventLoopGroup();
//        //netty基本操作，启动类
//        Bootstrap boot = new Bootstrap();
//        boot.option(ChannelOption.SO_KEEPALIVE, true)
//                .option(ChannelOption.TCP_NODELAY, true)
//                .group(group)
//                .handler(new LoggingHandler(LogLevel.INFO))
//                .channel(NioSocketChannel.class)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    protected void initChannel(SocketChannel socketChannel) throws Exception {
//                        ChannelPipeline pipeline = socketChannel.pipeline();
//                        pipeline.addLast("http-codec",new HttpClientCodec());
//                        pipeline.addLast("aggregator",new HttpObjectAggregator(1024*1024*10));
//                        pipeline.addLast("hookedHandler", new WebSocketClientHandler());
//                    }
//                });
//        //websocke连接的地址，/hello是因为在服务端的websockethandler设置的
//        URI websocketURI = null;
//		try {
////			websocketURI = new URI("ws://localhost:8080/websocket/123213321");
//			websocketURI = new URI(url);
//		} catch (URISyntaxException e1) {
//			e1.printStackTrace();
//		}
//        HttpHeaders httpHeaders = new DefaultHttpHeaders();
//        //进行握手
//        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String) null, true, httpHeaders);
//       //客户端与服务端连接的通道，final修饰表示只会有一个
//        try {
//			channel = boot.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
//        WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("hookedHandler");
//        handler.setHandshaker(handshaker);
//        handshaker.handshake(channel);
//        //阻塞等待是否握手成功
//        try {
//			handler.handshakeFuture().sync();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//        System.out.println("握手成功");
//	
//	}
	
    public static void sengMessage(final String message){
        //发送的内容，是一个文本格式的内容
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        channel.writeAndFlush(frame).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.out.println("消息发送成功，发送的消息是："+message);
                } else {
                    System.out.println("消息发送失败 " + channelFuture.cause().getMessage());
                }
            }
        });
    }
}