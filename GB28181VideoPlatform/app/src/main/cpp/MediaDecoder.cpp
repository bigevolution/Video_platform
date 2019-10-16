#include "MediaDecoder.h"
#include "log.h"

/**
 * 析构函数
 */
MediaDecoder::~MediaDecoder()
{
}

/**
 *	将数据写入环形缓冲区
 *  @param buffer:指向Naul单元的指针
 *  @param bytes:Naul单元的数据大小
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
 *	从环形缓冲区读取数据
 *  @param buffer:存放读取数据地址
 *  @param bytes:读取数据大小
 *  @return 实际读取的大小
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
