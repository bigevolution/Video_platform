#pragma once

#include <iostream>

#define BUF2U16(buf)	(((buf)[0] << 8) | (buf)[1])
//ÿ����Ŀ������
#define MAX_STREAM_NUM		(4)

/************************************************************************/
/* ���洦�����ӿ�                                                     */
/************************************************************************/
typedef struct
{
	int buf_size;
	int (*input)(uint8_t* buf, int size, void* context);
	int (*output)(uint8_t* buf, int size, void* context);
	void* context;
} TBufferHandler;

/************************************************************************/
/* ��Ŀ��Ϣ����                                                         */
/************************************************************************/
// ÿ����������
typedef struct
{
	uint8_t type;			// [I]ý������
	uint8_t stream_id;		// [O]ʵ����ID����PESͷ��id��ͬ��
	int es_pid;				// [O]ʵ������PID
	int continuity_counter;	// [O] TS��ͷ��������������, �ⲿ��Ҫά���������ֵ, ����ÿ�δ����ϴδ����ļ���ֵ
} TsStreamSpec;

typedef struct
{
	int stream_num;			// [I]�����Ŀ������������
	int key_stream_id;		// {I]��׼�����
	int pmt_pid;			// [O]�����Ŀ��Ӧ��PMT���PID��TS�����ã�
	int mux_rate;			// [O]�����Ŀ�����ʣ���λΪ50�ֽ�ÿ��(PS������)
	TsStreamSpec stream[MAX_STREAM_NUM];
} TsProgramSpec;

// ��Ŀ��Ϣ��Ŀǰ���֧��1����Ŀ2������
#define MAX_PROGRAM_NUM		(1)
typedef struct
{
	int program_num;		// [I]���TS�������Ľ�Ŀ����������PS��ֵֻ��Ϊ1
	int pat_pmt_counter;	// [O]PAT��PMT������
	TsProgramSpec prog[MAX_PROGRAM_NUM];
} TsProgramInfo;

//PS����ṹ��
typedef struct
{
	TsProgramInfo info;		// ��Ŀ��Ϣ
	int is_pes;				// �������ݣ�����PSI
	int pid;				// ��ǰ����PID
	int program_no;			// ��ǰ�������Ľ�Ŀ��
	int stream_no;			// ��ǰ������������
	uint64_t pts;			// ��ǰ����ʱ���
	uint64_t pes_pts;		// ��ǰPES��ʱ���
	uint8_t* pack_ptr;		// ���һ�����׵�ַ
	int pack_len;			// ���һ���ĳ���
	uint8_t* es_ptr;		// ES�����׵�ַ
	int es_len;				// ES���ݳ���
	int pes_head_len;		// PESͷ������
	int sync_only;			// ֻͬ��������������
	int ps_started;			// ���ҵ�PSͷ��
} TDemux;


/*********************************/
/* PS define:                    */
/********************************/
typedef struct
{
	char start_code[4];				// == 0x000001BA
	char scr[6];
	char program_mux_rate[3];		// == 0x000003
	char pack_stuffing_length;		// == 0xF8
}ps_pack_header;

// PSM
typedef struct
{
	char start_code[4];				// == 0x000001BC
	char header_length[2];			// == 6 + es_map_length
	char ps_map_version : 5,			// == 0
		reserved1 : 2,				// == 3
		current_next_indicator : 1;	// == 1
	char marker_bit : 1,				// == 1
		reserved2 : 7;				// == 127
	char ps_info_length[2];			// == 0
	char es_map_length[2];			// == 4 * es_num
}ps_map;

typedef struct
{
	char stream_type;
	char es_id;
	char es_info_length[2];			// == 0
}ps_map_es;