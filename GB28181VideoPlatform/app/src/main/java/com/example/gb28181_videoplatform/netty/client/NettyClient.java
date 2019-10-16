package com.example.gb28181_videoplatform.netty.client;


import android.util.Log;

import com.example.gb28181_videoplatform.app.Global;
import com.example.gb28181_videoplatform.netty.querier.Querier;
import com.example.gb28181_videoplatform.netty.service.impl.LoginResultNews;
import com.example.gb28181_videoplatform.netty.service.impl.PushFlyNews;
import com.example.gb28181_videoplatform.netty.service.impl.UpdateDeviceNews;
import com.example.gb28181_videoplatform.netty.util.CODE;
import com.example.gb28181_videoplatform.netty.util.MD5Util;
import com.example.gb28181_videoplatform.netty.util.PoliceErrCode;
import com.example.gb28181_videoplatform.netty.util.PoliceService;
import com.example.gb28181_videoplatform.netty.util.Request;
import com.example.gb28181_videoplatform.netty.util.SerializationUtil;
import com.example.gb28181_videoplatform.sip.impl.DeviceImpl;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * Created by fsg on 2018/08/19.
 *
 */
public class NettyClient implements NettyListener {

    Logger mLog = LoggerFactory.getLogger(NettyClient.class);

    PoliceService mParent;

    private EventLoopGroup group;

    private NettyListener listener;

    private Channel channel;

    private boolean isConnect = false;
    private boolean isReConnect;

    private int reconnectNum = 100;

    private long reconnectIntervalTime = 6000;
    private Runnable mReconnectRunable;
    public final static String TAG = NettyClient.class.getName();

    private Bootstrap bootstrap;
    public boolean mIsShowToast;
    public int mErrorConnectCount=0;

    public NettyClient(PoliceService parent) {
        mParent = parent;
        init();
    }

    public synchronized void startWork() {
        if (!getConnectStatus()) {
            mParent.runOnBackgroundDelay(new Runnable() {
                @Override
                public void run() {
                    connect();//连接服务器
                }
            }, 500);
        }
    }

    public void init() {
        // 注册消息处理类
        Querier.getInsatance().attach(CODE.PACKETYPE_LoginResult.code, new LoginResultNews());
        Querier.getInsatance().attach(CODE.PACKETYPE__PUSH_FLY.code, new PushFlyNews());
        Querier.getInsatance().attach(CODE.PACKETYPE__UPDATE_DEVICE.code, new UpdateDeviceNews());
        setListener(this);
        mReconnectRunable=new Runnable() {
            @Override
            public void run() {
                disconnect();
                connect();
            }
        };
    }

    public synchronized NettyClient connect() {

        if (!isConnect) {
            group = new NioEventLoopGroup();
            try {
                mLog.info("connect to " + NettyClientInitializer.WEB_SOCKET_URL);
                URI uri = new URI(NettyClientInitializer.WEB_SOCKET_URL);
                String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
                String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
                int port;
                if (uri.getPort() == -1) {
                    if ("ws".equalsIgnoreCase(scheme)) {
                        port = 80;
                    } else if ("wss".equalsIgnoreCase(scheme)) {
                        port = 443;
                    } else {
                        port = -1;
                    }
                } else {
                    port = uri.getPort();
                }
                if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                    System.err.println("Only WS(S) is supported.");
                    return null;
                }
                final boolean ssl = "wss".equalsIgnoreCase(scheme);
                final SslContext sslCtx;
                if (ssl) {
                    sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                } else {
                    sslCtx = null;
                }

                bootstrap = new Bootstrap().group(group)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                        .channel(NioSocketChannel.class)
                        .handler(new NettyClientInitializer(sslCtx, listener));

                ChannelFuture future = bootstrap.connect(host, port).sync();
                if (future != null && future.isSuccess()) {
                    channel = future.channel();
                    isConnect = true;
                } else {
                    isConnect = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
            }
        }
        return this;
    }

