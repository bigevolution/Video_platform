// PullMediaStream.cpp : 此文件包含 "main" 函数。程序执行将在此处开始并结束。
//

#include <iostream>
#include "PullMediaStream.h"
#include <thread>

FILE* mfile = nullptr;

int main()
{
	UserArguments arguments;
	arguments.localPort = 9000;
	arguments.in_width = 640;
	arguments.in_height = 480;

	PullMediaStreamPtr pullStream = make_shared<PullMediaStream>(&arguments);
	pullStream->StartPullStream();

	//fopen_s(&mfile, "rtp1.h264", "wb+");
	mfile = fopen("rtp1.h264", "wb+");
	if (!mfile)
	{
		printf("open rtp1.h264 failed\n");
		return -1;
	}

	size_t tlen = 1024 * 1024;
	uint8_t* tbuf = new uint8_t[tlen];

	while (1)
	{
		std::this_thread::sleep_for(std::chrono::microseconds(12));

		size_t realSize = pullStream->ReadVideoFrame(tbuf, tlen);
		if (realSize > 0)
		{
			fwrite(tbuf, realSize, 1, mfile);
		}
	}

	while (1)
	{
		std::this_thread::sleep_for(std::chrono::microseconds(12));
	}

	pullStream->StopPullStream();

	return 0;
}

