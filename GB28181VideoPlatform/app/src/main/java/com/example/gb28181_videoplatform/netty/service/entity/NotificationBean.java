package com.example.gb28181_videoplatform.netty.service.entity;

/**
 * @描述:
 * @包名: com.fh.powerpolice.displayagent.netty.service.entity
 * @类名: NotificationBean
 * @日期: 2018/8/9
 * @版权: Copyright ® 烽火星空. All right reserved.
 * @作者: Admin
 */
public class NotificationBean {


    /**
     * infoType : criminal
     * personsInfo : {"personName":"姚帅","personGender":"男","personAge":27,"personIdcard":"320381199105106056","personType":"criminal","personTag":"涉毒","control":false,"personNationality":"汉族","personAvatarUri":"/police/common/pic/upload/20180720_112338.584.jpg","personLevel":"A级","nativePlace":"江苏省徐州市新沂市","personIconUri":"/police/common/pic/upload/20180720_112338.584.jpg","phoneNumber":"","similarity":61}
     * voiceText : 姚帅,A级,涉毒
     */

    private String infoType;
    /**
     * personName : 姚帅
     * personGender : 男
     * personAge : 27
     * personIdcard : 320381199105106056
     * personType : criminal
     * personTag : 涉毒
     * control : false
     * personNationality : 汉族
     * personAvatarUri : /police/common/pic/upload/20180720_112338.584.jpg
     * personLevel : A级
     * nativePlace : 江苏省徐州市新沂市
     * personIconUri : /police/common/pic/upload/20180720_112338.584.jpg
     * phoneNumber :
     * similarity : 61
     */

    private PersonsInfoBean personsInfo;

    private CarInfoBean carInfo;
    private String voiceText;

    public String getInfoType() {
        return infoType;
    }

    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    public PersonsInfoBean getPersonsInfo() {
        return personsInfo;
    }

    public void setPersonsInfo(PersonsInfoBean personsInfo) {
        this.personsInfo = personsInfo;
    }

    public CarInfoBean getCarInfo() {
        return carInfo;
    }

    public void setCarInfo(CarInfoBean carInfo) {
        this.carInfo = carInfo;
    }

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public static class PersonsInfoBean {
        private String personName;
        private String personGender;
        private int personAge;
        private String personIdcard;
        private String personType;
        private String personTag;
        private boolean control;
        private String personNationality;
        private String personAvatarUri;
        private String personLevel;
        private String nativePlace;
        private String personIconUri;
        private String phoneNumber;
        private int similarity;

        public String getPersonName() {
            return personName;
        }

        public void setPersonName(String personName) {
            this.personName = personName;
        }

        public String getPersonGender() {
            return personGender;
        }

        public void setPersonGender(String personGender) {
            this.personGender = personGender;
        }

        public int getPersonAge() {
            return personAge;
        }

        public void setPersonAge(int personAge) {
            this.personAge = personAge;
        }

        public String getPersonIdcard() {
            return personIdcard;
        }

        public void setPersonIdcard(String personIdcard) {
            this.personIdcard = personIdcard;
        }

        public String getPersonType() {
            return personType;
        }

        public void setPersonType(String personType) {
            this.personType = personType;
        }

        public String getPersonTag() {
            return personTag;
        }

        public void setPersonTag(String personTag) {
            this.personTag = personTag;
        }

        public boolean isControl() {
            return control;
        }

        public void setControl(boolean control) {
            this.control = control;
        }

        public String getPersonNationality() {
            return personNationality;
        }

        public void setPersonNationality(String personNationality) {
            this.personNationality = personNationality;
        }

        public String getPersonAvatarUri() {
            return personAvatarUri;
        }

        public void setPersonAvatarUri(String personAvatarUri) {
            this.personAvatarUri = personAvatarUri;
        }

        public String getPersonLevel() {
            return personLevel;
        }

        public void setPersonLevel(String personLevel) {
            this.personLevel = personLevel;
        }

        public String getNativePlace() {
            return nativePlace;
        }

        public void setNativePlace(String nativePlace) {
            this.nativePlace = nativePlace;
        }

        public String getPersonIconUri() {
            return personIconUri;
        }

        public void setPersonIconUri(String personIconUri) {
            this.personIconUri = personIconUri;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public int getSimilarity() {
            return similarity;
        }

        public void setSimilarity(int similarity) {
            this.similarity = similarity;
        }
    }

    public static class CarInfoBean {
        /**
         * carNo : 苏EF10M7
         * carImageUri : http://47.98.142.126/images/20190103/20190103160953_fdc6c21feeef4a9686820029bf5be258.png
         */

        private String carNo;
        private String carImageUri;

        public String getCarNo() {
            return carNo;
        }

        public void setCarNo(String carNo) {
            this.carNo = carNo;
        }

        public String getCarImageUri() {
            return carImageUri;
        }

        public void setCarImageUri(String carImageUri) {
            this.carImageUri = carImageUri;
        }
    }
}
