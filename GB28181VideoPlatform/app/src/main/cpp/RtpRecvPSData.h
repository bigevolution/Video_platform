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

	//��ʼ��
	int Init(int localPort);

	//��ʼ
	int Start();

	//����RTP�����̴߳�����
	void RecvProcess();

	//������������ص�
	int SetPacketProcessCallBack(packet_output output, void* user_data);

	//����
	int Stop();

	int DecodingOutput(uint8_t* buf, int size);

	//buff ������
	int BufferHandle(uint8_t* buf, int size);

	//������
	void CheckError(int rtperr);

private:

	FILE *psFile = NULL;
	bool stop = false;

	thread* pThread = NULL;

	//�����˿�
	int listPort = 9000;

	//������
	size_t sendedBytes = 0;

	//����������
	size_t recvDataLen = 0;

	//�����
	size_t packetSeq = 0;

	//���װ��С
	int demuxLen = 0;

	//���ݴ���ص�
	packet_output callback_ = NULL;

	void* user_data_ = NULL;

	//PS���װ����
	PSDemux mpsDemux;

	//PS����ṹ��
	TDemux mdemux;

	//RTP�Ự
	RTPSession session;
	RTPUDPv4TransmissionParams transparams;
	RTPSessionParams sessparams;
};
typedef std::shared_ptr<RtpRecvPSData> RtpRecvPSDataPtr;

