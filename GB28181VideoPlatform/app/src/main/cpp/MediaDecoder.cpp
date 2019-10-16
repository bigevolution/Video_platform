#include "MediaDecoder.h"
#include "log.h"

/**
 * ��������
 */
MediaDecoder::~MediaDecoder()
{
}

/**
 *	������д�뻷�λ�����
 *  @param buffer:ָ��Naul��Ԫ��ָ��
 *  @param bytes:Naul��Ԫ�����ݴ�С
 *  @return 
 */
void MediaDecoder::Write(char* buffer, size_t bytes)
{
	if (!buffer || bytes == 0)
		return;

	while (ring_buffer_.capacity() - ring_buffer_.size() < bytes)
	{
		std::this_thread::sleep_for(std::chrono::milliseconds(10));
	}

	ring_buffer_.write(buffer, bytes);
}

/**
 *	�ӻ��λ�������ȡ����
 *  @param buffer:��Ŷ�ȡ���ݵ�ַ
 *  @param bytes:��ȡ���ݴ�С
 *  @return ʵ�ʶ�ȡ�Ĵ�С
 */
int MediaDecoder::Read(uint8_t* buf, int bufsize)
{
	int ret = 0;
	if (ring_buffer_.size() > 0)
	{
		bufsize = std::min(bufsize, (int)ring_buffer_.size());
		ret = ring_buffer_.read(buf, bufsize);
	}

	return ret;
}
