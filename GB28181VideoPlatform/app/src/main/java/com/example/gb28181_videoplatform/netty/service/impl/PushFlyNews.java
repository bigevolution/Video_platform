package com.example.gb28181_videoplatform.netty.service.impl;

import com.example.gb28181_videoplatform.netty.service.AbstractNews;
import com.example.gb28181_videoplatform.netty.util.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by xk on 2018/12/10.
 * 飞投信息接收类
 */
public class PushFlyNews extends AbstractNews {
    Logger mLog= LoggerFactory.getLogger(PushFlyNews.class);

    @Override
    public Object run(ChannelHandlerContext ctx, Request request) throws Exception {
        final String msg = request.getMessage().toString();
        mLog.debug("收到PushFlyNews。消息内容为："+request.toString());
        mParent.mNotifier.notifyPushFly(msg);
        return null;
    }

    @Override
    public String init() {
        return null;
    }
}
