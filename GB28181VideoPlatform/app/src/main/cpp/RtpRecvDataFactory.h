#pragma once

#include "RtpRecvDataBase.h"
#include "UserArguments.h"

class RtpRecvDataFactory
{
private:
	RtpRecvDataFactory();
	~RtpRecvDataFactory();

public:
	//创建工厂实例
	static RtpRecvDataFactory* Instance();

	//创建拉流实例
	static RtpRecvDataBasePtr CreateRtpRecvData(PullStreamType pStreamtype);

private:
	static RtpRecvDataFactory* mInstance;

};

