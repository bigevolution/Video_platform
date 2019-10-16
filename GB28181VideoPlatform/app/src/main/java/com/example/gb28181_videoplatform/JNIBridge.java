package com.example.gb28181_videoplatform;

public class JNIBridge {
    // Used to load the library on application startup.

    static {
        System.loadLibrary("native-lib");
    }

    public final static int ROTATE_0_CROP_LF = 0;
    /**
     * 旋转90度剪裁左上
     */
    public final static int ROTATE_90_CROP_LT = 1;
    /**
     * 暂时没处理
     */
    public final static int ROTATE_180 = 2;
    /**
     * 旋转270(-90)裁剪左上，左右镜像
     */
    public final static int ROTATE_270_CROP_LT_MIRROR_LR = 3;


    public final static int UDP = 0;
    public final static int TCP = 1;
    public static final int FILE = 2;

    /**
     * @param remoteip   服务器ip
     * @param remotePort 端口
     * @param in_width   输入视频宽度
     * @param in_height  输入视频高度
     * @param frameRate  视频帧率
     * @param filter     旋转镜像剪切处理
     * @param bit_rate   视频比特率
     * @return
     */
    public static native int StartPushStream(
            String remoteip,
            int remotePort,
            int in_width,
            int in_height,
            int out_width,
            int out_height,
            int frameRate,
            int filter,
            long bit_rate,
            long ssrc,
            int audioFrameLen
    );

    /**
     * 发送视频数据
     *
     * @param data
     * @return
     */
    public static native int SendVideoFrame(byte[] data);

    /**
     * 发送音频数据
     *
     * @param data
     * @return
     */
    public static native int sendOneAudioFrame(byte[] data,int size);

    /**
     * 发送H264视频数据
     *
     * @param data
     * @return
     */
    public static native int SendH264VideoFrame(byte[] data, int size, int type);

    /**
     * 停止编码
     *
     * @return
     */
    public static native int endMux();


    /**
     * @param localIp   服务器ip
     * @param localPort 端口
     * @param in_width   输入视频宽度
     * @param in_height  输入视频高度
     * @return
     */
    public static native int StartPullStream(
            String localIp,
            int localPort,
            int in_width,
            int in_height
    );

    /**
     * 接收视频数据
     *
     */
    public static native byte[] ReceiveVideoFrame();


    /**
     * 停止编码
     *
     * @return
     */
    public static native int StopPullStream();
}
