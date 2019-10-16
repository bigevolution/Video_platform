package com.example.gb28181_videoplatform.netty.client;


/**
 * Created by 张俨 on 2017/10/9.
 */

public interface NettyListener {

    byte STATUS_CONNECT_SUCCESS = 0;

    byte STATUS_CONNECT_CLOSED = 1;

    byte STATUS_CONNECT_ERROR = 2;

    byte STATUS_SEND_HEART_BEAT = 3;

    /**
     * 当服务状态发生变化时触发
     */
    void onServiceStatusConnectChanged(int statusCode);
}
