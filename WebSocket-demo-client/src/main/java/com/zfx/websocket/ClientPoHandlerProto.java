package com.zfx.websocket;

import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 运行时，断开重连。//暂时废弃
 */
public class ClientPoHandlerProto extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	try {
    		JSONObject json = (JSONObject)msg;
    		System.out.println("返回结果为:" + json);
		} catch (Exception e) {
			System.out.println("非JSON对象。");
		}
    }
 
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                System.out.println("长期没收到服务器推送数据");
                //可以选择重新连接
                ClientByNetty.connectWebSocket();
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                System.out.println("长期未向服务器发送数据");
                //发送心跳包
                JSONObject json = new JSONObject();
                json.put("cmd", "13");
                json.put("hbbyte", "1");
                //{"cmd":"13","hbbyte":"1"}
                ctx.writeAndFlush(json);
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                System.out.println("ALL");
            }
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("掉线了...");
        //使用过程中断线重连
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
            	ClientByNetty.connectWebSocket();
            }
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

}
