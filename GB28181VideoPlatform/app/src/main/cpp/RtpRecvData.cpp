#include "RtpRecvData.h"

RtpRecvData::RtpRecvData()
{
};

RtpRecvData::~RtpRecvData()
{
};

/*
* 初始化
*/
void RtpRecvData::CheckError(int rtperr)
{
	if (rtperr < 0)
	{
		cout <<"ERROR: " << RTPGetErrorString(rtperr) << endl;
		exit(-1);
	}
}

/**
 * 初始化
*/
int RtpRecvData::Init(int localPort)
{

#ifdef RTP_SOCKETTYPE_WINSOCK
	WSADATA dat;
	WSAStartup(MAKEWORD(2, 2), &dat);
#endif // RTP_SOCKETTYPE_WINSOCK


	sessparams.SetOwnTimestampUnit(1.0 / 90000.0);
	sessparams.SetAcceptOwnPackets(true);

	transparams.SetPortbase(localPort);
	int status = session.Create(sessparams, &transparams);
	CheckError(status);

	return 0;
};

int RtpRecvData::Start()
{
	pThread = new std::thread(&RtpRecvData::RecvProcess, this);
	return 0;
}

/**
 * 接收数据
*/
void RtpRecvData::RecvProcess()
{
	unsigned char headThreeBytes[] = { 0x00,0x00,0x01 };
	unsigned char headFourBytes[] = { 0x00,0x00,0x00,0x01 };

	unsigned char* recvRtpDataBuf = new unsigned char[RECV_BUFFER_MAX_SIZE];
	memset(recvRtpDataBuf, 0, RECV_BUFFER_MAX_SIZE* sizeof(unsigned char));

	while (!stop)
	{
		session.BeginDataAccess();
		if (session.GotoFirstSourceWithData())
		{
			do
			{
				RTPPacket* pack;
				while ((pack = session.GetNextPacket()) != NULL)
				{
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

					/*
					if (packetSeq + 1 != nSeqNum)
					{
						printf("数据包序列号不一致，可能丢包: 前一包序号为%d,当前包序号%d\n", packetSeq, nSeqNum);
					}
					*/

					if ((nSeqNum < packetSeq) && (packetSeq != 0))
					{
						session.DeletePacket(pack);
						packetSeq = nSeqNum;
						continue;
					}

					packetSeq = nSeqNum;
					sendedBytes += nLen;
					//printf("已接收数据:%d\n", sendedBytes);

					//分片组装
					unsigned char ualu_type = pPayData[0] & 0x1F;
					if ((ualu_type == 5) || (ualu_type == 1) ||
						(ualu_type == 7) || (ualu_type == 8))
					{
						memcpy(recvRtpDataBuf, headFourBytes, 4); //拷贝起始码
						recvDataLen += 4;
						memcpy(recvRtpDataBuf + recvDataLen, pPayData, nLen); //拷贝payData
						recvDataLen += nLen;

						//完整的Naul单元，
						callback_((char*)recvRtpDataBuf, recvDataLen, user_data_);
						recvDataLen = 0;
					}
					else if ((ualu_type == 28) || (ualu_type == 29))//FU-A 或 FU-B
					{
						if (pPayData[1] & 0x80)//分片开始  first pkt
						{
							//计算NALU头
							unsigned char NALU_Head = (pPayData[0] & 0xe0) | (pPayData[1] & 0x1F);
							unsigned char ualu_type = NALU_Head & 0x1F;
							if ((ualu_type == 7) || (ualu_type == 8) || (ualu_type == 1))
							{
								memcpy(recvRtpDataBuf, headFourBytes, 4);
								recvDataLen += 4;
							}
							else
							{
								memcpy(recvRtpDataBuf, headThreeBytes, 3);
								recvDataLen += 3;
							}

							memcpy(recvRtpDataBuf + recvDataLen, &NALU_Head, 1);
							recvDataLen += 1;
							memcpy(recvRtpDataBuf + recvDataLen, pPayData + 2, nLen - 2);
							recvDataLen += nLen - 2;
						}
						else if (pPayData[1] & 0x40) //last pkt
						{
							//组成了完成整的NAUL单元
							memcpy(recvRtpDataBuf + recvDataLen, pPayData + 2, nLen - 2);
							recvDataLen += nLen - 2;
							
							//将完整的Naul单元
							callback_((char*)recvRtpDataBuf, recvDataLen, user_data_);
							//清空
							recvDataLen = 0;

						} //middle pkt
						else
						{
							memcpy(recvRtpDataBuf + recvDataLen, pPayData + 2, nLen - 2);
							recvDataLen += nLen - 2;
						}
					}
					else
					{
						//printf("else nalu type:%x,startcode : 00 00 01 !\n", ualu_type);
						memcpy(recvRtpDataBuf, headThreeBytes, 3); //拷贝起始码
						recvDataLen += 3;

						memcpy(recvRtpDataBuf + recvDataLen, pPayData, nLen); //拷贝payData
						recvDataLen += nLen;

						//将完整的Naul单元
						callback_((char*)recvRtpDataBuf, recvDataLen, user_data_);
						//清空
						recvDataLen = 0;
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
	session.BYEDestroy(RTPTime(10, 0), 0, 0);
};


//设置数据输出回调
int RtpRecvData::SetPacketProcessCallBack(packet_output output, void* user_data)
{
	callback_ = output;

	user_data_ = user_data;

	return 0;
}

/*
* 结束
*/
int RtpRecvData::Stop()
{
	stop = true;
	pThread->join();
	delete pThread;

	//WSACleanup();
	return 0;
};