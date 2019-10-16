#pragma once

#include <iostream>
#include <thread>

#include "RtpRecvDataBase.h"
#include "RtpRecvDataFactory.h"

#include "UserArguments.h"
#include "MediaDecoder.h"

using namespace std;

class PullMediaStream
{
public:
	PullMediaStream(PullStreamArgs* arg) :marg(arg) {};
	~PullMediaStream();

public:
	//��ʼ��
	int Init(jint i);

	//��ʼ����
    int StartPullStream(jint i);

	//��������
	int StopPullStream();

	//�ӻ������ж�ȡ����
	int ReadVideoFrame(uint8_t* buf, int bufsize);
	
private:
	PullStreamArgs *marg = NULL;
	RtpRecvDataBasePtr mRtpRecvServer;
	MediaDecoder mDecoder;
};

typedef shared_ptr<PullMediaStream> PullMediaStreamPtr;

