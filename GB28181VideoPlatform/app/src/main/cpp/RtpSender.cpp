#include "RtpSender.h"
#include "log.h"

RtpSender::RtpSender() {

}

RtpSender::~RtpSender() {

}

//设置发送目的IP、目的Port
int RtpSender::SetSendParam(std::string remoteip, int port, long ssrc) {
    printf("RtpSenderI::SetSendParam:%s,%d", remoteip.c_str(), port);

    remote_ip = remoteip;
    iport = port;
    issrc = ssrc;
    return 0;
}

//发送器初始化
int RtpSender::init() {
    pflie = fopen("/storage/emulated/0/DCIM/output.ps", "wb+");
    //fopen_s(&pflie,"rtp_send_data.rtp","wb+");
//#ifdef RTP_SOCKETTYPE_WINSOCK
//    WSADATA dat;
//    WSAStartup(MAKEWORD(2, 2), &dat);
//#endif // RTP_SOCKETTYPE_WINSOCK

    static RTPSessionParams sessionparams;
    /* set h264 param */
    sessionparams.SetUsePredefinedSSRC(true);         // 设置使用预先定义的SSRC
    sessionparams.SetPredefinedSSRC((u_int32_t)issrc);     //定义SSRC

    sessionparams.SetOwnTimestampUnit(1.0 / 90000.0); // 设置采样间隔
    sessionparams.SetAcceptOwnPackets(false);// 接收自己发送的数据包

    RTPUDPv4TransmissionParams transparams;
    transparams.SetPortbase(8000); //这个端口必须未被占用

    LOGD("rtp port=%d",8000);
    int status = session.Create(sessionparams, &transparams);
    if (status < 0) {
        LOGE("initRtpSender_GetErrorString==" ,status);
        //std::cerr << RTPGetErrorString(status) << std::endl;
        return -1;
    }

#if 1
    RTPIPv4Address addr(ntohl(inet_addr(remote_ip.c_str())), iport);
    status = session.AddDestination(addr);
#else
    unsigned long addr = ntohl(inet_addr("192.168.30.24"));
    status = session.AddDestination(addr, 9000);
#endif
    if (status < 0) {
        //std::cerr << RTPGetErrorString(status) << std::endl;
        //LOGE("initRtpSender_GetErrorString==" ,status);
        return -1;
    }
//
    session.SetDefaultPayloadType(98);
    session.SetDefaultMark(true);
    session.SetDefaultTimestampIncrement(90000.0 / 25.0);
    return 0;
}

//启动编码器
int RtpSender::Start() {
    return 0;
}

int RtpSender::WriteRTP(const void *data, size_t len,
                         uint8_t pt, bool mark, uint32_t timestampinc) {

    LOGE("sendRTPdata: %d", len);
    //printf("已发送数据:%d\n", sended_bytes);
    //写文件
    //fwrite(data, len, 1, pflie);

    int ret = session.SendPacket(data, len, pt, mark, timestampinc);

    LOGE("sendRTPdata ret: %d", ret);
    return ret;
}


//关闭发送器
int RtpSender::Stop() {
    session.BYEDestroy(RTPTime(10,0), 0, 0);
    return 0;
}