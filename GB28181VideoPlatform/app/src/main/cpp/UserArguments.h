#pragma once

typedef struct {
	char *remoteip; //������ip
	int remotePort;  //�������˿ں�
	int in_width;    //������Ƶ���
	int in_height;	 //������Ƶ�߶�
    int out_width;    //�����Ƶ���
    int out_height;	 //�����Ƶ�߶�
	int frame_rate;  //��Ƶ֡��
	long bit_rate;   //��Ƶ������
	long ssrc;		 //ͬ����Դ��ʶ
	int filter;      //�Ƕ���ת
	int audioFrameLen; //��Ƶ��󳤶�
}UserArguments;

//����ö������
enum PullStreamType
{
	PULL_STREAM_H264, //h264��
	PULL_STREAM_PS	  //ps��
};

//���Ų����ṹ��
typedef struct {
	char* localip;	 //�����ip
	int localPort;   //����˿ں�
	int in_width;    //��Ƶ���
	int in_height;	 //��Ƶ�߶�
	int frame_rate;  //��Ƶ֡��
	long bit_rate;   //��Ƶ������
	int ssrc;		 //ͬ����Դ��ʶ
}PullStreamArgs;