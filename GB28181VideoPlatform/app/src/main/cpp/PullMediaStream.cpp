#include <jni.h>
#include "PullMediaStream.h"
#include "log.h"
#include "MediaDecoder.h"


//test
FILE * mfile = NULL;

PullMediaStream::~PullMediaStream()
{}

/**
  *�ص�����
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
 *	�ӻ��λ�������ȡ����
 *  @param buffer:��Ŷ�ȡ���ݵ�ַ
 *  @param bytes:��ȡ���ݴ�С
 *  @return ʵ�ʶ�ȡ�Ĵ�С
 */
int PullMediaStream::ReadVideoFrame(uint8_t* buf, int bufsize)
{
	int retSize = 0;
	retSize = mDecoder.Read(buf, bufsize);
	cout << "read:" << retSize << endl;
	return retSize;
}

/**
  *������ʼ��             			 
*/
int PullMediaStream::Init(jint i)
{
	//test
	mfile = fopen("/storage/emulated/0/DCIM/rtp1.h264", "wb+");
	if (!mfile) {
		printf("open rtp1.ps failed\n");
		exit(-1);
	}

	//��������RTP����H264�Ķ���
    if (NULL== mRtpRecvServer) {
        mRtpRecvServer = RtpRecvDataFactory::Instance()->CreateRtpRecvData(PULL_STREAM_PS);
    }
	//��ʼ��RTP��������
	mRtpRecvServer->Init(i);

    LOGD("receive video data port=%d",i);

	return 0;
}

/**
  *��ʼ����
*/
int PullMediaStream::StartPullStream(jint port)
{
	//��ʼ��
    Init(port);
	//���ûص�������
	mRtpRecvServer->SetPacketProcessCallBack(h264output, &mDecoder);
	//��ʼ����RTP����
	mRtpRecvServer->Start();

	return 0;
}

/**
  *��ʼ����
*/
int PullMediaStream::StopPullStream()
{
	//ֹͣ��������
	mRtpRecvServer->Stop();
	return 0;
}






