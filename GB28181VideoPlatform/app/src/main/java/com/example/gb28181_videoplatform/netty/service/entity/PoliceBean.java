package com.example.gb28181_videoplatform.netty.service.entity;

import java.util.List;

/**
 * Created by xk on 2018/12/14.
 */

public class PoliceBean {

    /**
     * policeRef : {"employeeId":6074892678153001000,"employeeCode":"00006","employeeName":"彭乐","location":"{\"latitude\":\"31.989836\",\"longitude\":\"118.738297\",\"address\":\"江苏省南京市建邺区云龙山路60号靠近烽火科技大厦\"}","cameraList":[{"id":"052554b28aeb406481bbdb743ac6bc7f","cid":"863525141020802","name":"眼镜-863525141020802","type":8036050588196878000,"location":"{\"latitude\":\"31.989836\",\"longitude\":\"118.738297\",\"address\":\"江苏省南京市建邺区云龙山路60号靠近烽火科技大厦\"}","groupId":70004,"status":"1","createTime":"2018-11-09 15:22:29","createTimeStr":"2018-11-09","updateTime":"2018-12-14 15:09:23","cameraTag":5726969229148175000,"isOnline":"0001","taskStatus":"on","groupType":"70004","distance":0,"employeeId":6074892678153001000,"employeeCode":"00006","employeeName":"彭乐","isPoliceDevice":"1"}]}
     * location : {"latitude":"31.990653","longitude":"118.737251","address":"江苏省南京市建邺区楠溪江东街63号靠近烽火科技大厦","locationTime":"2018-12-14 15:09:23","province":"江苏省","city":"南京市","street":"楠溪江东街","district":"建邺区","streetNumber":"63号","locationType":"device"}
     * deviceId : 863525141020802
     * status : online
     */

    private PoliceRefBean policeRef;
    private LocationBean location;
    private String deviceId;
    private String status;

    public PoliceRefBean getPoliceRef() {
        return policeRef;
    }

    public void setPoliceRef(PoliceRefBean policeRef) {
        this.policeRef = policeRef;
    }

    public LocationBean getLocation() {
        return location;
    }

