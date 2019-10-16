package com.example.gb28181_videoplatform.netty.service;

import com.example.gb28181_videoplatform.netty.util.PoliceService;
import com.example.gb28181_videoplatform.netty.util.Request;
import com.example.gb28181_videoplatform.netty.util.SerializationUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public abstract class AbstractNews {
	public PoliceService mParent;

	public AbstractNews() {
		mParent = PoliceService.instance;
		init();
	}

	/**
	 * 接受到所有的消息，都会
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract Object run(ChannelHandlerContext ctx, Request request) throws Exception;

	/**
	 * 初始化
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract String init();
	/**
	 * 发送二进制消息
	 * @param ctx
	 * @param request
	 * @throws Exception
	 */
	public void send(ChannelHandlerContext ctx, Request request) throws Exception {
		if (ctx == null || ctx.isRemoved()) {
			throw new Exception("尚未握手成功，无法向客户端发送WebSocket消息");
		}
		ctx.channel()
				.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(SerializationUtil.serialize(request))));
	}
}
