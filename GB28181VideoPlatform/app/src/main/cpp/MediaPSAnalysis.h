#pragma once

union littel_endian_size {
	unsigned short int	length;
	unsigned char		byte[2];
};
struct pack_start_code {
	unsigned char start_code[3];
	unsigned char stream_id[1];
};
struct program_stream_pack_header {  //14字节
	pack_start_code PackStart;// 4
	unsigned char Buf[9];
	unsigned char stuffinglen;
};
struct program_stream_system_head {  //可能有系统头，pack_start_code为00 00 01 BB
	pack_start_code PackStart;
	littel_endian_size PackLength;
};
struct program_stream_map {
	pack_start_code PackStart;
	littel_endian_size PackLength;
};
struct program_stream_e {
	pack_start_code		PackStart; //4字节
	littel_endian_size	PackLength;//we mast do exchange  2字节
	char				PackInfo1[2]; //2字节
	unsigned char		stuffing_length; //1字节
};

class MediaPSAnalysis
{
public:
	MediaPSAnalysis();
	~MediaPSAnalysis();
public:
	/* 获取MediaManage对象唯一实例 */
	static MediaPSAnalysis* getInstance();

	/* 获取h264数据 */
	int GetH246FromPs(unsigned char* buffer, int length, unsigned char* h264Buffer, int* h264length);

private:
	/* 解析ps头 */
	int inline ProgramStreamPackHeader(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength);

	/* 去掉系统头 */
	inline int ProgramStreamSystemHead(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength);

	/* 去掉programstreammap */
	inline int ProgramStreamMap(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength, unsigned char** PayloadData, int* PayloadDataLen);

	/* 解pes包 */
	inline int Pes(unsigned char* Pack, int length, unsigned char** NextPack, int* leftlength, unsigned char** PayloadData, int* PayloadDataLen);
private:
	static MediaPSAnalysis* p;
};