    public void setLocation(LocationBean location) {
        this.location = location;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class PoliceRefBean {
        /**
         * employeeId : 6074892678153001000
         * employeeCode : 00006
         * employeeName : 彭乐
         * location : {"latitude":"31.989836","longitude":"118.738297","address":"江苏省南京市建邺区云龙山路60号靠近烽火科技大厦"}
         * cameraList : [{"id":"052554b28aeb406481bbdb743ac6bc7f","cid":"863525141020802","name":"眼镜-863525141020802","type":8036050588196878000,"location":"{\"latitude\":\"31.989836\",\"longitude\":\"118.738297\",\"address\":\"江苏省南京市建邺区云龙山路60号靠近烽火科技大厦\"}","groupId":70004,"status":"1","createTime":"2018-11-09 15:22:29","createTimeStr":"2018-11-09","updateTime":"2018-12-14 15:09:23","cameraTag":5726969229148175000,"isOnline":"0001","taskStatus":"on","groupType":"70004","distance":0,"employeeId":6074892678153001000,"employeeCode":"00006","employeeName":"彭乐","isPoliceDevice":"1"}]
         */

        private long employeeId;
        private String employeeCode;
        private String employeeName;
        private String location;
        private List<CameraListBean> cameraList;

        public long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(long employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public List<CameraListBean> getCameraList() {
            return cameraList;
        }

        public void setCameraList(List<CameraListBean> cameraList) {
            this.cameraList = cameraList;
        }

        public static class CameraListBean {
            /**
             * id : 052554b28aeb406481bbdb743ac6bc7f
             * cid : 863525141020802
             * name : 眼镜-863525141020802
             * type : 8036050588196878000
             * location : {"latitude":"31.989836","longitude":"118.738297","address":"江苏省南京市建邺区云龙山路60号靠近烽火科技大厦"}
             * groupId : 70004
             * status : 1
             * createTime : 2018-11-09 15:22:29
             * createTimeStr : 2018-11-09
             * updateTime : 2018-12-14 15:09:23
             * cameraTag : 5726969229148175000
             * isOnline : 0001
             * taskStatus : on
             * groupType : 70004
             * distance : 0
             * employeeId : 6074892678153001000
             * employeeCode : 00006
             * employeeName : 彭乐
             * isPoliceDevice : 1
             */

            private String id;
            private String cid;
            private String name;
            private long type;
            private String location;
            private long groupId;
            private String status;
            private String createTime;
            private String createTimeStr;
            private String updateTime;
            private long cameraTag;
            private String isOnline;
            private String taskStatus;
            private String groupType;
            private long distance;
            private long employeeId;
            private String employeeCode;
            private String employeeName;
            private String isPoliceDevice;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getCid() {
                return cid;
            }

            public void setCid(String cid) {
                this.cid = cid;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public long getType() {
                return type;
            }

            public void setType(long type) {
                this.type = type;
            }

            public String getLocation() {
                return location;
            }

            public void setLocation(String location) {
                this.location = location;
            }

            public long getGroupId() {
                return groupId;
            }

            public void setGroupId(int groupId) {
                this.groupId = groupId;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getCreateTime() {
                return createTime;
            }

            public void setCreateTime(String createTime) {
                this.createTime = createTime;
            }

            public String getCreateTimeStr() {
                return createTimeStr;
            }

            public void setCreateTimeStr(String createTimeStr) {
                this.createTimeStr = createTimeStr;
            }

            public String getUpdateTime() {
                return updateTime;
            }

            public void setUpdateTime(String updateTime) {
                this.updateTime = updateTime;
            }

            public long getCameraTag() {
                return cameraTag;
            }

            public void setCameraTag(long cameraTag) {
                this.cameraTag = cameraTag;
            }

            public String getIsOnline() {
                return isOnline;
            }

            public void setIsOnline(String isOnline) {
                this.isOnline = isOnline;
            }

            public String getTaskStatus() {
                return taskStatus;
            }

            public void setTaskStatus(String taskStatus) {
                this.taskStatus = taskStatus;
            }

            public String getGroupType() {
                return groupType;
            }

            public void setGroupType(String groupType) {
                this.groupType = groupType;
            }

            public long getDistance() {
                return distance;
            }

            public void setDistance(int distance) {
                this.distance = distance;
            }

            public long getEmployeeId() {
                return employeeId;
            }

            public void setEmployeeId(long employeeId) {
                this.employeeId = employeeId;
            }

            public String getEmployeeCode() {
                return employeeCode;
            }

            public void setEmployeeCode(String employeeCode) {
                this.employeeCode = employeeCode;
            }

            public String getEmployeeName() {
                return employeeName;
            }

            public void setEmployeeName(String employeeName) {
                this.employeeName = employeeName;
            }

            public String getIsPoliceDevice() {
                return isPoliceDevice;
            }

            public void setIsPoliceDevice(String isPoliceDevice) {
                this.isPoliceDevice = isPoliceDevice;
            }
        }
    }

    public static class LocationBean {
        /**
         * latitude : 31.990653
         * longitude : 118.737251
         * address : 江苏省南京市建邺区楠溪江东街63号靠近烽火科技大厦
         * locationTime : 2018-12-14 15:09:23
         * province : 江苏省
         * city : 南京市
         * street : 楠溪江东街
         * district : 建邺区
         * streetNumber : 63号
         * locationType : device
         */

        private double latitude;
        private double longitude;
        private String address;
        private String locationTime;
        private String province;
        private String city;
        private String street;
        private String district;
        private String streetNumber;
        private String locationType;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getLocationTime() {
            return locationTime;
        }

        public void setLocationTime(String locationTime) {
            this.locationTime = locationTime;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getStreetNumber() {
            return streetNumber;
        }

        public void setStreetNumber(String streetNumber) {
            this.streetNumber = streetNumber;
        }

        public String getLocationType() {
            return locationType;
        }

        public void setLocationType(String locationType) {
            this.locationType = locationType;
        }
    }
}
