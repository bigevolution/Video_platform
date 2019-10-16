package com.example.gb28181_videoplatform.sip;

import java.io.Serializable;

public class RtpAddressMsg implements Serializable {
    private String ip;
    private int port;
    private String ssrc;

    public RtpAddressMsg() {
    }

    public RtpAddressMsg(String ip, int port, String ssrc) {
        this.ip = ip;
        this.port = port;
        this.ssrc = ssrc;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSsrc() {
        return ssrc;
    }

    public void setSsrc(String ssrc) {
        this.ssrc = ssrc;
    }
}
