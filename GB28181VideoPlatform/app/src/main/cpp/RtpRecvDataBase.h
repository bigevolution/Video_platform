#pragma once

#include <thread>
#include <iostream>
#include <algorithm>
#include "jrtplib/rtpsession.h"
#include "jrtplib/rtpsessionparams.h"
#include "jrtplib/rtpudpv4transmitter.h"
#include "jrtplib/rtpipv4address.h"
#include "jrtplib/rtptimeutilities.h"
#include "jrtplib/rtppacket.h"
#include "jrtplib/rtperrors.h"
#include "jrtplib/rtpsourcedata.h"
using namespace jrtplib;
using namespace std;

#define  RECV_BUFFER_MAX_SIZE (10 * 1024 * 1024)

typedef void (*packet_output)(void* buffer, size_t bytes, void* user_data);

class RtpRecvDataBase
{
public:
	RtpRecvDataBase();
	virtual ~RtpRecvDataBase();

	//初始化
	virtual int Init(int localPort) = 0;

	virtual int Start() = 0;

	//接收RTP数据线程处
	// 理函数
	virtual void RecvProcess() = 0;

	//设置数据输出回调
	virtual int SetPacketProcessCallBack(packet_output output, void* user_data) = 0;

	//结束
	virtual int Stop() = 0;
};

typedef std::shared_ptr<RtpRecvDataBase> RtpRecvDataBasePtr;