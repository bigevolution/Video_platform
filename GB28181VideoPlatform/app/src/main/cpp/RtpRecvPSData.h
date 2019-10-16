#pragma once

#include <thread>
#include <iostream>
#include <algorithm>
#include "PSDemux.h"
#include "RtpRecvDataBase.h"
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

class RtpRecvPSData :public RtpRecvDataBase
{
public:
	RtpRecvPSData();
	~RtpRecvPSData();

	//初始化
	int Init(int localPort);

	//开始
	int Start();

	//接收RTP数据线程处理函数
	void RecvProcess();

	//设置数据输出回调
	int SetPacketProcessCallBack(packet_output output, void* user_data);

	//结束
	int Stop();

	int DecodingOutput(uint8_t* buf, int size);

	//buff 处理函数
	int BufferHandle(uint8_t* buf, int size);

	//错误检查
	void CheckError(int rtperr);

private:

	FILE *psFile = NULL;
	bool stop = false;

	thread* pThread = NULL;

	//监听端口
	int listPort = 9000;

	//数据量
	size_t sendedBytes = 0;

	//接收数据量
	size_t recvDataLen = 0;

	//包序号
	size_t packetSeq = 0;

	//解封装大小
	int demuxLen = 0;

	//数据处理回调
	packet_output callback_ = NULL;

	void* user_data_ = NULL;

	//PS解封装对象
	PSDemux mpsDemux;

	//PS解码结构体
	TDemux mdemux;

	//RTP会话
	RTPSession session;
	RTPUDPv4TransmissionParams transparams;
	RTPSessionParams sessparams;
};
typedef std::shared_ptr<RtpRecvPSData> RtpRecvPSDataPtr;

