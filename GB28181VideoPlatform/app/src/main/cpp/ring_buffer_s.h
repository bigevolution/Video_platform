/*
* ring_buffer_s.h
*/
#pragma once
#include <mutex>
#include "spin_mutex.h"

/**
* \brief �̰߳�ȫ�Ļ��λ�����
*/
class ring_buffer_s
{
public:
	ring_buffer_s(size_t capacity);
	ring_buffer_s(ring_buffer_s&&) = delete;
	ring_buffer_s& operator=(ring_buffer_s&& other) = delete;

	ring_buffer_s(const ring_buffer_s&) = delete;
	ring_buffer_s& operator=(const ring_buffer_s& other) = delete;

	~ring_buffer_s();

	/**
	 * \brief ��ȡ���������ô�С
	 */
	size_t size() const;

	/**
	 * \brief ��ȡ����������
	 */
	size_t capacity() const;

	/**
	 * \brief д������
	 * \param data Ҫд�������
	 * \param bytes Ҫд������ݵĴ�С
	 * \return ����д������ݵĴ�С
	 */
	size_t write(const void* data, size_t bytes);


	/**
	 * \brief ��ȡ����
	 * \param data ������Ŷ�ȡ���ݵ�buffer
	 * \param bytes Ҫ��ȡ�����ݴ�С
	 * \return ���ջ�ȡ�������ݵĴ�С
	 */
	size_t read(void* data, size_t bytes);

private:
	size_t front_, rear_, size_, capacity_;
	uint8_t* data_;
	mutable spin_mutex mut_;
	mutable std::mutex mut_read_;
	mutable std::mutex mut_write_;

};