    //登陆接口
    public void login() {
        if(!getConnectStatus()){
            return;
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("request_locale", PoliceErrCode.locale);
        obj.addProperty("gbId", DeviceImpl.getInstance().getSipProfile().getSipUserName());
        JsonObject location = new JsonObject();
        location.addProperty("latitude", "31.9903000000");
        location.addProperty("longitude", "118.7377400000");
        obj.add("location", location);
        Request request = new Request();
        request.setType(CODE.PACKETYPE_LoginResult.code);
        request.setSessionId(Global.getInstance().getDeviceId());
        request.setMessage(obj.toString());
        request.setToken(MD5Util.doSign(request.toString(), "Fh,", "UTF-8"));
        mLog.debug(request.toString());
        Log.e(TAG, "connect to " + request.toString());
        mParent.sendWebSocketRequest(request, null);
    }

    public void onDestory() {
        setReconnectNum(0);
        disconnect();
    }

    public void disconnect() {
        if(null != group){
            group.shutdownGracefully();
        }
    }

    public synchronized void reconnect() {
        mLog.debug("terible! try to reconnect,isConnect="+isConnect);
        if (reconnectNum > 0 && !isConnect) {
            isReConnect = true;
            mLog.debug("try to reconnect");
            if(mIsShowToast){
                mParent.runOnUI(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }else {
                mIsShowToast=true;
            }
            reconnectNum--;
            mParent.runOnBackgroundDelay(mReconnectRunable,reconnectIntervalTime);
        }
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * 发送消息
     *
     * @param request
     * @param futureListener
     */
    public void sendMessage(final Request request, FutureListener futureListener) {
        boolean flag = channel != null && isConnect;
        if (!flag) {
            mLog.error("------尚未连接,msg="+request.toString());
            return;
        }
        WebSocketFrame frame = new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(SerializationUtil.serialize(request)));
        if (futureListener == null) {
            channel.writeAndFlush(frame).addListener(new FutureListener() {

                @Override
                public void success() {
                    mLog.debug("发送成功--->" + CODE.getNote(request.getType()));
                }

                @Override
                public void error() {
                    if(mErrorConnectCount>5){
                        mErrorConnectCount=0;
                        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
                    }
                    mErrorConnectCount++;
                    mLog.error("发送失败--->" + request.toString());
                }
            });
        } else {
            channel.writeAndFlush(frame).addListener(futureListener);
        }
    }

    /**
     * 设置重连次数
     *
     * @param reconnectNum 重连次数
     */
    public void setReconnectNum(int reconnectNum) {
        this.reconnectNum = reconnectNum;
    }

    /**
     * 设置重连时间间隔
     *
     * @param reconnectIntervalTime 时间间隔
     */
    public void setReconnectIntervalTime(long reconnectIntervalTime) {
        this.reconnectIntervalTime = reconnectIntervalTime;
    }

    public boolean getConnectStatus() {
        return isConnect;
    }

    /**
     * 设置连接状态
     *
     * @param status
     */
    public void setConnectStatus(boolean status) {
        this.isConnect = status;
    }

    public void setListener(NettyListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener == null ");
        }
        this.listener = listener;
    }

    @Override
    public void onServiceStatusConnectChanged(int statusCode) {
        mLog.debug("connect status change, code=" + statusCode);
        switch (statusCode) {
            case NettyListener.STATUS_CONNECT_SUCCESS:
                isConnect = true;
                mParent.mBackgroundHandler.removeCallbacks(mReconnectRunable);
                if (isReConnect) {
                    login();
                }
                mErrorConnectCount=0;
                break;
            case NettyListener.STATUS_CONNECT_CLOSED:
                isConnect = false;
                reconnect();
                break;
            case NettyListener.STATUS_CONNECT_ERROR:
                connect();
                break;
            case NettyListener.STATUS_SEND_HEART_BEAT:
                sendHeartBeat();
                break;
            default:
                break;
        }
    }

    public void sendHeartBeat() {
        Request req = new Request();
        req.setRequestId(Global.getInstance().getDeviceId());
        req.setType(CODE.PACKETYPE__REPORT_HEART_BEAT.code);
        sendMessage(req, null);
    }
}
