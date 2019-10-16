#include "RtpRecvDataFactory.h"
#include "RtpRecvData.h"
#include "RtpRecvPSData.h"

RtpRecvDataFactory* RtpRecvDataFactory::mInstance = NULL;

RtpRecvDataFactory::RtpRecvDataFactory()
{
}

RtpRecvDataFactory::~RtpRecvDataFactory()
{
}

/*
 * ��������ʵ��
 */
RtpRecvDataFactory* RtpRecvDataFactory::Instance()
{
	static RtpRecvDataFactory instance;
	if (!mInstance)
	{
		mInstance = &instance;
	}

	return mInstance;
}

/*
* ��������RtpDataʵ��
* @param pStreamtype: ��������-ps����h264��
*/
RtpRecvDataBasePtr RtpRecvDataFactory::CreateRtpRecvData(PullStreamType pStreamtype)
{
	switch (pStreamtype)
	{
		case PULL_STREAM_H264:
			return std::dynamic_pointer_cast<RtpRecvDataBase>(std::make_shared<RtpRecvData>());
		case PULL_STREAM_PS:
			return std::dynamic_pointer_cast<RtpRecvDataBase>(std::make_shared<RtpRecvPSData>());
		default:
			return nullptr;
	}
}









