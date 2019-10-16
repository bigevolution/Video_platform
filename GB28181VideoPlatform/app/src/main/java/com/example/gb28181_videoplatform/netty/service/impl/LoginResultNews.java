package com.example.gb28181_videoplatform.netty.service.impl;

import android.util.Log;
import android.widget.Toast;

import com.example.gb28181_videoplatform.app.Global;
import com.example.gb28181_videoplatform.netty.util.DeviceInfo;
import com.example.gb28181_videoplatform.netty.util.PoliceErrCode;
import com.example.gb28181_videoplatform.netty.util.PoliceOfficerInfo;
import com.example.gb28181_videoplatform.netty.service.AbstractNews;
import com.example.gb28181_videoplatform.netty.util.CODE;
import com.example.gb28181_videoplatform.netty.util.Request;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Administrator on 2018/7/24.
 */

public class LoginResultNews extends AbstractNews {
    Logger mLog= LoggerFactory.getLogger(LoginResultNews.class);
    @Override
    public Object run(ChannelHandlerContext ctx, Request request) throws Exception {
        mLog.debug(request.getMessage().toString());
        JsonElement jelement = new JsonParser().parse(request.getMessage().toString());
        JsonObject jobject = jelement.getAsJsonObject();
        boolean isPolice = request.getMessage().toString().contains("policeRef");
        processLoginResult(jobject, isPolice);
        return null;
    }
    private void processLoginResult(JsonObject obj, boolean isPolice){
        //result:登录结果，200表示成功，其它见错误码定义
        //msg：错误信息
        int result = obj.get("result").getAsInt();
        String msg = obj.get("msg").getAsString();
        if(result== PoliceErrCode.SUCCESS){
            mLog.debug("登陆成功");
            Log.d("ss","登陆成功");
            PoliceOfficerInfo info = new PoliceOfficerInfo();
            info.setSessionId(Global.getInstance().getDeviceId());
            info.setPoliceId(Global.getInstance().getDeviceId());
            info.setPoliceName("");
            info.setAvatarUri("");
            mParent.mNotifier.notifyLoginResultChanged(true, true, info);

        }else if(result== PoliceErrCode.DEVICE_DISABLED){
            mLog.error(msg);
            mParent.mWebSocketClient.mIsShowToast=false;
            Toast.makeText(mParent.mContext, msg, Toast.LENGTH_SHORT).show();
        }else {
            mLog.debug("注册设备");
            register();
            PoliceOfficerInfo info = new PoliceOfficerInfo();
            info.setSessionId(mParent.getDeviceInfo().getEsn());
            info.setPoliceId(mParent.getDeviceInfo().getEsn());
            info.setPoliceName("");
            info.setAvatarUri("");
            mParent.mNotifier.notifyLoginResultChanged(true, true, info);
        }
    }

    //注册设备信息
    private void register() {
        JsonObject obj=new JsonObject();
        DeviceInfo deviceInfo = mParent.getDeviceInfo();
        obj.addProperty("devicetype",deviceInfo.getDeviceType());
        obj.addProperty("clientversion",deviceInfo.getClientVersion());
        obj.addProperty("osversion",deviceInfo.getOsVersion());
        obj.addProperty("network",deviceInfo.getNetwork());
        obj.addProperty("imsi",deviceInfo.getImsi());
        obj.addProperty("platformid",deviceInfo.getPlatformId());
        obj.addProperty("esn",deviceInfo.getEsn());
        obj.addProperty("request_locale", PoliceErrCode.locale);
        JsonObject location = new JsonObject();
        location.addProperty("latitude", "31.9903000000");
        location.addProperty("longitude", "118.7377400000");
        obj.add("location",location);
        Request request=new Request();
        request.setType(CODE.PACKETYPE__REGISTERED.code);
        request.setSessionId(mParent.getDeviceInfo().getEsn());
        request.setMessage(obj.toString());
        mParent.sendWebSocketRequest(request,null);
    }

    @Override
    public String init() {
        return null;
    }
}
