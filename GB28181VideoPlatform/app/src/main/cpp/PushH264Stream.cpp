#include "PushH264Stream.h"
#include "log.h"
#include "UserArguments.h"

extern "C"
{
    #include "g711.h"
}
#include <thread>
#define RTP_H264      (98)
#define RTP_STAMPTIME (3000)

PushH264Stream::PushH264Stream(UserArguments* arg) :m_arg(arg), find_nalu_head_(0), frame_count_(0), outbuf_size_(0),
last_frame_type(0), ps_packet_thread_stop_flag_(false), ps_packet_thread_ptr(NULL), mux_type_(96)
{
}

PushH264Stream::~PushH264Stream()
{
	FreeNALU(nalu);
}
/*
	������ʼ��
*/
int PushH264Stream::Init(int muxType)
{
    LOGE("begin init.");
	mux_type_ = muxType;

	printf("arg->remoteip:%s, arg->remoteport:%d", m_arg->remoteip, m_arg->remotePort);

	//��ȡ����H264��ʵ��
	pSender = std::make_shared<RtpSender>();

	//���÷���Ŀ��IP��Ŀ��PORT��ssrc
	pSender->SetSendParam(m_arg->remoteip, m_arg->remotePort, m_arg->ssrc);

	nalu = AllocNALU(8000000);

	memset(&prog_info_, 0, sizeof(prog_info_));
	prog_info_.program_num = 1;
	prog_info_.prog[0].stream_num = 2;
	prog_info_.prog[0].stream[0].type = STREAM_TYPE_VIDEO_H264;
	prog_info_.prog[0].stream[1].type = STREAM_TYPE_AUDIO_G711;

    LOGE("queue init.");
    // ��ʼ�����ж���
    for(int i =0;i <10;i++)
    {
        av_packet_t packet(m_arg->audioFrameLen);
        idel_audio_queue_.push(packet);
    }

    for(int i = 0;i<3;i++)
    {
        av_packet_t packet(1000*1000*8);
        idel_video_queue_.push(packet);
    }

    LOGE("queue init end.");
	// ��������߳�
	ps_packet_thread_ptr = new std::thread(&PushH264Stream::PsMuxProcess, this);

    LOGE("init end.");
	return 0;
}

//�����ڴ�
NALU_t* PushH264Stream::AllocNALU(int buffersize) {
	NALU_t* n = NULL;

	if ((n = (NALU_t*)calloc(1, sizeof(NALU_t))) == NULL) {
		printf("AllocNALU: n");
		exit(0);
	}

	n->max_size = buffersize;

	n->begin = 0;

	n->end = 0;

	if ((n->buf = (unsigned char*)calloc(buffersize, sizeof(char))) == NULL) {
		free(n);
		printf("AllocNALU: n->buf");
		exit(0);
	}

	return n;
}

//�ͷ�
void PushH264Stream::FreeNALU(NALU_t* n) {
	if (n) {
		if (n->buf) {
			free(n->buf);
			n->buf = NULL;
		}
		free(n);
	}
}

