#include <jni.h>
#include "PullMediaStream.h"
#include "log.h"
#include "MediaDecoder.h"


//test
FILE * mfile = NULL;

PullMediaStream::~PullMediaStream()
{}

/**
  *回调函数
  *@param buffer:
  *@param bytes:
  *@param user_data:
*/
void h264output(void* buffer, size_t bytes, void* user_data)
{
	MediaDecoder* pMediaDecoder = (MediaDecoder*)user_data;
	pMediaDecoder->Write((char*)buffer, bytes);
	LOGI("receive data,write:%d",bytes);

	//test
//    if ( bytes > 0)
//	{
//		fwrite(buffer, bytes, 1, mfile);
//	}
}

/**
 *	从环形缓冲区读取数据
 *  @param buffer:存放读取数据地址
 *  @param bytes:读取数据大小
 *  @return 实际读取的大小
 */
int PullMediaStream::ReadVideoFrame(uint8_t* buf, int bufsize)
{
	int retSize = 0;
	retSize = mDecoder.Read(buf, bufsize);
	cout << "read:" << retSize << endl;
	return retSize;
}

/**
  *推流初始化             			 
*/
int PullMediaStream::Init(jint i)
{
	//test
	mfile = fopen("/storage/emulated/0/DCIM/rtp1.h264", "wb+");
	if (!mfile) {
		printf("open rtp1.ps failed\n");
		exit(-1);
	}

	//创建接收RTP接收H264的对象
    if (NULL== mRtpRecvServer) {
        mRtpRecvServer = RtpRecvDataFactory::Instance()->CreateRtpRecvData(PULL_STREAM_PS);
    }
	//初始化RTP接收数据
	mRtpRecvServer->Init(i);

    LOGD("receive video data port=%d",i);

	return 0;
}

/**
  *开始推流
*/
int PullMediaStream::StartPullStream(jint port)
{
	//初始化
    Init(port);
	//设置回调处理函数
	mRtpRecvServer->SetPacketProcessCallBack(h264output, &mDecoder);
	//开始接收RTP数据
	mRtpRecvServer->Start();

	return 0;
}

/**
  *开始推流
*/
int PullMediaStream::StopPullStream()
{
	//停止接收数据
	mRtpRecvServer->Stop();
	return 0;
}






