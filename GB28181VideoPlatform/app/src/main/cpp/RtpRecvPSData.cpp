#include "RtpRecvPSData.h"
#include "log.h"
RtpRecvPSData::RtpRecvPSData()
{
//	psFile = fopen("/storage/emulated/0/DCIM/mobile_receive.ps", "wb+");
}

RtpRecvPSData::~RtpRecvPSData()
{
};

/*
* 初始化
*/
void RtpRecvPSData::CheckError(int rtperr)
{
	if (rtperr < 0)
	{
		cout << "ERROR: " << RTPGetErrorString(rtperr) << endl;
		exit(-1);
	}
}

/**
 * 初始化
*/
int RtpRecvPSData::Init(int localPort)
{

#ifdef RTP_SOCKETTYPE_WINSOCK
	WSADATA dat;
	WSAStartup(MAKEWORD(2, 2), &dat);
#endif // RTP_SOCKETTYPE_WINSOCK


	sessparams.SetOwnTimestampUnit(1.0 / 90000.0);
	sessparams.SetUsePollThread(true);
	sessparams.SetMaximumPacketSize(65535);
	sessparams.SetAcceptOwnPackets(true);

	transparams.ClearLocalIPList();
	transparams.SetPortbase(localPort);
	transparams.SetRTCPReceiveBuffer(65535*20);
	int status = session.Create(sessparams, &transparams);
	CheckError(status);
	LOGD("fsg: rtpRecvPSData:line:45,state:%d,port=%d",status,localPort);

	return 0;
};

int RtpRecvPSData::Start()
{
	pThread = new std::thread(&RtpRecvPSData::RecvProcess, this);
    stop= false;
	return 0;
}

/**
 * 接收数据
*/
void RtpRecvPSData::RecvProcess()
{
	bool isCompletePS = false; //是否为完整的PS帧

	unsigned char* recvRtpDataBuf = new unsigned char[RECV_BUFFER_MAX_SIZE]; //接收RTP的PS流
	unsigned char* analysisH264Buf = new unsigned char[RECV_BUFFER_MAX_SIZE]; //存放从PS帧从提取的H264帧
	
	memset(recvRtpDataBuf, 0, RECV_BUFFER_MAX_SIZE * sizeof(unsigned char));
	memset(analysisH264Buf,0, RECV_BUFFER_MAX_SIZE * sizeof(unsigned char));
	memset(&mdemux, 0, sizeof(mdemux));


    //LOGD("stop:%d",stop);
	while (!stop)
	{
		//LOGD("rtpRecvPSData:line:72");
		session.BeginDataAccess();
		if (session.GotoFirstSourceWithData())
		{
			//LOGD("rtpRecvPSData:line:76");
			do
			{
				RTPPacket* pack;
				while ((pack = session.GetNextPacket()) != NULL)
				{
					//LOGD("rtpRecvPSData:line:78");
					int nPayType = pack->GetPayloadType();
					int nLen = pack->GetPayloadLength();
					unsigned char* pPayData = pack->GetPayloadData();
					int nPackLen = pack->GetPacketLength();
					unsigned char* pPackData = pack->GetPacketData();
					int csrcCont = pack->GetCSRCCount();
					int ssrc = pack->GetSSRC();
					int nTimestamp = pack->GetTimestamp();
					int nSeqNum = pack->GetSequenceNumber();
					bool nMarker = pack->HasMarker();

					LOGD("nSeqNum:%d",nSeqNum);

					//查找PS头0x000001BA
					if (pack->GetPacketData()[12] == 0x00 && pack->GetPacketData()[13] == 0x00 && pack->GetPacketData()[14] == 0x01 && pack->GetPacketData()[15] == 0xba)
					{
						if (isCompletePS) //前一完整的PS帧
						{
							//解封装PS，并将提取的h264帧，赋值给回调函数
//							fwrite(recvRtpDataBuf, recvDataLen, 1, psFile);
							BufferHandle(recvRtpDataBuf, recvDataLen);
							recvDataLen = 0;
						}

						memcpy(recvRtpDataBuf + recvDataLen, pPayData, nLen); //拷贝payData
						recvDataLen += nLen;
						isCompletePS = true;
					}
					else //如果开头不是0x000001BA,默认为一个帧的中间部分,将这部分内存顺着帧的开头向后存储
					{
						memcpy(recvRtpDataBuf + recvDataLen, pPayData, nLen); //拷贝payData
						recvDataLen += nLen;
					}

					session.DeletePacket(pack);
				}
			} while (session.GotoNextSourceWithData());
		}
		else
		{
			//Sleep(10);
		}
		session.EndDataAccess();

#ifndef RTP_SUPPORT_THREAD
		int status = session.Poll();
		CheckError(status);
#endif // RTP_SUPPORT_THREAD

	}

	//session.Destroy();
	delete[] recvRtpDataBuf;
	delete[] analysisH264Buf;
	//session.BYEDestroy(RTPTime(10, 0), 0, 0);
};


//设置数据输出回调
int RtpRecvPSData::SetPacketProcessCallBack(packet_output output, void* user_data)
{
	callback_ = output;

	user_data_ = user_data;

	return 0;
}

/*
* 结束
*/
int RtpRecvPSData::Stop()
{
    LOGD("RtpRecvPSData line 155");
	stop = true;
	pThread->join();
	delete pThread;

	session.BYEDestroy(RTPTime(10, 0), 0, 0);
	//WSACleanup();
	return 0;
};


/*
* buff处理函数
*/
int RtpRecvPSData::DecodingOutput(uint8_t* buf, int size)
{
	 demuxLen = mpsDemux.ps_demux_output(buf, size, mdemux);
	 //获取解封装PS后的H264帧
	 if ((demuxLen > 0)&& mdemux.is_pes)
	 {
	 	callback_((char*)mdemux.es_ptr, mdemux.es_len, user_data_);
	 }

	 return demuxLen;
}


/*
* buff处理函数
*/
int RtpRecvPSData::BufferHandle(uint8_t* buf, int size)
{
	//读入数据并解封装PS
	int len = 0;
	int parsed = 0;

	while (1)
	{
		len = DecodingOutput(buf + parsed, size - parsed);
		if (len <= 0)
		{
			break;
		}
		parsed += len;
	}

	//memmove(buf, buf + parsed, buf_len - parsed);
	//buf_len -= parsed;

	return 0;
}