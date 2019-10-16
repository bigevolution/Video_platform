#pragma once

#include <thread>
#include <algorithm>
#include "pch.h"

#include "ring_buffer_s.h"
#include "UserArguments.h"

#define  RING_BUFFER_MAX_SIZE (10 * 1024 * 1024)
using namespace std;

class MediaDecoder
{
public:
	MediaDecoder():ring_buffer_(RING_BUFFER_MAX_SIZE) {};
	~MediaDecoder();

public:
	//������д�뻷�λ�����
	void Write(char* buffer, size_t bytes);

	//�ӻ��λ������ж�ȡ����
	int Read(uint8_t* buf, int bufsize);

private:
	ring_buffer_s ring_buffer_;
};

