package com.example.gb28181_videoplatform.util;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import com.example.gb28181_videoplatform.JNIBridge;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;


public class AvcEncoder2 {
    private final static String TAG = "MeidaCodec";
    private int TIMEOUT_USEC = 12000;
    private MediaCodec mediaCodec;
    private int m_width;
    private int m_height;
    private int m_framerate;
    private int m_payloadType;
    public byte[] configbyte;//配置相关的内容,SPS，PPS
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private BufferedOutputStream outputStream_File;
    public static int yuvqueuesize = 100;
    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.h264";
    MyMediaCodec myMediaCode = new MyMediaCodec();
    private int colorFormat ;//颜色格式

    @SuppressLint("NewApi")
    public AvcEncoder2(int width, int height, int framerate, int bitrate, int payloadType) {
        //动态检查手机支持的MediaCodec编解码颜色格式
        colorFormat = myMediaCode.getMediaCodecList();
        m_width = width;
        m_height = height;
        m_framerate = framerate;
        m_payloadType = payloadType;
        Log.e(TAG, "width==" + width + "height==" + height + "m_framerate==" + m_framerate + "birte====" + m_framerate * width * height / 15);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        // 码率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, m_framerate * width * height / 15);
        // 设置帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, m_framerate);
        // 设置 I 帧间隔
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //配置编码器参数
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //启动编码器
        mediaCodec.start();
        //创建保存编码后数据的文件
        createfile();
    }


    private void createfile() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream_File = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void StopEncoder() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
            //mediaCodec = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //ByteBuffer[] inputBuffers;
    //ByteBuffer[] outputBuffers;

    public volatile boolean isRuning = false;

