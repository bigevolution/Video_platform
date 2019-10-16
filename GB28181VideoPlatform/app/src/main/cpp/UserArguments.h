#pragma once

typedef struct {
	char *remoteip; //服务器ip
	int remotePort;  //服务器端口号
	int in_width;    //输入视频宽度
	int in_height;	 //输入视频高度
    int out_width;    //输出视频宽度
    int out_height;	 //输出视频高度
	int frame_rate;  //视频帧率
	long bit_rate;   //视频比特率
	long ssrc;		 //同步信源标识
	int filter;      //角度旋转
	int audioFrameLen; //音频最大长度
}UserArguments;

//拉流枚举类型
enum PullStreamType
{
	PULL_STREAM_H264, //h264流
	PULL_STREAM_PS	  //ps流
};

//播放参数结构体
typedef struct {
	char* localip;	 //服务端ip
	int localPort;   //服务端口号
	int in_width;    //视频宽度
	int in_height;	 //视频高度
	int frame_rate;  //视频帧率
	long bit_rate;   //视频比特率
	int ssrc;		 //同步信源标识
}PullStreamArgs;