//��Ƭ����
int PushH264Stream::SendNalu(NALU_t* n) {
	NALU_HEADER* nalu_hdr;
	FU_INDICATOR* fu_ind;
	FU_HEADER* fu_hdr;
	char sendbuf[1500] = { 0 };
	char* nalu_payload;
	unsigned int timestamp_increse = 0, ts_current = 0;
	int status = 0;

	bool start = false;

	unsigned char head3[] = { 0x00, 0x00, 0x01 };
	unsigned char head4[] = { 0x00, 0x00, 0x00, 0x01 };

	if (!start) {
		if (n->nal_unit_type == 5 || n->nal_unit_type == 6 ||
			n->nal_unit_type == 7 || n->nal_unit_type == 7) {
			printf("begin\n");
			start = true;
		}
	}

	LOGE("BEGIN SEND A NALU.....");

	if (n->len <= MAX_RTP_PKT_LENGTH)
	{
		LOGE("not splite ..... ");

		nalu_hdr = (NALU_HEADER*)sendbuf;
		nalu_hdr->F = n->forbidden_bit;
		nalu_hdr->NRI = n->nal_reference_idc >> 5;
		nalu_hdr->TYPE = n->nal_unit_type;

		nalu_payload = &sendbuf[1];
		memcpy(nalu_payload, n->buf + 1, n->len - 1);

		if (n->nal_unit_type == 1 || n->nal_unit_type == 5)
		{
			status = pSender->WriteRTP((void*)sendbuf, n->len, 98, true, RTP_STAMPTIME);
		}
		else
		{
			status = pSender->WriteRTP((void*)sendbuf, n->len, 98, false, 0);
		}

		if (status < 0)
		{
			LOGE("%s,line:%d", RTPGetErrorString(status).c_str(), __LINE__);
			exit(-1);
		}

	}
	else if (n->len > MAX_RTP_PKT_LENGTH)
	{
		LOGE("do split .... ");
		int k = 0, l = 0;
		k = n->len / MAX_RTP_PKT_LENGTH;
		l = n->len % MAX_RTP_PKT_LENGTH;

		if (l == 0)
		{
			k = k - 1;
			l = MAX_RTP_PKT_LENGTH;

			LOGE("warnning @@@@@@@@@@@@ l = 0...@@@@@@@@");
		}

		int t = 0;

		ts_current = ts_current + timestamp_increse;
		while (t <= k)
		{
			if (!t)
			{
				LOGE("start nalu split...");

				memset(sendbuf, 0, 1500);
				fu_ind = (FU_INDICATOR*)& sendbuf[0];
				fu_ind->F = n->forbidden_bit;
				fu_ind->NRI = n->nal_reference_idc >> 5;
				fu_ind->TYPE = 28;

				fu_hdr = (FU_HEADER*)& sendbuf[1];
				fu_hdr->E = 0;
				fu_hdr->R = 0;
				fu_hdr->S = 1;
				fu_hdr->TYPE = n->nal_unit_type;


				nalu_payload = &sendbuf[2];
				memcpy(nalu_payload, n->buf + 1, MAX_RTP_PKT_LENGTH);

				status = pSender->WriteRTP((void*)sendbuf, MAX_RTP_PKT_LENGTH + 2, 98, false, 0);

				if (status < 0)
				{
					LOGE("%s,line:%d", RTPGetErrorString(status).c_str(), __LINE__);
					exit(-1);
				}
				t++;
			}
			else if (k == t)
			{
				LOGE("end nalu split...");

				memset(sendbuf, 0, 1500);
				fu_ind = (FU_INDICATOR*)& sendbuf[0];
				fu_ind->F = n->forbidden_bit;
				fu_ind->NRI = n->nal_reference_idc >> 5;
				fu_ind->TYPE = 28;

				LOGE("end nalu split1...");

				fu_hdr = (FU_HEADER*)& sendbuf[1];
				fu_hdr->R = 0;
				fu_hdr->S = 0;
				fu_hdr->TYPE = n->nal_unit_type;
				fu_hdr->E = 1;
				nalu_payload = &sendbuf[2];

				LOGE("end nalu split2 %d ...", l);
				memcpy(nalu_payload, n->buf + t * MAX_RTP_PKT_LENGTH + 1, l - 1);

				LOGE("end nalu split before send ...");

				status = pSender->WriteRTP((void*)sendbuf, l + 1, 98, true, RTP_STAMPTIME);
				if (status < 0) {
					LOGE("%s,line:%d", RTPGetErrorString(status).c_str(), __LINE__);
					exit(-1);
				}
				t++;
			}
			else if (t < k && 0 != t)
			{
				LOGE("middle nalu split...");

				memset(sendbuf, 0, 1500);
				fu_ind = (FU_INDICATOR*)& sendbuf[0];
				fu_ind->F = n->forbidden_bit;
				fu_ind->NRI = n->nal_reference_idc >> 5;
				fu_ind->TYPE = 28;


				fu_hdr = (FU_HEADER*)& sendbuf[1];
				//fu_hdr->E=0;
				fu_hdr->R = 0;
				fu_hdr->S = 0;
				fu_hdr->E = 0;
				fu_hdr->TYPE = n->nal_unit_type;

				nalu_payload = &sendbuf[2];

				memcpy(nalu_payload, n->buf + t * MAX_RTP_PKT_LENGTH + 1,
					MAX_RTP_PKT_LENGTH);


				status = pSender->WriteRTP((void*)sendbuf, MAX_RTP_PKT_LENGTH + 2, 98,
					false, 0);
				if (status < 0) {
					LOGE("%s,line:%d", RTPGetErrorString(status).c_str(), __LINE__);
					exit(-1);
				}
				t++;
			}
		}
	}

	return 0;

}

