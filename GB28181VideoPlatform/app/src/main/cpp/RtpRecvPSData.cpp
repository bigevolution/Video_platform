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
* ��ʼ��
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
 * ��ʼ��
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
 * ��������
*/
void RtpRecvPSData::RecvProcess()
{
	bool isCompletePS = false; //�Ƿ�Ϊ������PS֡

	unsigned char* recvRtpDataBuf = new unsigned char[RECV_BUFFER_MAX_SIZE]; //����RTP��PS��
	unsigned char* analysisH264Buf = new unsigned char[RECV_BUFFER_MAX_SIZE]; //��Ŵ�PS֡����ȡ��H264֡
	
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

					//����PSͷ0x000001BA
					if (pack->GetPacketData()[12] == 0x00 && pack->GetPacketData()[13] == 0x00 && pack->GetPacketData()[14] == 0x01 && pack->GetPacketData()[15] == 0xba)
					{
						if (isCompletePS) //ǰһ������PS֡
						{
							//���װPS��������ȡ��h264֡����ֵ���ص�����
//							fwrite(recvRtpDataBuf, recvDataLen, 1, psFile);
							BufferHandle(recvRtpDataBuf, recvDataLen);
							recvDataLen = 0;
						}

						memcpy(recvRtpDataBuf + recvDataLen, pPayData, nLen); //����payData
						recvDataLen += nLen;
						isCompletePS = true;
					}
					else //�����ͷ����0x000001BA,Ĭ��Ϊһ��֡���м䲿��,���ⲿ���ڴ�˳��֡�Ŀ�ͷ���洢
					{
						memcpy(recvRtpDataBuf + recvDataLen, pPayData, nLen); //����payData
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


//������������ص�
int RtpRecvPSData::SetPacketProcessCallBack(packet_output output, void* user_data)
{
	callback_ = output;

	user_data_ = user_data;

	return 0;
}

/*
* ����
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
* buff������
*/
int RtpRecvPSData::DecodingOutput(uint8_t* buf, int size)
{
	 demuxLen = mpsDemux.ps_demux_output(buf, size, mdemux);
	 //��ȡ���װPS���H264֡
	 if ((demuxLen > 0)&& mdemux.is_pes)
	 {
	 	callback_((char*)mdemux.es_ptr, mdemux.es_len, user_data_);
	 }

	 return demuxLen;
}


/*
* buff������
*/
int RtpRecvPSData::BufferHandle(uint8_t* buf, int size)
{
	//�������ݲ����װPS
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