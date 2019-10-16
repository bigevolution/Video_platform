#pragma once

#include "RtpRecvDataBase.h"

typedef void (*packet_output)(void* buffer, size_t bytes, void* user_data);

class RtpRecvData :public RtpRecvDataBase
{
public:
	RtpRecvData();
	~RtpRecvData();

	//��ʼ��
	virtual int Init(int localPort);

	//��ʼ
	virtual int Start();

	//����RTP�����̴߳�����
	virtual void RecvProcess();

	//������������ص�
	virtual int SetPacketProcessCallBack(packet_output output, void* user_data);

	//����
	virtual int Stop();

	//������
	void CheckError(int rtperr);

private:

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

	//���ݴ���ص�
	packet_output callback_ = NULL;

	void* user_data_ = NULL;

	//RTP�Ự
	RTPSession session;
	RTPUDPv4TransmissionParams transparams;
	RTPSessionParams sessparams;
};

typedef std::shared_ptr<RtpRecvData> RtpRecvDataPtr;