//�ж��Ƿ�Ϊ0x000001,����Ƿ���1
static int FindStartCode2(unsigned char* Buf)
{
	if (Buf[0] != 0 || Buf[1] != 0 || Buf[2] != 1) return 0;
	else return 1;
}

//�ж��Ƿ�Ϊ0x00000001,����Ƿ���1
static int FindStartCode3(unsigned char* Buf)
{
	if (Buf[0] != 0 || Buf[1] != 0 || Buf[2] != 0 || Buf[3] != 1) return 0;
	else return 1;
}

/*
buffer:���ݻ�����
bytes:���ݳ���
pos:��ʼ����λ��
prefix_len��ͷ����
return: ����ҵ��򷵻���ʼλ�ã����򷵻��������һ�����
*/
int findH264StartCode(unsigned char* buffer, size_t bytes, int pos, int& prefix_len) {
	int ret = 0;
	prefix_len = 0;
	int i = pos;
	for (; i <= bytes - 4; i++) {
		ret = FindStartCode2(buffer + i);
		if (ret == 1) {
			prefix_len = 3;
			break;
		}
		else {
			ret = FindStartCode3(buffer + i);
			if (ret == 1) {
				prefix_len = 4;
				break;
			}
		}
	}

	if (!ret) {
		return (bytes);
	}

	return i;
}
// NALU slice ��Ƭ����
int PushH264Stream::Writeh264(void* buffer, size_t bytes) {
	// �ҵ���һ��nalu
	int fornt_pos = findH264StartCode((unsigned char*)buffer, bytes, 0, nalu->startcodeprefix_len);
	unsigned char* buffer_t = (unsigned char*)buffer;

	int first_pos = fornt_pos;
	int second_pos = fornt_pos;
	int first_prefix_len = nalu->startcodeprefix_len;

	int ret = 0;
	do {
		int next_prefix_len = 3;
		second_pos = findH264StartCode(buffer_t, bytes, first_pos + first_prefix_len,
			next_prefix_len);

		nalu->buf = buffer_t + first_pos + first_prefix_len;
		nalu->len = second_pos - first_pos - first_prefix_len;
		nalu->startcodeprefix_len = first_prefix_len;

		nalu->forbidden_bit = nalu->buf[0] & 0x80; //1 bit
		nalu->nal_reference_idc = nalu->buf[0] & 0x60; // 2 bit
		nalu->nal_unit_type = (nalu->buf[0]) & 0x1f;// 5 bit

		first_pos = second_pos;
		first_prefix_len = next_prefix_len;

		LOGE("send a nalu.");
		SendNalu(nalu);

	} while (second_pos != bytes);

	return bytes;
}

int findH264StartCode3(unsigned char* buffer, size_t bytes, int pos, int& prefix_len)
{
	int ret = 0;
	prefix_len = 4;
	int i = pos;
	for (; i <= bytes - 4; i++)
	{
		ret = FindStartCode3(buffer + i);
		if (ret == 1)
		{
			prefix_len = 4;
			break;
		}
	}

	if (!ret)
	{
		return (bytes);
	}

	return i;
}
// NALU slice PS����
int PushH264Stream::WritePS(void* buffer, size_t bytes)
{
	// �ҵ���һ��nalu
	int fornt_pos = findH264StartCode((unsigned char*)buffer, bytes, 0, nalu->startcodeprefix_len);
	unsigned char* buffer_t = (unsigned char*)buffer;

	int first_pos = fornt_pos;
	int second_pos = fornt_pos;
	int first_prefix_len = nalu->startcodeprefix_len;

	int ret = 0;
	do
	{
		int next_prefix_len = 4;
		second_pos = findH264StartCode(buffer_t, bytes, first_pos + first_prefix_len, next_prefix_len);

		// ps���������ʼ�ֽ�
		nalu->buf = buffer_t + first_pos;
		nalu->len = second_pos - first_pos;
		nalu->startcodeprefix_len = first_prefix_len;

		nalu->forbidden_bit = nalu->buf[first_prefix_len] & 0x80; //1 bit
		nalu->nal_reference_idc = nalu->buf[first_prefix_len] & 0x60; // 2 bit
		nalu->nal_unit_type = (nalu->buf[first_prefix_len]) & 0x1f;// 5 bit

		first_pos = second_pos;
		first_prefix_len = next_prefix_len;

		LOGE("type = %d,line:%d", nalu->nal_unit_type, __LINE__);
		SendPS(nalu);
        LOGE("SendPS end");

	} while (second_pos != bytes);

	return bytes;
}

