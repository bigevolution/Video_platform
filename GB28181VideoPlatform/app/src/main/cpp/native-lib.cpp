#include <jni.h>
#include <string>
#include <iostream>
#include <memory>
#include "PushH264Stream.h"
#include "log.h"
#include "UserArguments.h"
#include "PullMediaStream.h"

PushH264StreamPtr pMediaStream;

#define RECV_MAX_SIZE (2*1024 * 1024)
PullMediaStreamPtr pullStream;
uint8_t*  tbuf ;

FILE* pcmFile = NULL;
FILE* h264File = NULL;

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_StartPushStream(JNIEnv *env, jclass type, jstring remoteip_,
                                                   jint remotePort, jint in_width, jint in_height,
                                                   jint out_width, jint out_height,
                                                   jint frameRate, jint in_filter, jlong bit_rate,
                                                   jlong ssrc_,jint audioFrameLen_) {
    const char *remoteip = env->GetStringUTFChars(remoteip_, 0);
    UserArguments *arguments = (UserArguments *) malloc(sizeof(UserArguments));
    arguments->remoteip = (char *) malloc(strlen(remoteip) + 1);
    strcpy(arguments->remoteip, remoteip);
    arguments->remotePort = remotePort;
    arguments->in_width = in_width;
    arguments->in_height = in_height;
    arguments->out_width = out_width;
    arguments->out_height = out_height;
    arguments->frame_rate = frameRate;
    arguments->filter = in_filter;
    arguments->bit_rate = bit_rate;
    arguments->ssrc = ssrc_;
    arguments->audioFrameLen=audioFrameLen_;

    //pcmFile = fopen("/storage/emulated/0/DCIM/record.pcm", "wb+");
    h264File = fopen("/storage/emulated/0/DCIM/record.h264", "wb+");

    env->ReleaseStringUTFChars(remoteip_, remoteip);

    pMediaStream = std::make_shared<PushH264Stream>(arguments);
    return pMediaStream->StartPushStream();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_SendVideoFrame(JNIEnv *env, jclass type, jbyteArray data_) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    //
    env->ReleaseByteArrayElements(data_, data, 0);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_sendOneAudioFrame(JNIEnv *env, jclass type, jbyteArray data_,jint size_) {

    jbyte *data = env->GetByteArrayElements(data_, NULL);
    int i = pMediaStream->SendAudioFrame(reinterpret_cast<uint8_t *>(data), size_);

    //fwrite(reinterpret_cast<uint8_t *>(data), size_, 1, pcmFile);

    env->ReleaseByteArrayElements(data_, data, 0);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_endMux(JNIEnv *env, jclass type) {

    //fclose(pcmFile);
    //fclose(h264File);

    if(!pMediaStream) {
        pMediaStream->StopPushStream();
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_SendH264VideoFrame(JNIEnv *env, jclass type, jbyteArray data_,
                                                      jint size, jint streamtype) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    int i = pMediaStream->SendVideoFrame(reinterpret_cast<uint8_t *>(data), size);
    //fwrite(reinterpret_cast<uint8_t *>(data), size, 1, h264File);
    env->ReleaseByteArrayElements(data_, data, 0);
    return i;
}

/**
 *H264硬编码，推流初始化接口
 *@param env:JNI 接口指针
 *@param type:Java类对象
 *@param remoteip_:远程接收IP
 *@param remotePort:远程接收端口
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_InitPushStream(JNIEnv *env, jclass type, jstring remoteip_,
                                                  jint remotePort, jstring ssrc) {
    const char *remoteip = env->GetStringUTFChars(remoteip_, 0);
    UserArguments *arguments = (UserArguments *) malloc(sizeof(UserArguments));
    arguments->remoteip = (char *) malloc(strlen(remoteip) + 1);
    strcpy(arguments->remoteip, remoteip);
    arguments->remotePort = remotePort;

    LOGD("InitPushStream");

    pMediaStream = std::make_shared<PushH264Stream>(arguments);
    env->ReleaseStringUTFChars(remoteip_, remoteip);

    return pMediaStream->Init(96);
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_StartPullStream(JNIEnv *env, jclass type, jstring localip_,
                                                   jint localPort, jint in_width, jint in_height) {

    const char *localip = env->GetStringUTFChars(localip_, 0);
    PullStreamArgs *arguments = (PullStreamArgs *) malloc(sizeof(PullStreamArgs));
    arguments->localip = (char *) malloc(strlen(localip) + 1);
    strcpy(arguments->localip, localip);
    arguments->localPort = localPort;
    arguments->in_width = in_width;
    arguments->in_height = in_height;

    tbuf = new uint8_t[RECV_MAX_SIZE];
//    h264File = fopen("/storage/emulated/0/DCIM/mobile_receive.h264", "wb+");
    env->ReleaseStringUTFChars(localip_, localip);
    if (NULL==pullStream) {
        pullStream = std::make_shared<PullMediaStream>(arguments);
    }
    return pullStream->StartPullStream(localPort);
}

/**
 *接收视频帧
 * return:返回存放视频数据的数组
 */
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_ReceiveVideoFrame(JNIEnv *env, jclass type) {

    size_t realFrameSize = pullStream->ReadVideoFrame(tbuf, RECV_MAX_SIZE);
    //LOGD("ReceiveVideoFrame,addr=%p,size=%d",&tbuf,realFrameSize);
    jbyteArray frameData = env->NewByteArray(realFrameSize);
    env->SetByteArrayRegion(frameData, 0, realFrameSize, (jbyte*)tbuf);
//    fwrite(tbuf, realFrameSize, 1, h264File);
    return frameData;
}

/**
 *停止拉流
 * return:
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_gb28181_1videoplatform_JNIBridge_StopPullStream(JNIEnv *env, jclass type) {

    //停止接收数据
    if (NULL!=pullStream) {
        pullStream->StopPullStream();
    }

    //释放动态分配的地址空间
    if (NULL!=tbuf) {
        delete[] tbuf;
        tbuf=NULL;
    }
    LOGD("stop stream end");

    return 0;
}
