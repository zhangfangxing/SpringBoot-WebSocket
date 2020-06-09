package com.zfx.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
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
 */
public class ClientByNetty {
	
	private static String url = "ws://localhost:50200";
//	private static String url = "ws://localhost:20201";
	
//	@Value()//解决static赋值的问题
	public static void setUrl(String url) {
		ClientByNetty.url = url;
	}

	static Channel channel = null;
	public static void connectWebSocket(){
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
//                        pipeline.addLast("ping", new IdleStateHandler(60, 20, 60 * 10, TimeUnit.SECONDS));
                        //IdleStateHandler第一个参数 60 表示读操作空闲时间/第二个参数 20 表示写操作空闲时间/第三个参数 60*10 表示读写操作空闲时间/第四个参数 单位/秒
//                        pipeline.addLast(new ClientPoHandlerProto());
                    }
                });
        URI websocketURI = null;
		try {
			websocketURI = new URI(url);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        //进行握手
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String) null, true, httpHeaders);
       //客户端与服务端连接的通道，final修饰表示只会有一个
        try {
        	ChannelFuture connect = boot.connect(websocketURI.getHost(), websocketURI.getPort());
        	channel = connect.addListener(new ConnectionListener()).sync().channel();
//			channel = boot.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
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

    public static void sengMessage(final String message){
        //发送的内容，是一个文本格式的内容
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        channel.writeAndFlush(frame).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.out.println("消息发送成功，发送的消息是："+message);
                } else {
                    System.out.println("消息发送失败 " + channelFuture.cause().getMessage());
                	final EventLoop loop = channelFuture.channel().eventLoop();
                    loop.schedule(new Runnable() {
                        @Override
                        public void run() {
                            System.err.println("发送消息时服务端链接不上，开始重连操作...");
                            ClientByNetty.connectWebSocket();
                        }
                    }, 1L, TimeUnit.SECONDS);
                }
            }
        });
    }
}