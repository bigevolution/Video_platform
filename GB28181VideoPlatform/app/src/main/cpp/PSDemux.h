#pragma once

#include <iostream>
#include <stdio.h>
#include <string.h>
#include "StreamDef.h"
using namespace std;

class PSDemux
{
public:
	PSDemux();
	~PSDemux();
public:
	//PS Demux
	int ps_demux_output(uint8_t* buf, int size, TDemux &g_demux);

private:

	//PS解码
	int lts_ps_demux(TDemux* handle, uint8_t* ps_buf, int len);

	// PS header
	int handle_header(TDemux* handle, uint8_t* buf, int len);

	// PS system header
	int handle_system_header(TDemux* handle, uint8_t* buf, int len);

	// PS map
	int handle_map(TDemux* handle, uint8_t* buf, int len);

	// PS finish
	int handle_finish(TDemux* handle, uint8_t* buf, int len);

	// PES
	int handle_pes(TDemux* handle, uint8_t* buf, int len);

	//
	int handle_common_pack(uint8_t* buf, int len);

	//解析PES头部长度，返回头部总长度
	int lts_pes_parse_header(uint8_t* pes, int len, uint8_t* stream_id, uint64_t* pts, int* es_len);

	//解析PES总长
	int get_pes_head_len(uint8_t* pes, int len);

};

