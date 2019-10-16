package com.example.gb28181_videoplatform.netty.service.entity;

import java.util.List;

/**
 * @描述:
 * @包名: com.fh.powerpolice.displayagent.netty.service.entity
 * @类名: ActionTaskBean
 * @日期: 2018/8/20
 * @版权: Copyright ® 烽火星空. All right reserved.
 * @作者: Admin
 */
public class ActionTaskBean {


    /**
     * taskId : bcf2e2e2452f45738116e3ec38db7bcc
     * task : {"controlArea":[{"center":{"longitude":118.780362,"latitude":32.008695},
     * "color":"white","radius":584.248407807415,"type":"circle","area":[{"longitude":118.793873,"latitude":32.005694},
     * {"longitude":118.804581,"latitude":32.007532},{"longitude":118.816654,"latitude":32.010471},{"longitude":118.812055,"latitude":32.00104},
     * {"longitude":118.802497,"latitude":31.994793},{"longitude":118.797825,"latitude":31.998529},{"longitude":118.797825,"latitude":31.998529}]},
     * {"center":{"longitude":118.8052635,"latitude":32.002632000000006},"area":[{"longitude":118.793873,"latitude":32.005694},
     * {"longitude":118.804581,"latitude":32.007532},{"longitude":118.816654,"latitude":32.010471},{"longitude":118.812055,"latitude":32.00104},
     * {"longitude":118.802497,"latitude":31.994793},{"longitude":118.797825,"latitude":31.998529},{"longitude":118.797825,"latitude":31.998529}],
     * "color":"white","radius":0,"type":"polygon"}],"person":[{"personIdcard":"445222198708160328","personNationality":"汉族","personGender":"女",
     * "control":false,"personAvatarUri":"/police/common/pic/upload/20180104_135807.156.jpg","phoneNumber":"","personAge":31,"nativePlace":"广东省揭阳市揭西县",
     * "personTag":"普通人","personType":"normalPerson","personName":"李桂芳"},{"personIdcard":"320322199303162593","personNationality":"汉族",
     * "personGender":"男","control":false,"personAvatarUri":"/police/common/pic/upload/20180622_090639.964.jpg","phoneNumber":"","personAge":25,
     * "nativePlace":"江苏省徐州市沛县","personTag":"普通人","personType":"normalPerson","personName":"康传奇"}],"type":"person","msg":"查找嫌疑人张三"}
     */

    private String taskId;
    private TaskBean task;
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskBean getTask() {
        return task;
    }

    public void setTask(TaskBean task) {
        this.task = task;
    }

    public static class TaskBean {
        /**
         * controlArea : [{"center":{"longitude":118.780362,"latitude":32.008695},"color":"white","radius":584.248407807415,"type":"circle",
         * "area":[{"longitude":118.793873,"latitude":32.005694},{"longitude":118.804581,"latitude":32.007532},{"longitude":118.816654,
         * "latitude":32.010471},{"longitude":118.812055,"latitude":32.00104},{"longitude":118.802497,"latitude":31.994793},{"longitude":118.797825,
         * "latitude":31.998529},{"longitude":118.797825,"latitude":31.998529}]},{"center":{"longitude":118.8052635,"latitude":32.002632000000006},
         * "area":[{"longitude":118.793873,"latitude":32.005694},{"longitude":118.804581,"latitude":32.007532},{"longitude":118.816654,"latitude":32.010471},
         * {"longitude":118.812055,"latitude":32.00104},{"longitude":118.802497,"latitude":31.994793},{"longitude":118.797825,"latitude":31.998529},
         * {"longitude":118.797825,"latitude":31.998529}],"color":"white","radius":0,"type":"polygon"}]
         * person : [{"personIdcard":"445222198708160328","personNationality":"汉族","personGender":"女","control":false,
         * "personAvatarUri":"/police/common/pic/upload/20180104_135807.156.jpg","phoneNumber":"","personAge":31,"nativePlace":"广东省揭阳市揭西县",
         * "personTag":"普通人","personType":"normalPerson","personName":"李桂芳"},{"personIdcard":"320322199303162593","personNationality":"汉族",
         * "personGender":"男","control":false,"personAvatarUri":"/police/common/pic/upload/20180622_090639.964.jpg","phoneNumber":"","personAge":25,
         * "nativePlace":"江苏省徐州市沛县","personTag":"普通人","personType":"normalPerson","personName":"康传奇"}]
         * type : person
         * msg : 查找嫌疑人张三
         */

        private String type;
        private String msg;
        private String isNotice;
        private List<ControlAreaBean> controlArea;
        private List<PersonBean> person;
        private String taskName;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getIsNotice() {
            return isNotice;
        }

