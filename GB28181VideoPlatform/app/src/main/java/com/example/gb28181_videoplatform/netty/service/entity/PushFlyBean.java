package com.example.gb28181_videoplatform.netty.service.entity;

/**
 * Created by 吴迪 on 2019/8/22.
 */
public class PushFlyBean {

    /**
     * name : IPCamera 06
     * address : 武汉市
     * deviceId : 34020000001320000083
     * parentId : 34020000001180000002
     */

    private String name;
    private String address;
    private String deviceId;
    private String parentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
