package com.example.gb28181_videoplatform.sip.impl;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.example.gb28181_videoplatform.sip.IDevice;
import com.example.gb28181_videoplatform.sip.NotInitializedException;

import java.io.Serializable;
import java.util.HashMap;

public class DeviceImpl implements IDevice, Serializable {
    private static DeviceImpl device;
    private Context context;
    private MySipManager sipManager;
    private SipProfile sipProfile;
    private Handler deviceHandler,timeHandler;
    private int sn = 0;
    private int con = 0;
    private int sn1 = 0;

    public SipProfile getSipProfile() {
        return sipProfile;
    }

    public void setSipProfile(SipProfile sipProfile) {
        this.sipProfile = sipProfile;
    }

    private DeviceImpl() {

    }

    public static DeviceImpl getInstance() {
        if (device == null) {
            device = new DeviceImpl();
        }
        return device;
    }

    public void Initialize(Context context, SipProfile sipProfile, HashMap<String, String> customHeaders) {
        this.Initialize(context, sipProfile);
        sipManager.setCustomHeaders(customHeaders);
    }

    public void Initialize(Context context, SipProfile sipProfile) {
        this.context = context;
        this.sipProfile = sipProfile;
        sipManager = new MySipManager(sipProfile);

    }

    public Handler getDeviceHandler() {
        return deviceHandler;
    }

    public void setDeviceHandler(Handler deviceHandler) {
        this.deviceHandler = deviceHandler;
        sipManager.setmUpdateHandler(this.deviceHandler);
    }


    public Handler getTimeHandler() {
        return timeHandler;
    }

    public void setTimeHandler(Handler timeHandler) {
        this.timeHandler = timeHandler;
        sipManager.setTimeHandler(this.timeHandler);
    }
    @Override
    public void Register(int state) {
        this.sipManager.Register(state);
    }



    @Override
    public void Heart() {
        try {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\"?>");
            xml.append("\n");
            xml.append("<Notify>");
            xml.append("\n");
            xml.append("<CmdType>Keepalive</CmdType>");
            xml.append("\n");
            xml.append("<SN>" + sn + "" + "</SN>");
            xml.append("\n");
            xml.append("<DeviceID>" + sipProfile.getMySipNum() + "</DeviceID>");
            xml.append("\n");
            xml.append("<Status>OK</Status>");
            xml.append("\n");
            xml.append("</Notify>");
            this.sipManager.SendHeart("", sipProfile.getRemoteEndpoint(), xml.toString());
            sn++;
        } catch (NotInitializedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Call(String to) {

    }

    @Override
    public void Accept() {

    }

    @Override
    public void Reject() {

    }

    @Override
    public void Cancel() {

    }

    @Override
    public void Hangup() {

    }

    @Override
    public void invite(String to, int port, String nvrSipId, String ipcSipId, boolean isNvr) {
        try {
            this.sipManager.invite(to, port, nvrSipId, ipcSipId, isNvr);
        } catch (NotInitializedException e) {
            e.printStackTrace();
        }
    }

    public void backInvite(String to, int port, String nvrSipId, String ipcSipId,String startTime,String endTime) {
        try {
            this.sipManager.backInvite(to, port, nvrSipId, ipcSipId,startTime,endTime);
        } catch (NotInitializedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Bye() {
        try {
            this.sipManager.SendBye();
        } catch (NotInitializedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendMessage(String targetName, String deviceId, String to, String message) {
        try {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\"?>");
            xml.append("\n");
            xml.append("<Control>");
            xml.append("\n");
            xml.append("<CmdType>DeviceControl</CmdType>");
            xml.append("\n");
            xml.append("<SN>" + con + "" + "</SN>");
            xml.append("\n");
            xml.append("<DeviceID>" + targetName + "</DeviceID>");
            xml.append("\n");
            xml.append("<PTZCmd>" + message + "</PTZCmd>");
            xml.append("\n");
            xml.append("</Control>");
            this.sipManager.SendMessage(targetName, deviceId, to, xml.toString());
            con++;
        } catch (NotInitializedException e) {
            e.printStackTrace();
        }
    }
    /*
    * deviceId--设备ID
    * nvrId--nvrID
    * to--服务器Ip
    * message--消息
    * */
    public void SendTimeMsg(String deviceId, String nvrId, String to, String sdate,String edate) {
        try {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\"?>");
            xml.append("\n");
            xml.append("<Query>");
            xml.append("\n");
            xml.append("<CmdType>RecordInfo</CmdType>");
            xml.append("\n");
            xml.append("<SN>" + sn1 + "</SN>");
            xml.append("\n");
            xml.append("<DeviceID>" + deviceId + "</DeviceID>");
            xml.append("\n");
            xml.append("<StartTime>" + sdate + "</StartTime>");
            xml.append("\n");
            xml.append("<EndTime>" + edate + "</EndTime>");
            xml.append("\n");
            xml.append("<FilePath></FilePath>");
            xml.append("\n");
            xml.append("<Address>" + DeviceImpl.getInstance().getSipProfile().getLocalIp() + "</Address>");
            xml.append("\n");
            xml.append("<Secrecy>0</Secrecy>");
            xml.append("\n");
            xml.append("<Type>time</Type>");
            xml.append("\n");
            xml.append("<RecorderID>" + SPUtils.getInstance().getString("mySipName") + "</RecorderID>");
            xml.append("\n");
            xml.append("</Query>");
            this.sipManager.SendTimeMsg(deviceId, nvrId, to, xml.toString());
            Log.e("SendBackListRequest-gpc", "SendTimeMsg: "+xml.toString() );
            sn1++;
        } catch (NotInitializedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendDTMF(String digit) {

    }

    @Override
    public void Mute(boolean muted) {

    }

    public void Destroy() {
        this.sipManager.Destroy();
    }
}