// audio �����ps
int PushH264Stream::AddAudioPes()
{
    bool flag = true;
	while (flag)
	{
		// ��ȡ��Ƶ����
		av_packet_t packet;

        LOGE("[audio] encode audio_queue_ size=%d.",audio_queue_.size());
        LOGE("[audio] encode idel_audio_queue_ size=%d.",idel_audio_queue_.size());
		flag = audio_queue_.try_pop(packet);
       if (!flag)
        break;

		TEsFrame es = { 0 };
		es.program_number = 0;
		es.stream_number = 1;
		es.frame = packet.buf_;
		es.length = packet.len_;
		es.is_key = 0;					    // ����򵥴�����Ϊ��Ϣ֡��������֡��Ϊ�ؼ�֡��
		es.pts = 3600L * frame_count_;
		es.ps_pes_length = 8000;
		es.is_mutli_pes = 1;


		int outlen = lts_ps_stream(&es, outbuf_ + outbuf_size_, BUF_SIZE - outbuf_size_, &prog_info_);
        outbuf_size_ += outlen;

        idel_audio_queue_.push(packet);
        LOGE("[audio] end encode audio_queue_ size=%d.",audio_queue_.size());
        LOGE("[audio] end encode idel_audio_queue_ size=%d.",idel_audio_queue_.size());
	}

    return 0;
}

// ����ps
int PushH264Stream::SendPS(NALU_t* n)
{
	int ret = 0;

	//SPS��PPS�ȷ�����֡��
	int is_key = 0;
	int is_mutli_pes = 0;
	int timestampinc = 0;
	if (n->nal_unit_type == 7)//�յ�sps��Ҫ��ps ͷ��sysͷ, psm
	{
		LOGE("Get SPS NALU. set has_sps_pps_before_idr_ = 1");
		is_key = 1;
		last_frame_type = 5;
	}
	// pps,IDRֻ��Ҫ���pes����
	else if ((n->nal_unit_type == 6) || (n->nal_unit_type == 8))
	{
		is_mutli_pes = 1;
		last_frame_type = 5;
	}
	else if (n->nal_unit_type == 5)
	{
		if (last_frame_type != 5)
		{
			AddAudioPes();

			SendPacket(outbuf_, outbuf_size_);
			outbuf_size_ = 0;
		}

		is_mutli_pes = 1;
		frame_count_++;
		last_frame_type = 5;
	}
	else
	{
		AddAudioPes();

		SendPacket(outbuf_, outbuf_size_);
		outbuf_size_ = 0;

		timestampinc = 3600;
		frame_count_++;
		last_frame_type = n->nal_unit_type;
	}

	TEsFrame es = { 0 };
	es.program_number = 0;
	es.stream_number = 0;
	es.frame = n->buf;
	es.is_mutli_pes = is_mutli_pes;   // һ��ps���а������buͬ����pes��
	es.length = n->len;
	es.is_key = is_key;// ����򵥴�����Ϊ��Ϣ֡��������֡��Ϊ�ؼ�֡��

	es.pts = 3600L * frame_count_;  // ʾ���а�֡��Ϊ25fps�ۼ�ʱ�������ʽʹ��Ӧ����֡ʵ�ʵ�ʱ�����д��
	es.ps_pes_length = 8000;

	int len = lts_ps_stream(&es, outbuf_ + outbuf_size_, BUF_SIZE - outbuf_size_, &prog_info_);
	if (len < 0)
	{
		printf("ps packet error,len < 0.\n");
	}

	outbuf_size_ += len;

	return 0;
}

int PushH264Stream::SendPacket(uint8_t* buffer, size_t bytes)
{
    LOGE("begin send SendPacket");
	int len_send = 0;
	while (bytes - len_send > 1300)
	{
		int status = pSender->WriteRTP((void*)(buffer + len_send), 1300, 96, false, 0);
		len_send += 1300;
	}

	int status = pSender->WriteRTP((void*)(buffer + len_send), bytes - len_send, 96, true, 3600);

    LOGE("end send SendPacket");
	return status;
}

