#pragma once

#include "jrtplib3/rtpsession.h"
#include "jrtplib3/rtpsessionparams.h"
#include "jrtplib3/rtpudpv4transmitter.h"
#include "jrtplib3/rtpipv4address.h"
#include "jrtplib3/rtptimeutilities.h"
#include "jrtplib3/rtppacket.h"
#include <memory>

using namespace jrtplib;

class RtpSender {
public:
    RtpSender();

    virtual ~RtpSender();

    int SetSendParam(std::string remoteip, int port, long ssrc);

    virtual int init();

    virtual int Start();

    int WriteRTP(const void *data, size_t len,
                 uint8_t pt, bool mark, uint32_t timestampinc);

    virtual int Stop();

private:
    RTPSession session;
    //std::string remote_ip = "192.168.30.24";
    //std::string remote_ip = "192.168.18.1";
    //int iport = 9000;

    std::string remote_ip;
    int iport;
    long issrc;
    size_t sended_bytes = 0;

    FILE *pflie = NULL;
};

typedef std::shared_ptr<RtpSender> RtpSenderPtr;