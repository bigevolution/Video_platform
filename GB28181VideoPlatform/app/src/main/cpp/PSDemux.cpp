#include "PSDemux.h"

/**
 *���캯��
*/
PSDemux::PSDemux() {
}

/**
 *��������
*/
PSDemux::~PSDemux() {
}

/**
 * PS���װ
 *@param buf:
 *@param size:
*/
int PSDemux::ps_demux_output(uint8_t* buf, int size, TDemux& g_demux)
{
	int len = 0;
	len = lts_ps_demux(&g_demux, buf, size);

	return len;
}

/**
 * PS����
 *@param handle:
 *@param ps_buf:
 *@param len:
*/
int PSDemux::lts_ps_demux(TDemux* handle, uint8_t* ps_buf, int len)
{
	int i = 0;
	int ret_len = 0;

	if (!handle || !ps_buf || len <= 0)
	{
		return -1;
	}

	handle->is_pes = 0;
	handle->program_no = 0;
	handle->stream_no = 0;
	handle->pts = 0;
	handle->pack_ptr = nullptr;
	handle->pack_len = 0;
	handle->pes_head_len = 0;
	handle->es_ptr = nullptr;
	handle->es_len = 0;

	for (i = 0; i < len - 4; i++)
	{
		ret_len = i;

		if (ps_buf[i] == 0 && ps_buf[i + 1] == 0 && ps_buf[i + 2] == 1)
		{
			int type = ps_buf[i + 3];

			/*
			PES stream id table:
			stream_id	stream coding
			1011 1100	program_stream_map
			1011 1101	private_stream_1
			1011 1110	padding_stream
			1011 1111	private_stream_2
			110x xxxx	ISO/IEC 13818-3 or ISO/IEC 11172-3 or ISO/IEC 13818-7 or ISO/IEC14496-3 audio stream number x xxxx
			1110 xxxx	ITU-T Rec. H.262 | ISO/IEC 13818-2 or ISO/IEC 11172-2 or ISO/IEC14496-2 video stream number xxxx
			1111 0000	ECM_stream
			1111 0001	EMM_stream
			1111 0010	ITU-T Rec. H.222.0 | ISO/IEC 13818-1 Annex A or ISO/IEC 13818-6_DSMCC_stream
			1111 0011	ISO/IEC_13522_stream
			1111 0100	ITU-T Rec. H.222.1 type A
			1111 0101	ITU-T Rec. H.222.1 type B
			1111 0110	ITU-T Rec. H.222.1 type C
			1111 0111	ITU-T Rec. H.222.1 type D
			1111 1000	ITU-T Rec. H.222.1 type E
			1111 1001	ancillary_stream
			1111 1010	ISO/IEC14496-1_SL-packetized_stream
			1111 1011	ISO/IEC14496-1_FlexMux_stream
			1111 11xx	reserved data stream
			1111 1111	program_stream_directory
			*/
			// PS stream_id�������0xB9
			if (type < 0xB9)
			{
				continue;
			}
			// �����û�ҵ�psͷ
			if (!handle->ps_started)
			{
				// �����һ������psͷ
				if (type != 0xBA)
				{
					// ��ô�Թ�������������Ѱ
					continue;
				}

				// ֱ���ҵ�psͷ
				handle->ps_started = 1;
			}

			// ͬ��
			if (handle->sync_only)
			{
				switch (type)
				{
				case 0xBA:
					// PS header
					ret_len = handle_header(handle, ps_buf + i, len - i);
					break;

				case 0xB9:
					// PS finish
					ret_len = handle_finish(handle, ps_buf + i, len - i);
					break;

				default:
					// PES
					ret_len = handle_common_pack(ps_buf + i, len - i);
					break;
				}
			}
			// �⸴�ã������ϸ��ý����Ϣ
			else
			{
				switch (type)
				{
				case 0xBA:
					// PS header
					ret_len = handle_header(handle, ps_buf + i, len - i);
					break;

				case 0xBB:
					// PS system header
					ret_len = handle_system_header(handle, ps_buf + i, len - i);
					break;

				case 0xBC:
					// PS map
					ret_len = handle_map(handle, ps_buf + i, len - i);
					break;

				case 0xB9:
					// PS finish
					ret_len = handle_finish(handle, ps_buf + i, len - i);
					break;

				default:
					// PES
					ret_len = handle_pes(handle, ps_buf + i, len - i);
					break;
				}
			}

			// ���һ���ͷ���
			break;
		}
	}

	if (!handle->ps_started) {
		return -1;
	}

	// ����������볤�ȣ�����0���´��ٽ���
	if (i + ret_len > len)
	{
		return 0;
	}

	handle->pack_ptr = ps_buf + i;
	handle->pack_len = ret_len;
	return i + ret_len;
}

/**
 * ȥ��PS֡�е�PSͷ
 *@param handle:
 *@param buf:
 *@param len:
*/
int PSDemux::handle_header(TDemux* handle, uint8_t* buf, int len)
{
	int psize = 0;
	int staffing_len = 0;
	ps_pack_header* head = (ps_pack_header*)buf;

	if (len < sizeof(ps_pack_header))
	{
		return 0;
	}

	staffing_len = head->pack_stuffing_length & 0x07;
	psize = sizeof(ps_pack_header) + staffing_len;
	if (len < psize)
	{
		return 0;
	}

	//do somethings future...
	return psize;
}

