#pragma once

#include "RtpRecvDataBase.h"

typedef void (*packet_output)(void* buffer, size_t bytes, void* user_data);

class RtpRecvData :public RtpRecvDataBase
{
public:
	RtpRecvData();
	~RtpRecvData();

	//初始化
	virtual int Init(int localPort);

	//开始
	virtual int Start();

	//接收RTP数据线程处理函数
	virtual void RecvProcess();

	//设置数据输出回调
	virtual int SetPacketProcessCallBack(packet_output output, void* user_data);

	//结束
	virtual int Stop();

	//错误检查
	void CheckError(int rtperr);

private:

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

	//数据处理回调
	packet_output callback_ = NULL;

	void* user_data_ = NULL;

	//RTP会话
	RTPSession session;
	RTPUDPv4TransmissionParams transparams;
	RTPSessionParams sessparams;
};

typedef std::shared_ptr<RtpRecvData> RtpRecvDataPtr;