// NALU slice ��Ƭ����
int PushH264Stream::Write(uint8_t* buffer, int bytes, int type) {

	int ret = 0;

	LOGE("type = %d,line:%d", type, __LINE__);
	if (type == 98)
	{
		ret = Writeh264(buffer, bytes);
	}
	else if (type == 96)
	{
		ret = WritePS(buffer, bytes);
	}
	else
	{
		LOGE("not supported package type (98:rtp+264 or 96:ps is expected),line:%d", __LINE__);
	}

	return ret;
}

/*
	��ʼ����
*/
int PushH264Stream::StartPushStream()
{
	LOGE("initPush");
	//��������ʼ��
	Init(96);

	//����H264����ʼ��
	pSender->init();

	pSender->Start();
	return 0;
}

/*
	��������
*/
int PushH264Stream::StopPushStream()
{
	LOGE("StopPushStream enter");
	ps_packet_thread_stop_flag_ = true;

    if (!ps_packet_thread_ptr)
	{
		LOGE("ps_packet_thread_ptr->join()");
		ps_packet_thread_ptr->join();
	}

    if (!pSender)
	{
		LOGE("pSender->Stop()");
		pSender->Stop();
	}

	LOGE("StopPushStream out");
	return 0;
}

// ������Ƶ֡��һ������nalu��Ԫ
int PushH264Stream::SendVideoFrame(uint8_t* buf, int bytes)
{
    LOGE(" push video video_queue_.size()=%d !.\n",video_queue_.size());
    LOGE(" push video idel_video_queue_.size()=%d !.\n",idel_video_queue_.size());
    av_packet_t packet;
    bool res = idel_video_queue_.try_pop(packet);
    if (!res)
    {
        LOGE("push video error,!.\n");
        return -1;
    }
	packet.copy(buf, bytes);

	video_queue_.push(packet);
    LOGE(" end push video video_queue_.size()=%d !.\n",video_queue_.size());
    LOGE(" end push video idel_video_queue_.size()=%d !.\n",idel_video_queue_.size());
	return 0;
}

// ����һ����Ƶ֡��һ��pcm����
int PushH264Stream::SendAudioFrame(uint8_t* buf, int bytes)
{
	if ((bytes <= 0) && (bytes % 2 != 0))
	{
		LOGI("[PushH264Stream][SendAudioFrame] param: bytes=%d is not expected value.", bytes);
		return -1;
	}

    LOGE("[audio] push audio_queue_ size=%d.",audio_queue_.size());
    LOGE("[audio] push idel_audio_queue_ size=%d.",idel_audio_queue_.size());
	int ret = 0;
    av_packet_t packet;
    bool res = idel_audio_queue_.try_pop(packet);
    if (!res)
    {
        LOGE("send audio error,!.\n");
        return -1;
    }

	ret = g711a_encode(packet.buf_, (short const*)buf, bytes / 2);
	if (ret < 0)
	{
		LOGE("Failed to encode audio!.\n");
		return ret;
	}
    packet.len_ = bytes / 2;

	audio_queue_.push(packet);
    LOGE("[audio] end push audio_queue_ size=%d.",audio_queue_.size());
    LOGE("[audio] end push idel_audio_queue_ size=%d.",idel_audio_queue_.size());

	return 0;
}

void PushH264Stream::PsMuxProcess()
{
	int ret = 0;

	LOGE("begin PsMuxProcess !.\n");
	while (!ps_packet_thread_stop_flag_)
	{
		if (mux_type_ == 98)//h264
		{
			av_packet_t packet;

			video_queue_.wait_and_pop(packet);

			LOGE("get viceo packet !.\n");
			ret = Writeh264(packet.buf_, packet.len_);
			LOGE("end mux viceo packet !.\n");

			idel_video_queue_.push(packet);
		}
		else if (mux_type_ == 96)//ps
		{
			av_packet_t packet;

			// ��Ƶ���
            LOGE("to get viceo packet video_queue_.size=%d!.\n",video_queue_.size());
			video_queue_.wait_and_pop(packet);
			ret = WritePS(packet.buf_, packet.len_);
			// ��Ƶ���,����Ƶ֡��
			//if (!audio_queue_.empty())
			//{
			//	audio_queue_.try_pop(packet);
			//}

            idel_video_queue_.push(packet);
            LOGE("push viceo packet idel_video_queue_.size=%d!.\n",idel_video_queue_.size());

		}
	}
}