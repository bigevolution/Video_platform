package com.example.gb28181_videoplatform.netty.service.entity;

import java.util.List;

/**
 * Created by Gaozy on 2017/10/28.
 */

public class MapBean {
    /**
     * armsgId : 870f5ee84655435a88f908a5d56a20a7
     * taskId : 1007
     * operateCode : 101
     * markerKey : f078a283-c4af-4809-b794-a7d7f3eb88ee
     * markerNum : 1
     * markerType : 4
     * markerPos : {"lng":113.350067,"lat":23.157249,"location":"广东省广州市天河区五山街道珠江路华南农业大学"}
     * status : 1
     * createTime : Sep 3, 2018 4:56:52 PM
     */

    private String armsgId;
    private String taskId;//任务id
    private int operateCode;//操作类型：101：新增标识 201：删除标识 202：清空全部标识
    private String markerKey;
    private int markerNum;//点编号
    private int markerType;//类型：1：警员2：作战点 3：无人机 4：嫌疑人 5: 线条 6：多边形 10：警员（实时) 11:无人机（实时）12：云台（实时）
    private MarkerPosBean markerPos;//点坐标信息
    private List<DrawInfoBean> drawInfo;//点坐标集合
    private int status;
    private String createTime;
    private String photoUrl;
    private String markerInfo;
    private Object droneInfo;

    public Object getDroneInfo() {
        return droneInfo;
    }

    public void setDroneInfo(Object droneInfo) {
        this.droneInfo = droneInfo;
    }

    public List<DrawInfoBean> getDrawInfo() {
        return drawInfo;
    }

    public void setDrawInfo(List<DrawInfoBean> drawInfo) {
        this.drawInfo = drawInfo;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getMarkerInfo() {
        return markerInfo;
    }

    public void setMarkerInfo(String markerInfo) {
        this.markerInfo = markerInfo;
    }

    public String getArmsgId() {
        return armsgId;
    }

    public void setArmsgId(String armsgId) {
        this.armsgId = armsgId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getOperateCode() {
        return operateCode;
    }

    public void setOperateCode(int operateCode) {
        this.operateCode = operateCode;
    }

    public String getMarkerKey() {
        return markerKey;
    }

    public void setMarkerKey(String markerKey) {
        this.markerKey = markerKey;
    }

    public int getMarkerNum() {
        return markerNum;
    }

    public void setMarkerNum(int markerNum) {
        this.markerNum = markerNum;
    }

    public int getMarkerType() {
        return markerType;
    }

    public void setMarkerType(int markerType) {
        this.markerType = markerType;
    }

    public MarkerPosBean getMarkerPos() {
        return markerPos;
    }

    public void setMarkerPos(MarkerPosBean markerPos) {
        this.markerPos = markerPos;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public static class MarkerPosBean {
        /**
         * lng : 113.350067
         * lat : 23.157249
         * location : 广东省广州市天河区五山街道珠江路华南农业大学
         */

        private double lng;
        private double lat;
        private String location;
        private double height;
        private double dist;
        private double angle;
        private double cameraAngle;

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getDist() {
            return dist;
        }

        public void setDist(double dist) {
            this.dist = dist;
        }

        public double getAngle() {
            return angle;
        }

        public void setAngle(double angle) {
            this.angle = angle;
        }

        public double getCameraAngle() {
            return cameraAngle;
        }

        public void setCameraAngle(double cameraAngle) {
            this.cameraAngle = cameraAngle;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class DrawInfoBean {
        /**
         * taskId : 1007
         * operateCode : 103
         * markerKey : c9ee6a3a-bf14-4f5b-95da-5753e275b9ca
         * markerNum : 4
         * markerType : 5
         * markerPos : {"lng":118.732563,"lat":31.994867}
         * markerInfo : 11111
         */

        private String taskId;
        private int operateCode;
        private String markerKey;
        private int markerNum;
        private int markerType;
        private MarkerPosBean markerPos;
        private String markerInfo;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public int getOperateCode() {
            return operateCode;
        }

        public void setOperateCode(int operateCode) {
            this.operateCode = operateCode;
        }

        public String getMarkerKey() {
            return markerKey;
        }

        public void setMarkerKey(String markerKey) {
            this.markerKey = markerKey;
        }

        public int getMarkerNum() {
            return markerNum;
        }

        public void setMarkerNum(int markerNum) {
            this.markerNum = markerNum;
        }

        public int getMarkerType() {
            return markerType;
        }

        public void setMarkerType(int markerType) {
            this.markerType = markerType;
        }

        public MarkerPosBean getMarkerPos() {
            return markerPos;
        }

        public void setMarkerPos(MarkerPosBean markerPos) {
            this.markerPos = markerPos;
        }

        public String getMarkerInfo() {
            return markerInfo;
        }

        public void setMarkerInfo(String markerInfo) {
            this.markerInfo = markerInfo;
        }

        public static class MarkerPosBean {
            /**
             * lng : 118.732563
             * lat : 31.994867
             */

            private double lng;
            private double lat;

            public double getLng() {
                return lng;
            }

            public void setLng(double lng) {
                this.lng = lng;
            }

            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }
        }
    }

}
