package com.example.gb28181_videoplatform.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 吴迪 on 2019/8/9.
 * 设备信息实体类
 */
public class DeviceBean implements Serializable {

    /**
     * status : 200
     * data : {"appDeviceInfo":[{"deviceId":"21321312162312312319","name":"u呜呜呜呜","accessType":null,
     * "ptzType":"智能眼镜","manufacturer":"","agreement":null,"ip":null,"port":null,"address":null,"status":"0",
     * "parental":null,"parentId":null,"longitude":null,"latitude":null,"num":0,"createTime":null,"createPeople":null,
     * "updateTime":1565247334000,"updatePeople":null,"password":null,"username":null}]}
     * msg : OK
     */

    private int status;
    private DataBean data;
    private String msg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class DataBean implements Serializable {
        private List<AppDeviceInfoBean> appDeviceInfo;

        public List<AppDeviceInfoBean> getAppDeviceInfo() {
            return appDeviceInfo;
        }

        public void setAppDeviceInfo(List<AppDeviceInfoBean> appDeviceInfo) {
            this.appDeviceInfo = appDeviceInfo;
        }

        public static class AppDeviceInfoBean implements Serializable {
            /**
             * deviceId : 21321312162312312319
             * name : u呜呜呜呜
             * accessType : null
             * ptzType : 智能眼镜
             * manufacturer :
             * agreement : null
             * ip : null
             * port : null
             * address : null
             * status : 0
             * parental : null
             * parentId : null
             * longitude : null
             * latitude : null
             * num : 0
             * createTime : null
             * createPeople : null
             * updateTime : 1565247334000
             * updatePeople : null
             * password : null
             * username : null
             */

            private String deviceId;
            private String name;
            private String accessType;
            private String ptzType;
            private String manufacturer;
            private String agreement;
            private String ip;
            private String port;
            private String address;
            private int status;
            private String parental;
            private String parentId;
            private String longitude;
            private String latitude;
            private int num;
            private String createTime;
            private String createPeople;
            private long updateTime;
            private String updatePeople;
            private String password;
            private String username;

            public String getDeviceId() {
                return deviceId;
            }

            public void setDeviceId(String deviceId) {
                this.deviceId = deviceId;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getAccessType() {
                return accessType;
            }

            public void setAccessType(String accessType) {
                this.accessType = accessType;
            }

            public String getPtzType() {
                return ptzType;
            }

            public void setPtzType(String ptzType) {
                this.ptzType = ptzType;
            }

            public String getManufacturer() {
                return manufacturer;
            }

            public void setManufacturer(String manufacturer) {
                this.manufacturer = manufacturer;
            }

            public String getAgreement() {
                return agreement;
            }

            public void setAgreement(String agreement) {
                this.agreement = agreement;
            }

            public String getIp() {
                return ip;
            }

            public void setIp(String ip) {
                this.ip = ip;
            }

            public String getPort() {
                return port;
            }

            public void setPort(String port) {
                this.port = port;
            }

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public String getParental() {
                return parental;
            }

            public void setParental(String parental) {
                this.parental = parental;
            }

            public String getParentId() {
                return parentId;
            }

            public void setParentId(String parentId) {
                this.parentId = parentId;
            }

            public String getLongitude() {
                return longitude;
            }

            public void setLongitude(String longitude) {
                this.longitude = longitude;
            }

            public String getLatitude() {
                return latitude;
            }

            public void setLatitude(String latitude) {
                this.latitude = latitude;
            }

            public int getNum() {
                return num;
            }

            public void setNum(int num) {
                this.num = num;
            }

            public String getCreateTime() {
                return createTime;
            }

            public void setCreateTime(String createTime) {
                this.createTime = createTime;
            }

            public String getCreatePeople() {
                return createPeople;
            }

            public void setCreatePeople(String createPeople) {
                this.createPeople = createPeople;
            }

            public long getUpdateTime() {
                return updateTime;
            }

            public void setUpdateTime(long updateTime) {
                this.updateTime = updateTime;
            }

            public String getUpdatePeople() {
                return updatePeople;
            }

            public void setUpdatePeople(String updatePeople) {
                this.updatePeople = updatePeople;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }
        }
    }
}
