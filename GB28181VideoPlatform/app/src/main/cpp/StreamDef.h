#pragma once

#include <iostream>

#define BUF2U16(buf)	(((buf)[0] << 8) | (buf)[1])
//每个节目的详情
#define MAX_STREAM_NUM		(4)

/************************************************************************/
/* 缓存处理辅助接口                                                     */
/************************************************************************/
typedef struct
{
	int buf_size;
	int (*input)(uint8_t* buf, int size, void* context);
	int (*output)(uint8_t* buf, int size, void* context);
	void* context;
} TBufferHandler;

/************************************************************************/
/* 节目信息定义                                                         */
/************************************************************************/
// 每条流的详情
typedef struct
{
	uint8_t type;			// [I]媒体类型
	uint8_t stream_id;		// [O]实体流ID（与PES头部id相同）
	int es_pid;				// [O]实体流的PID
	int continuity_counter;	// [O] TS包头部的连续计数器, 外部需要维护这个计数值, 必须每次传入上次传出的计数值
} TsStreamSpec;

typedef struct
{
	int stream_num;			// [I]这个节目包含的流个数
	int key_stream_id;		// {I]基准流编号
	int pmt_pid;			// [O]这个节目对应的PMT表的PID（TS解码用）
	int mux_rate;			// [O]这个节目的码率，单位为50字节每秒(PS编码用)
	TsStreamSpec stream[MAX_STREAM_NUM];
} TsProgramSpec;

// 节目信息（目前最多支持1个节目2条流）
#define MAX_PROGRAM_NUM		(1)
typedef struct
{
	int program_num;		// [I]这个TS流包含的节目个数，对于PS该值只能为1
	int pat_pmt_counter;	// [O]PAT、PMT计数器
	TsProgramSpec prog[MAX_PROGRAM_NUM];
} TsProgramInfo;

//PS解码结构体
typedef struct
{
	TsProgramInfo info;		// 节目信息
	int is_pes;				// 属于数据，不是PSI
	int pid;				// 当前包的PID
	int program_no;			// 当前包所属的节目号
	int stream_no;			// 当前包所属的流号
	uint64_t pts;			// 当前包的时间戳
	uint64_t pes_pts;		// 当前PES的时间戳
	uint8_t* pack_ptr;		// 解出一包的首地址
	int pack_len;			// 解出一包的长度
	uint8_t* es_ptr;		// ES数据首地址
	int es_len;				// ES数据长度
	int pes_head_len;		// PES头部长度
	int sync_only;			// 只同步包，不解析包
	int ps_started;			// 已找到PS头部
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