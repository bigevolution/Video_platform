package com.example.gb28181_videoplatform.netty.util;

/**
 * @描述: 设备信息
 * @包名: com.fiberhome.police.powerpolice.service.dataobj
 * @类名: DeviceInfo
 * @日期: 2017/4/11
 * @版权: Copyright ® 烽火星空. All right reserved.
 * @作者: fsg
 */
public class DeviceInfo {
    private String imsi;
    private String esn;
    private String osVersion;
    private String platformId;
    private String clientVersion;
    private String screenWidth;
    private String screenHeight;
    private String dpi;
    private String deviceType;
    private String network;
    private String mac;

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getEsn() {
        return esn;
    }

    public void setEsn(String esn) {
        this.esn = esn;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(String screenWidth) {
        this.screenWidth = screenWidth;
    }

    public String getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(String screenHeight) {
        this.screenHeight = screenHeight;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "imsi='" + imsi + '\'' +
                ", esn='" + esn + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", platformId='" + platformId + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", screenWidth='" + screenWidth + '\'' +
                ", screenHeight='" + screenHeight + '\'' +
                ", dpi='" + dpi + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", network='" + network + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
