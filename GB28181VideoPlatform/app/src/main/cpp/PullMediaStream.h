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
	//初始化
	int Init(jint i);

	//开始拉流
    int StartPullStream(jint i);

	//结束拉流
	int StopPullStream();

	//从缓冲区中读取数据
	int ReadVideoFrame(uint8_t* buf, int bufsize);
	
private:
	PullStreamArgs *marg = NULL;
	RtpRecvDataBasePtr mRtpRecvServer;
	MediaDecoder mDecoder;
};

typedef shared_ptr<PullMediaStream> PullMediaStreamPtr;

