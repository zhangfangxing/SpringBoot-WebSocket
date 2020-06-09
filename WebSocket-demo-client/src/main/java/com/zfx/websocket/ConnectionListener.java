package com.zfx.websocket;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

/**
 * 监听启动时连接失败，重新连接功能
 */
public class ConnectionListener implements ChannelFutureListener{

	@Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    System.err.println("启动时服务端链接不上，开始重连操作...");
                    ClientByNetty.connectWebSocket();
                }
            }, 1L, TimeUnit.SECONDS);
        } else {
            System.out.println("服务端链接成功...");
        }
    }
}