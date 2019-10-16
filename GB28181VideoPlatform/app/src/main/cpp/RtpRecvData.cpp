#include "RtpRecvData.h"

RtpRecvData::RtpRecvData()
{
};

RtpRecvData::~RtpRecvData()
{
};

/*
* ��ʼ��
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
 * ��ʼ��
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
 * ��������
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
						printf("���ݰ����кŲ�һ�£����ܶ���: ǰһ�����Ϊ%d,��ǰ�����%d\n", packetSeq, nSeqNum);
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
					//printf("�ѽ�������:%d\n", sendedBytes);

					//��Ƭ��װ
					unsigned char ualu_type = pPayData[0] & 0x1F;
					if ((ualu_type == 5) || (ualu_type == 1) ||
						(ualu_type == 7) || (ualu_type == 8))
					{
						memcpy(recvRtpDataBuf, headFourBytes, 4); //������ʼ��
						recvDataLen += 4;
						memcpy(recvRtpDataBuf + recvDataLen, pPayData, nLen); //����payData
						recvDataLen += nLen;

						//������Naul��Ԫ��
						callback_((char*)recvRtpDataBuf, recvDataLen, user_data_);
						recvDataLen = 0;
					}
					else if ((ualu_type == 28) || (ualu_type == 29))//FU-A �� FU-B
					{
						if (pPayData[1] & 0x80)//��Ƭ��ʼ  first pkt
						{
							//����NALUͷ
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
							//������������NAUL��Ԫ
							memcpy(recvRtpDataBuf + recvDataLen, pPayData + 2, nLen - 2);
							recvDataLen += nLen - 2;
							
							//��������Naul��Ԫ
							callback_((char*)recvRtpDataBuf, recvDataLen, user_data_);
							//���
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
						memcpy(recvRtpDataBuf, headThreeBytes, 3); //������ʼ��
						recvDataLen += 3;

						memcpy(recvRtpDataBuf + recvDataLen, pPayData, nLen); //����payData
						recvDataLen += nLen;

						//��������Naul��Ԫ
						callback_((char*)recvRtpDataBuf, recvDataLen, user_data_);
						//���
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


//������������ص�
int RtpRecvData::SetPacketProcessCallBack(packet_output output, void* user_data)
{
	callback_ = output;

	user_data_ = user_data;

	return 0;
}

/*
* ����
*/
int RtpRecvData::Stop()
{
	stop = true;
	pThread->join();
	delete pThread;

	//WSACleanup();
	return 0;
};