        public void setIsNotice(String isNotice) {
            this.isNotice = isNotice;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public List<ControlAreaBean> getControlArea() {
            return controlArea;
        }

        public void setControlArea(List<ControlAreaBean> controlArea) {
            this.controlArea = controlArea;
        }

        public List<PersonBean> getPerson() {
            return person;
        }

        public void setPerson(List<PersonBean> person) {
            this.person = person;
        }

        public static class ControlAreaBean {
            /**
             * center : {"longitude":118.780362,"latitude":32.008695}
             * color : white
             * radius : 584.248407807415
             * type : circle
             * area : [{"longitude":118.793873,"latitude":32.005694},{"longitude":118.804581,"latitude":32.007532},{"longitude":118.816654,
             * "latitude":32.010471},{"longitude":118.812055,"latitude":32.00104},{"longitude":118.802497,"latitude":31.994793},{"longitude":118.797825,
             * "latitude":31.998529},{"longitude":118.797825,"latitude":31.998529}]
             */

            private CenterBean center;
            private String color;
            private double radius;
            private String type;
            private List<AreaBean> area;

            public CenterBean getCenter() {
                return center;
            }

            public void setCenter(CenterBean center) {
                this.center = center;
            }

            public String getColor() {
                return color;
            }

            public void setColor(String color) {
                this.color = color;
            }

            public double getRadius() {
                return radius;
            }

            public void setRadius(double radius) {
                this.radius = radius;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public List<AreaBean> getArea() {
                return area;
            }

            public void setArea(List<AreaBean> area) {
                this.area = area;
            }

            public static class CenterBean {
                /**
                 * longitude : 118.780362
                 * latitude : 32.008695
                 */

                private double longitude;
                private double latitude;

                public double getLongitude() {
                    return longitude;
                }

                public void setLongitude(double longitude) {
                    this.longitude = longitude;
                }

                public double getLatitude() {
                    return latitude;
                }

                public void setLatitude(double latitude) {
                    this.latitude = latitude;
                }
            }

            public static class AreaBean {
                /**
                 * longitude : 118.793873
                 * latitude : 32.005694
                 */

                private double longitude;
                private double latitude;

                public double getLongitude() {
                    return longitude;
                }

                public void setLongitude(double longitude) {
                    this.longitude = longitude;
                }

                public double getLatitude() {
                    return latitude;
                }

                public void setLatitude(double latitude) {
                    this.latitude = latitude;
                }
            }
        }

        public static class PersonBean {
            /**
             * personIdcard : 445222198708160328
             * personNationality : 汉族
             * personGender : 女
             * control : false
             * personAvatarUri : /police/common/pic/upload/20180104_135807.156.jpg
             * phoneNumber :
             * personAge : 31
             * nativePlace : 广东省揭阳市揭西县
             * personTag : 普通人
             * personType : normalPerson
             * personName : 李桂芳
             */

            private String personIdcard;
            private String personNationality;
            private String personGender;
            private boolean control;
            private String personAvatarUri;
            private String phoneNumber;
            private int personAge;
            private String nativePlace;
            private String personTag;
            private String personType;
            private String personName;

            public String getPersonIdcard() {
                return personIdcard;
            }

            public void setPersonIdcard(String personIdcard) {
                this.personIdcard = personIdcard;
            }

            public String getPersonNationality() {
                return personNationality;
            }

            public void setPersonNationality(String personNationality) {
                this.personNationality = personNationality;
            }

            public String getPersonGender() {
                return personGender;
            }

            public void setPersonGender(String personGender) {
                this.personGender = personGender;
            }

            public boolean isControl() {
                return control;
            }

            public void setControl(boolean control) {
                this.control = control;
            }

            public String getPersonAvatarUri() {
                return personAvatarUri;
            }

            public void setPersonAvatarUri(String personAvatarUri) {
                this.personAvatarUri = personAvatarUri;
            }

            public String getPhoneNumber() {
                return phoneNumber;
            }

            public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
            }

            public int getPersonAge() {
                return personAge;
            }

            public void setPersonAge(int personAge) {
                this.personAge = personAge;
            }

            public String getNativePlace() {
                return nativePlace;
            }

            public void setNativePlace(String nativePlace) {
                this.nativePlace = nativePlace;
            }

            public String getPersonTag() {
                return personTag;
            }

            public void setPersonTag(String personTag) {
                this.personTag = personTag;
            }

            public String getPersonType() {
                return personType;
            }

            public void setPersonType(String personType) {
                this.personType = personType;
            }

            public String getPersonName() {
                return personName;
            }

            public void setPersonName(String personName) {
                this.personName = personName;
            }
        }
    }
}
