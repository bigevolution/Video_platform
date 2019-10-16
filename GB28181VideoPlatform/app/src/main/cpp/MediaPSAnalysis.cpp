#include "log.h"
#include "MediaPSAnalysis.h"
#include <string.h>
#include <stdio.h>
#include <iostream>
using namespace std;


/*���캯��*/
MediaPSAnalysis::MediaPSAnalysis()
{

}

/* �������� */
MediaPSAnalysis::~MediaPSAnalysis()
{

}

// ����ʵ��ָ��
MediaPSAnalysis* MediaPSAnalysis::p = NULL;


/* ��ȡMediaPsAnaly����Ψһʵ�� */
MediaPSAnalysis* MediaPSAnalysis::getInstance() {
	if (p == NULL) {
		p = new MediaPSAnalysis();
	}
	return p;
}

/* ����psͷ */
int inline MediaPSAnalysis::ProgramStreamPackHeader(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength)
{
	program_stream_pack_header* PsHead = (program_stream_pack_header*)Pack;
	unsigned char pack_stuffing_length = PsHead->stuffinglen & '\x07';
	*leftlength = length - sizeof(program_stream_pack_header) - pack_stuffing_length;//ʣ���ֽڣ�length��ȥͷ�������ֽ�
	*NextPack = Pack + sizeof(program_stream_pack_header) + pack_stuffing_length;
	if (*leftlength < 4) return 0; //�޺���ͷ��������

    LOGI("ProgramStreamPackHeader:%d",*leftlength);
	return *leftlength;
}

/* ȥ��ϵͳͷ */
inline int MediaPSAnalysis::ProgramStreamSystemHead(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength)
{
	program_stream_system_head* PssHead = (program_stream_system_head*)Pack;
	littel_endian_size pssh_length;
	pssh_length.byte[0] = PssHead->PackLength.byte[1];
	pssh_length.byte[1] = PssHead->PackLength.byte[0];
	*leftlength = length - pssh_length.length - sizeof(program_stream_system_head);
	*NextPack = Pack + sizeof(program_stream_system_head) + pssh_length.length;
	if (*leftlength < 4) return 0;

    LOGI("ProgramStreamSystemHead:%d",*leftlength);
	return *leftlength;
}

/* ȥ��programstreammap */
inline int MediaPSAnalysis::ProgramStreamMap(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength, 
	unsigned char** PayloadData, int* PayloadDataLen) {

	program_stream_map* PSMPack = (program_stream_map*)Pack;
	*PayloadData = 0;
	*PayloadDataLen = 0;
	if (length < sizeof(program_stream_map)) return 0;
	littel_endian_size psm_length;
	psm_length.byte[0] = PSMPack->PackLength.byte[1];
	psm_length.byte[1] = PSMPack->PackLength.byte[0];
	*leftlength = length - psm_length.length - sizeof(program_stream_map);
	if (*leftlength <= 0) return 0;
	*NextPack = Pack + psm_length.length + sizeof(program_stream_map);

    LOGI("ProgramStreamMap:%d",*leftlength);
	return *leftlength;
}

/* ��pes�� */
inline int MediaPSAnalysis::Pes(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength, 
	unsigned char** PayloadData, int* PayloadDataLen) {

	program_stream_e* PSEPack = (program_stream_e*)Pack;
	*PayloadData = 0;
	*PayloadDataLen = 0;
	if (length < sizeof(program_stream_e)) return 0;
	
	littel_endian_size pse_length;
	pse_length.byte[0] = PSEPack->PackLength.byte[1];
	pse_length.byte[1] = PSEPack->PackLength.byte[0];
	//pse_length.byte[4] = PSEPack->stuffing_length;

	*PayloadDataLen = pse_length.length - 2 - 1 - PSEPack->stuffing_length;

	if (*PayloadDataLen > 0) {
		*PayloadData = Pack + sizeof(program_stream_e) + PSEPack->stuffing_length;
	}
	*leftlength = length - pse_length.length - sizeof(pack_start_code) - sizeof(littel_endian_size);
	if (*leftlength <= 0) return 0;
	*NextPack = Pack + sizeof(pack_start_code) + sizeof(littel_endian_size) + pse_length.length;

    LOGI("Pes:%d",*leftlength);
	return *leftlength;
}

