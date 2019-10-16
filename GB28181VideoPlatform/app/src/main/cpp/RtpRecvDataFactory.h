#pragma once

#include "RtpRecvDataBase.h"
#include "UserArguments.h"

class RtpRecvDataFactory
{
private:
	RtpRecvDataFactory();
	~RtpRecvDataFactory();

public:
	//��������ʵ��
	static RtpRecvDataFactory* Instance();

	//��������ʵ��
	static RtpRecvDataBasePtr CreateRtpRecvData(PullStreamType pStreamtype);

private:
	static RtpRecvDataFactory* mInstance;

};

