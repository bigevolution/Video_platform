package com.example.gb28181_videoplatform.netty.client;

import com.example.gb28181_videoplatform.netty.querier.Querier;
import com.example.gb28181_videoplatform.netty.util.CODE;
import com.example.gb28181_videoplatform.netty.util.Request;
import com.example.gb28181_videoplatform.netty.util.SerializationUtil;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private Logger mLog= LoggerFactory.getLogger(WebSocketClientHandler.class);
    private static final int READ_TIME_OUT_COUNT=2;

    private NettyListener listener;
    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private int readTimeOutCount;

    private Gson gson = new Gson();

    public WebSocketClientHandler(WebSocketClientHandshaker webSocketClientHandshaker, NettyListener listener) {
        this.listener = listener;
        handshaker=webSocketClientHandshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public NettyListener getListener() {
        return listener;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        mLog.error("WebSocket Client disconnected!，ctx info: isActive="+ctx.channel().isActive()+",to string"+ctx.toString());
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
    }
 
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            mLog.error("WebSocket Client connected!"+ctx.toString());
            handshakeFuture.setSuccess();
            readTimeOutCount=0;
            listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
            return;
        }
 
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.getStatus() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
 
        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;

            String req =((TextWebSocketFrame) frame).text();
            Request request = gson.fromJson(req,Request.class);
            //把消息推送给注册上来的实现类
            Querier.getInsatance().execute(ctx,request);
            mLog.debug("WebSocket Client received message: " + textFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
            mLog.debug("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            mLog.debug("WebSocket Client received closing");
            ch.close();
        } else if (frame instanceof BinaryWebSocketFrame) {
			ByteBuf buf = frame.content();
			byte[] req = new byte[buf.readableBytes()];
			buf.readBytes(req);
			Request request= SerializationUtil.deserialize(req, Request.class);
            if(request.getType().equals(CODE.PACKETYPE__REPORT_HEART_BEAT.code)) {
                //心跳消息只需要打印日记即可
                readTimeOutCount=0;
                mLog.debug("收到服务端心跳，会话ID为:"+request.getSessionId());
                return;
            }
			//把消息推送给注册上来的实现类
			Querier.getInsatance().execute(ctx,request);
            request.setType(CODE.PUSHSUCCESS.code);
            send(ctx, request);
		}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        mLog.error("ctx="+ctx.toString()+",cause="+cause.getMessage());
        ctx.close();
    }

    public void send(ChannelHandlerContext ctx, Request request) throws Exception {
        if (ctx == null || ctx.isRemoved()) {
            throw new Exception("尚未握手成功，无法向客户端发送WebSocket消息");
        }
        ctx.channel()
                .writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(SerializationUtil.serialize(request))));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        mLog.debug("循环触发时间：" + new Date());
        // 心跳检查
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                //可以选择重新连接
                readTimeOutCount++;
                if(readTimeOutCount>READ_TIME_OUT_COUNT){
                    readTimeOutCount=0;
                    mLog.debug("长期没收到服务器推送数据 userEventTriggered time to use on disconnect");
                    listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
                }
                mLog.debug("长期没收到服务器推送数据,发送心跳");
                listener.onServiceStatusConnectChanged(NettyListener.STATUS_SEND_HEART_BEAT);
            }else if(event.state()==IdleState.WRITER_IDLE){
                mLog.debug("长期间未发送数据,发送心跳");
                listener.onServiceStatusConnectChanged(NettyListener.STATUS_SEND_HEART_BEAT);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        //listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_ERROR);
        mLog.error("network disconnect");
    }
}
