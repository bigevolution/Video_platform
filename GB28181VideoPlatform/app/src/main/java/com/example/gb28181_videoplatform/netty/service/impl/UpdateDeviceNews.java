package com.example.gb28181_videoplatform.netty.service.impl;

import com.example.gb28181_videoplatform.netty.service.AbstractNews;
import com.example.gb28181_videoplatform.netty.util.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by 吴迪 on 2019/8/22.
 */

public class UpdateDeviceNews extends AbstractNews {
    Logger mLog= LoggerFactory.getLogger(UpdateDeviceNews.class);

    @Override
    public Object run(ChannelHandlerContext ctx, Request request) throws Exception {
        final String msg = request.getMessage().toString();
        mLog.debug("收到UpdateDeviceNews。消息内容为："+request.toString());
        mParent.mNotifier.notifyUpdateDevice(msg);
        return null;
    }

    @Override
    public String init() {
        return null;
    }
}
