#pragma once

#include "NALDecoder.h"
#include "UserArguments.h"
#include "RtpSender.h"
#include <iostream>
#include <memory>
#include <thread>
#include "litets.h"
#include "threadsafe_queue.h"
#include "log.h"
#define BUF_SIZE (1<<21)

class av_packet_t
{
public:
	uint8_t* buf_;			//������
	size_t len_;			//���ݳ���
	size_t max_len_;	    //�ռ��С

	av_packet_t()
	{
		buf_ = NULL;
		len_ = 0;
		max_len_ = 0;
	}
	av_packet_t(size_t bytes)
	{
        buf_ = NULL;
        len_ = 0;
        max_len_ = 0;

		malloc(bytes);
	}

	~av_packet_t()
	{
		buf_ = NULL;
		len_ = 0;
		max_len_ = 0;
	}

	av_packet_t(const av_packet_t& other)
	{
		buf_ = other.buf_;
		len_ = other.len_;
		max_len_ = other.max_len_;
	}

	av_packet_t& operator=(const av_packet_t& other)
	{
		buf_ = other.buf_;
		len_ = other.len_;
		max_len_ = other.max_len_;

		return *this;
	}

	int malloc(size_t bytes)
	{
		if (buf_ == NULL)
		{
			buf_ = new uint8_t[bytes];
			max_len_ = bytes;
		}
		else if (bytes > max_len_)
		{
            LOGE("malloc bytes= %d > max_len_\n",bytes);
			free();

			buf_ = new uint8_t[bytes];
			max_len_ = bytes;
		}

		if (buf_ == NULL)
		{
			return -1;
		}

		return max_len_;
	}

	int free()
	{
		LOGE("free packet !.\n");
		if (buf_!=NULL)
		{
			delete[] buf_;
			buf_ = NULL;
			max_len_ = 0;
			len_ = 0;
		}
		LOGE("end free packet !.\n");
		return 0;
	}

	int copy(uint8_t* data, size_t bytes)
	{
		int ret = 0;

		if (buf_ == NULL)
		{
			buf_ = new uint8_t[bytes*2];
			max_len_ = bytes*2;
		}
		else if (bytes > max_len_)
		{
            LOGE("copy bytes= %d > max_len_ = %d\n",bytes,max_len_);
			free();

			buf_ = new uint8_t[bytes*2];
			max_len_ = bytes*2;
		}

		if (ret == 0)
		{
			memcpy(buf_,data, bytes);
			len_ = bytes;
			ret = len_;
		}

		return ret;
	}
	
};

typedef std::shared_ptr<av_packet_t> PacketPtr;

class PushH264Stream {
public:
    PushH264Stream(UserArguments *arg);

    ~PushH264Stream();

    //������ʼ��
    int Init(int muxType);

	// ������Ƶ֡��һ������nalu��Ԫ
	int SendVideoFrame(uint8_t* buf, int bytes);

	// ����һ����Ƶ֡��һ��pcm����
	int SendAudioFrame(uint8_t* buf, int bytes);

	// ����߳�
	void PsMuxProcess();

    //��Ƭ����
    int SendNalu(NALU_t *n);

    // NALU slice ��Ƭ����
    int Writeh264(void *buffer, size_t bytes);

    // NALU slice PS����
    int WritePS(void *buffer, size_t bytes);

    //��app��YUVԭʼ��Ƶ����д�뻺������
    int Write(uint8_t *buf, int bytes, int type);

    // ����ps
    int SendPS(NALU_t *n);

	// audio �����ps
	int AddAudioPes();

	// ����rtp���ݰ�
	int SendPacket(uint8_t* buffer, size_t bytes);

    //��ʼ����
    virtual int StartPushStream();

    //��������
    virtual int StopPushStream();

    //�����ڴ�
    NALU_t *AllocNALU(int buffersize);

    //�ͷ�
    void FreeNALU(NALU_t *n);

private:
    UserArguments *m_arg;
    RtpSenderPtr pSender;
    int find_nalu_head_;
    NALU_t *nalu = NULL;

    int frame_count_;
    TsProgramInfo prog_info_;
    uint8_t outbuf_[BUF_SIZE];
	size_t outbuf_size_;

	int last_frame_type;

	int mux_type_;  //���÷�ʽ��96,PS; 98,H264

	// ��Ƶ����֡����
	threadsafe_queue<av_packet_t> audio_queue_;
	// ������Ƶ����֡����
	threadsafe_queue<av_packet_t> idel_audio_queue_;

	// ��Ƶ����֡����
	threadsafe_queue<av_packet_t> video_queue_;
	// ������Ƶ����֡����
	threadsafe_queue<av_packet_t> idel_video_queue_;

	std::thread* ps_packet_thread_ptr;
	bool ps_packet_thread_stop_flag_;

};

typedef std::shared_ptr<PushH264Stream> PushH264StreamPtr;