/**
 * ȥ��PS֡�е�ϵͳͷ
 *@param handle:
 *@param buf:
 *@param len:
*/
int PSDemux::handle_system_header(TDemux* handle, uint8_t* buf, int len)
{
	int psize = handle_common_pack(buf, len);

	if (psize > 0)
	{
		//ps_system_header *head = (ps_system_header *)buf;
	    // do somethings future...
	}

	return psize;
}

/**
 * PS finish
 *@param handle:
 *@param buf:
 *@param len:
*/
int PSDemux::handle_map(TDemux* handle, uint8_t* buf, int len)
{
	int psize = handle_common_pack(buf, len);

	if (psize > 0)
	{
		ps_map* head = (ps_map*)buf;
		int info_len = BUF2U16(head->ps_info_length);
		int map_len = BUF2U16(head->es_map_length + info_len);
		uint8_t* map_buf = buf + sizeof(ps_map) + info_len;
		int pos = 0;
		int i = 0;

		handle->info.program_num = 1;
		while (pos < map_len)
		{
			ps_map_es* mes = (ps_map_es*)(map_buf + pos);
			int es_info_len = BUF2U16(mes->es_info_length);
			if (es_info_len < 0)
				break;

			if (i >= MAX_STREAM_NUM)
			{
				break;
			}

			pos += (sizeof(ps_map_es) + es_info_len);

			// ���������Ƶ����Ƶ
			if ((mes->es_id & 0xC0) == 0xC0 || (mes->es_id & 0xE0) == 0xE0)
			{

				handle->info.prog[0].stream[i].type = mes->stream_type;
				handle->info.prog[0].stream[i].stream_id = mes->es_id;
				i++;
			}
		}
		handle->info.prog[0].stream_num = i;
	}

	return psize;
}

/**
 * PES
 *@param handle:
 *@param buf:
 *@param len:
*/
int PSDemux::handle_pes(TDemux* handle, uint8_t* buf, int len)
{
	int psize = handle_common_pack(buf, len);

	if (psize > 0)
	{
		uint8_t stream_id;
		uint64_t pts;
		int es_len;
		int pes_head_len = lts_pes_parse_header(buf, len, &stream_id, &pts, &es_len);
		if (pes_head_len > 0)
		{
			// �ж��Ƿ���map���е�es
			if (handle->info.program_num == 1)
			{
				int i;
				for (i = 0; i < handle->info.prog[0].stream_num; i++)
				{
					if (stream_id == handle->info.prog[0].stream[i].stream_id)
					{
						// ��PES������д��Ŀ�ź����ţ��Լ�������Ϣ
						handle->is_pes = 1;
						handle->program_no = 0;
						handle->stream_no = i;
						handle->pts = pts;
						handle->pes_head_len = pes_head_len;
						handle->es_ptr = buf + pes_head_len;
						handle->es_len = es_len;
						break;
					}
				}
			}
		}

		// do somethings future...
	}

	return psize;
}

/**
 * PES
 *@param handle:
 *@param buf:
 *@param len:
*/
int PSDemux::handle_finish(TDemux* handle, uint8_t* buf, int len)
{
	int psize = 4;
	return psize;
}



/**
 * pack common
 *@param buf:
 *@param len:
*/
int PSDemux::handle_common_pack(uint8_t* buf, int len)
{
	int psize = 0;

	if (len < 6)
	{
		return 0;
	}

	psize = 6 + BUF2U16(&buf[4]);
	if (len < psize)
	{
		return 0;
	}

	return psize;
}

/**
 * ����PESͷ�����ȣ�����ͷ���ܳ���
 *@param pes:
 *@param len:
 *@param stream_id:
 *@param pts:
 *@param es_len:
*/
int PSDemux::lts_pes_parse_header(uint8_t* pes, int len, uint8_t* stream_id, uint64_t* pts, int* es_len)
{
	int pes_head_len = get_pes_head_len(pes, len);
	if (pes_head_len <= 0)
	{
		return 0;
	}

	if (stream_id)
	{
		*stream_id = pes[3];
	}

	// ����PTS
	if (pts)
	{
		uint8_t flags_2 = pes[7];
		if (flags_2 & 0x80)
		{
			uint8_t* pts_buf = &pes[9];
			*pts = ((uint64_t)pts_buf[0] & 0x0E) << 29;
			*pts |= ((uint64_t)pts_buf[1]) << 22;
			*pts |= ((uint64_t)pts_buf[2] & 0xFE) << 14;
			*pts |= ((uint64_t)pts_buf[3]) << 7;
			*pts |= ((uint64_t)pts_buf[4] & 0xFE) >> 1;
			*pts /= 90;
		}
	}

	// ����ES����
	if (es_len)
	{
		int pes_len = BUF2U16(&pes[4]);
		int pes_head_len = pes[8];
		*es_len = pes_len - 3 - pes_head_len;
	}

	return pes_head_len;
}

/**
 * ����PES�ܳ�
 *@param pes:
 *@param len:
*/
int PSDemux::get_pes_head_len(uint8_t* pes, int len)
{
	int pes_head_len = 0;

	if (len < 9)
	{
		return 0;
	}

	if (pes[0] == 0 && pes[1] == 0 && pes[2] == 1)
	{
		if ((pes[3] & 0xC0) || (pes[3] & 0xE0))
		{
			pes_head_len = 9 + pes[8];
		}
	}

	return pes_head_len;
}