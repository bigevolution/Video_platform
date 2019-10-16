package com.example.gb28181_videoplatform.netty.client;


import com.blankj.utilcode.util.SPUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by fsg on 2018/07/23.
 */

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private static final int READ_IDEL_TIME_OUT = 30; // 读超时
    private static final int WRITE_IDEL_TIME_OUT = 25;// 写超时
    private static final int ALL_IDEL_TIME_OUT = 0; // 所有超时

    //public static final String WEB_SOCKET_URL="ws://47.98.142.126:9093/websocket";
//    public static final String WEB_SOCKET_URL="ws://219.151.20.11:9093/websocket";  //公网
    //public static final String WEB_SOCKET_URL="ws://47.97.126.243:9093/websocket";  //测试部
    public static final String WEB_SOCKET_URL ="ws://" + SPUtils.getInstance().getString("webIp") + ":" +
            SPUtils.getInstance().getString("socketPort") + "/websocket";  //测试部

    private URI uri;
    private NettyListener listener;
    private SslContext mSslContext;

    public NettyClientInitializer(SslContext sslContext,NettyListener listener) {
        if(listener == null){
            throw new IllegalArgumentException("listener == null");
        }
        mSslContext=sslContext;
        this.listener=listener;
        try {
            uri=new URI(WEB_SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        if(null!=mSslContext){
            pipeline.addLast(mSslContext.newHandler(ch.alloc()));    // 开启SSL
        }
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(8192));
        pipeline.addLast(new IdleStateHandler(READ_IDEL_TIME_OUT,WRITE_IDEL_TIME_OUT,ALL_IDEL_TIME_OUT, TimeUnit.SECONDS));
        pipeline.addLast(new WebSocketClientHandler(WebSocketClientHandshakerFactory
                .newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders(),65536*10),listener));
    }
}