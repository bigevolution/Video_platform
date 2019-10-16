package com.example.gb28181_videoplatform.sip;


public interface IMediaRecorder {

    /**
     * 开始混流
     */
    public void startMux();

    /**
     * 停止混流
     */
    public void stopMux();

    /**
     * 音频错误
     *
     * @param what    错误类型
     * @param message
     */
    public void onAudioError(int what, String message);

    /**
     * 接收音频数据
     *
     * @param sampleBuffer 音频数据
     */
    public void receiveAudioData(byte[] sampleBuffer);
}