    public void StopThread() {
        isRuning = false;
        try {
            StopEncoder();
            outputStream_File.flush();
            outputStream_File.close();
            YUVQueue.clear();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void StartEncoderThread() {
        Thread EncoderThread = new Thread(new Runnable() {

            @SuppressLint("NewApi")
            @Override
            public void run() {
                isRuning = true;
                byte[] input = null;
                long pts = 0;
                long generateIndex = 0;
                while (isRuning) {
                    //访问MainActivity用来缓冲待解码数据的队列
                    if (YUVQueue.size() > 0) {
                        //从缓冲队列中取出一帧
                        input = YUVQueue.poll();
                        byte[] yuv420 = new byte[m_width * m_height * 3 / 2];
                        if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                            swapYV12toI420(input, yuv420, m_width, m_height);
                        } else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                            swapYV12toNV12(input, yuv420, m_width, m_height);
                        }
                        input = yuv420;
                    }
                    if (input != null) {
                        try {
                            long startMs = System.currentTimeMillis();
                            //编码器输入缓冲区
                            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                            //编码器输出缓冲区
                            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0) {
                                pts = computePresentationTime(generateIndex);
                                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                                inputBuffer.clear();
                                //把转换后的YUV420格式的视频帧放到编码器输入缓冲区中
                                inputBuffer.put(input);
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                                generateIndex += 1;
                            }
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            //Log.e("gaozy", "outputBufferIndex=====" + outputBufferIndex + "");
                            while (outputBufferIndex >= 0) {
                                //Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
                                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                                byte[] outData = new byte[bufferInfo.size];
                                outputBuffer.get(outData);
//                                if (bufferInfo.flags == 2) {
//                                    configbyte = new byte[bufferInfo.size];
//                                    configbyte = outData;
//                                } else if (bufferInfo.flags == 1) {
//                                    byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
//                                    System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
//                                    //把编码后的视频帧从编码器输出缓冲区中拷贝出来
//                                    System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
//                                    //outputStream.write(keyframe, 0, keyframe.length);
//                                    outputStream.write(keyframe);
//                                    //写到文件中
//                                    saveH264ToFile(outputStream);
//                                } else {
//                                    //outputStream.write(outData, 0, outData.length);
//                                    outputStream.write(outData);
//                                    //写到文件中
//                                    saveH264ToFile(outputStream);
//                                }
                                // flags 利用位操作，定义的 flag 都是 2 的倍数
                                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) { // 配置相关的内容，也就是 SPS，PPS
                                    configbyte = new byte[bufferInfo.size];//先保存SPS,PPS，后面要使用
                                    configbyte = outData;
                                    //outputStream.write(outData, 0, outData.length);
                                    //saveH264ToFile(outputStream);
                                } else if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) { // 关键帧加上sps，pps
                                    byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                                    System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                                    System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                                    outputStream.write(keyframe);
                                    saveH264ToFile(outputStream);
                                } else {
                                    // 非关键帧和SPS、PPS,直接写入文件，可能是B帧或者P帧
                                    outputStream.write(outData, 0, outData.length);
                                    saveH264ToFile(outputStream);
                                }
                                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        EncoderThread.start();
    }


    private void saveH264ToFile(ByteArrayOutputStream outputStream_put) {
        final byte[] ret = outputStream_put.toByteArray();
        outputStream_put.reset();
//        try {
//            outputStream_File.write(ret, 0, ret.length);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //发送数据到C++
        JNIBridge.SendH264VideoFrame(ret, ret.length, m_payloadType);
    }

//*********
//     I420: YYYYYYYY UU VV    =>YUV420P
//     YV12: YYYYYYYY VV UU    =>YUV420P
//     NV12: YYYYYYYY UVUV     =>YUV420SP
//     NV21: YYYYYYYY VUVU     =>YUV420SP
//     Android COLOR_FormatYUV420Planar   即YUV420P，也就是说它要求的传给编码器的数据格式为: YYYYYYYY UU VV (I420)
//     Android COLOR_FormatYUV420SemiPlanar 即YUV420SP，它要求传给编码器的数据格式为: YYYYYYYY UVUV(NV12)

    /**
     * nv21ToI420(YUV420P)
     *
     * @param data
     * @param I420
     * @param width
     * @param height
     * @return
     */
    public byte[] nv21ToI420(byte[] data, byte[] I420, int width, int height) {
        byte[] ret = I420;
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total, total / 4);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total + total / 4, total / 4);

        bufferY.put(data, 0, total);
        for (int i = total; i < data.length; i += 2) {
            bufferV.put(data[i]);
            bufferU.put(data[i + 1]);
        }

        return ret;
    }

    /**
     * NV21转nv12(YUV420SP)
     *
     * @param nv21
     * @param nv12
     * @param width
     * @param height
     */
    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }


    /**
     * YV12转I420（YUV420P）
     *
     * @param yv12bytes
     * @param i420bytes
     * @param width
     * @param height
     */
    private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        System.arraycopy(yv12bytes, width * height + width * height / 4, i420bytes, width * height, width * height / 4);
        System.arraycopy(yv12bytes, width * height, i420bytes, width * height + width * height / 4, width * height / 4);
    }

    /**
     * YV12转NV12(YUV420SP）
     *
     * @param yv12bytes
     * @param nv12bytes
     * @param width
     * @param height
     */
    private void swapYV12toNV12(byte[] yv12bytes, byte[] nv12bytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;
        System.arraycopy(yv12bytes, 0, nv12bytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            nv12bytes[nLenY + 2 * i + 1] = yv12bytes[nLenY + i];
            nv12bytes[nLenY + 2 * i] = yv12bytes[nLenY + nLenU + i];
        }
    }

    /**
     * nv12转I420（YUV420SP转YUV420P）
     *
     * @param nv12bytes
     * @param i420bytes
     * @param width
     * @param height
     */
    private void swapNV12toI420(byte[] nv12bytes, byte[] i420bytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;
        System.arraycopy(nv12bytes, 0, i420bytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            i420bytes[nLenY + i] = nv12bytes[nLenY + 2 * i + 1];
            i420bytes[nLenY + nLenU + i] = nv12bytes[nLenY + 2 * i];
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }

    /**
     * 往缓冲队列添加摄像头yuv数据 s
     *
     * @param data yuv
     */
    public synchronized void addData(byte[] data) {
        YUVQueue.add(data);
    }

}
