package com.example.gb28181_videoplatform.bean;

import java.util.List;

/**
 * Created by 吴迪 on 2019/8/9.
 * 设备类型实体类
 */
public class DeviceType {

    /**
     * status : 200
     * data : {"ptzTypeList":[{"dateCode":"1","dateValue":"球机"},{"dateCode":"2","dateValue":"半球"},
     * {"dateCode":"3","dateValue":"固定枪机"},{"dateCode":"4","dateValue":"遥控枪机"}],"agreementTypeList":
     * [{"dateCode":"1","dateValue":"GB 28181"}]}
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

    public static class DataBean {
        private List<PtzTypeListBean> ptzTypeList;
        private List<AgreementTypeListBean> agreementTypeList;

        public List<PtzTypeListBean> getPtzTypeList() {
            return ptzTypeList;
        }

        public void setPtzTypeList(List<PtzTypeListBean> ptzTypeList) {
            this.ptzTypeList = ptzTypeList;
        }

        public List<AgreementTypeListBean> getAgreementTypeList() {
            return agreementTypeList;
        }

        public void setAgreementTypeList(List<AgreementTypeListBean> agreementTypeList) {
            this.agreementTypeList = agreementTypeList;
        }

        public static class PtzTypeListBean {
            /**
             * dateCode : 1
             * dateValue : 球机
             */

            private String dateCode;
            private String dateValue;

            public String getDateCode() {
                return dateCode;
            }

            public void setDateCode(String dateCode) {
                this.dateCode = dateCode;
            }

            public String getDateValue() {
                return dateValue;
            }

            public void setDateValue(String dateValue) {
                this.dateValue = dateValue;
            }
        }

        public static class AgreementTypeListBean {
            /**
             * dateCode : 1
             * dateValue : GB 28181
             */

            private String dateCode;
            private String dateValue;

            public String getDateCode() {
                return dateCode;
            }

            public void setDateCode(String dateCode) {
                this.dateCode = dateCode;
            }

            public String getDateValue() {
                return dateValue;
            }

            public void setDateValue(String dateValue) {
                this.dateValue = dateValue;
            }
        }
    }
}
