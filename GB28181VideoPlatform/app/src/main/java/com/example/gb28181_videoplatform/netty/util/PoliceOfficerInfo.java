package com.example.gb28181_videoplatform.netty.util;

/**
 * @描述: 警员信息
 * @包名: com.fh.powerpolice.displayagent.service.dataobj
 * @类名: PoliceOfficerInfo
 * @日期: 2017/4/12
 * @版权: Copyright ® 烽火星空. All right reserved.
 * @作者: fengshiguang
 */
public class PoliceOfficerInfo {
    private String sessionId;
    private String policeId;
    private String policeName;
    private String avatarUri;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPoliceId() {
        return policeId;
    }

    public void setPoliceId(String policeId) {
        this.policeId = policeId;
    }

    public String getPoliceName() {
        return policeName;
    }

    public void setPoliceName(String policeName) {
        this.policeName = policeName;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    @Override
    public String toString() {
        return "\""+sessionId+"&"+policeId+"&"+policeName+"&"+avatarUri+"\"";
    }
}