/* ��ȡh264���� */
int MediaPSAnalysis::GetH246FromPs(unsigned char* buffer, int length, unsigned char* h264Buffer, int* h264length) {

    LOGI("length of PS:%d",length);

	/*
    unsigned char psbuffer_[8000] = {0};
    for(int i = 0; i<length;i++ )
    {
        psbuffer_[i] = buffer[i];
    }
	*/

	int leftlength = 0;
	unsigned char* NextPack = 0;
	*h264length = 0;

	if (ProgramStreamPackHeader(buffer, length, &NextPack, &leftlength) == 0) {//����PSͷ
		return 0;
	}

	unsigned char* PayloadData = NULL;
	int PayloadDataLen = 0;
	if (NextPack && NextPack[0] == 0 && NextPack[1] == 0 && NextPack[2] == 1 && NextPack[3] == 0xbb) {//��ϵͳͷ�Ļ���������
		if (ProgramStreamSystemHead(NextPack, leftlength, &NextPack, &leftlength) == 0)
			return 0;
	}

    /*
	if (NextPack && NextPack[0] == 0 && NextPack[1] == 0 && NextPack[2] == 1 && NextPack[3] == 0xbc) {//Program Stream map��ֱ�������ð�
		if (ProgramStreamMap(NextPack, leftlength, &NextPack, &leftlength, &PayloadData, &PayloadDataLen) == 0)
			return 0;
	}
    */

	//printf("[%s] %x %x %x %x\n", __FUNCTION__, NextPack[0], NextPack[1], NextPack[2], NextPack[3]);
	while (leftlength >= sizeof(pack_start_code))  //����pes����Ŀǰ��֪��pes����������Program Stream map��H264
	{
		PayloadData = NULL;
		PayloadDataLen = 0;
		if (NextPack && NextPack[0] == 00 && NextPack[1] == 00 && NextPack[2] == 01 && NextPack[3] == 0xE0) {  //H264����Ҫ��ȡ��pes������Ч�غɣ���H264����
			//���ž���������˵���Ƿ�i֡�����м����Program Stream map������i֡�� ��ȡH264��ʱ������Ҫ����i֡���i֡
			if (Pes(NextPack, leftlength, &NextPack, &leftlength, &PayloadData, &PayloadDataLen)) {
				if (PayloadDataLen) {
					memcpy(h264Buffer, PayloadData, PayloadDataLen);
					h264Buffer += PayloadDataLen;
					*h264length += PayloadDataLen;
				}
			}
			else {//˵�������һ��pes��
				if (PayloadDataLen) {
					memcpy(h264Buffer, PayloadData, PayloadDataLen);
					h264Buffer += PayloadDataLen;
					*h264length += PayloadDataLen;
				}

				/*
				unsigned char pbuffer_[8000] = {0};
				for(int i = 0; i<(*h264length);i++ )
				{
					pbuffer_[i] = h264Buffer[i];
				}
				 */

				break;
			}
		}
		else if (NextPack && NextPack[0] == 00 && NextPack[1] == 00 && NextPack[2] == 01 && NextPack[3] == 0xBC) {//Program Stream map��ֱ�������ð�
			if (ProgramStreamMap(NextPack, leftlength, &NextPack, &leftlength, &PayloadData, &PayloadDataLen) == 0) {
				break;
			}
		}
		else {
			//printf("[%s]no konw %02x %02x %02x %02x %02x %02x %02x %02x\n", __FUNCTION__, NextPack[0], NextPack[1], NextPack[2], NextPack[3],NextPack[4], NextPack[5], NextPack[6], NextPack[7]);
			break;
		}
	}

	/*
    unsigned char p264buffer_[8000] = {0};
    for(int i = 0; i<(*h264length);i++ )
    {
        p264buffer_[i] = h264Buffer[i];
    }
    */

    LOGI("H264:%d",*h264length);
	return *h264length;
}

