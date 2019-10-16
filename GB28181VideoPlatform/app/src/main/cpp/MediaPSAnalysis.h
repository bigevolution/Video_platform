#pragma once

union littel_endian_size {
	unsigned short int	length;
	unsigned char		byte[2];
};
struct pack_start_code {
	unsigned char start_code[3];
	unsigned char stream_id[1];
};
struct program_stream_pack_header {  //14�ֽ�
	pack_start_code PackStart;// 4
	unsigned char Buf[9];
	unsigned char stuffinglen;
};
struct program_stream_system_head {  //������ϵͳͷ��pack_start_codeΪ00 00 01 BB
	pack_start_code PackStart;
	littel_endian_size PackLength;
};
struct program_stream_map {
	pack_start_code PackStart;
	littel_endian_size PackLength;
};
struct program_stream_e {
	pack_start_code		PackStart; //4�ֽ�
	littel_endian_size	PackLength;//we mast do exchange  2�ֽ�
	char				PackInfo1[2]; //2�ֽ�
	unsigned char		stuffing_length; //1�ֽ�
};

class MediaPSAnalysis
{
public:
	MediaPSAnalysis();
	~MediaPSAnalysis();
public:
	/* ��ȡMediaManage����Ψһʵ�� */
	static MediaPSAnalysis* getInstance();

	/* ��ȡh264���� */
	int GetH246FromPs(unsigned char* buffer, int length, unsigned char* h264Buffer, int* h264length);

private:
	/* ����psͷ */
	int inline ProgramStreamPackHeader(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength);

	/* ȥ��ϵͳͷ */
	inline int ProgramStreamSystemHead(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength);

	/* ȥ��programstreammap */
	inline int ProgramStreamMap(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength, unsigned char** PayloadData, int* PayloadDataLen);

	/* ��pes�� */
	inline int Pes(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength, unsigned char** PayloadData, int* PayloadDataLen);
private:
	static MediaPSAnalysis* p;
};



