package com.example.gb28181_videoplatform.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gb28181_videoplatform.JNIBridge;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.bean.DeviceBean;
import com.example.gb28181_videoplatform.netty.util.PoliceService;
import com.example.gb28181_videoplatform.sip.impl.DeviceImpl;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.MyUtil;
import com.example.gb28181_videoplatform.util.NetWorkUtil;
import com.example.gb28181_videoplatform.util.PixelTool;
import com.example.gb28181_videoplatform.util.ScreenSwitchUtils;
import com.example.gb28181_videoplatform.util.Toasty;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.example.gb28181_videoplatform.util.MyUtil.controlTwoView;
import static com.example.gb28181_videoplatform.util.MyUtil.setViewParams;

/**
 * Created by 吴迪 on 2019/7/10.
 * 实时视频Activity
 */
public class LiveVideoActivity extends Activity implements View.OnClickListener ,View.OnTouchListener {

    //播放
    SurfaceView live_video_view;
    RelativeLayout video_layout, title_layout;
    ImageView fullScreen_view, screenshot_view, screenshot_land_view;
    LinearLayout landSpace_layout;
    ImageView live_back, gif_view;
    RelativeLayout buffer_layout;
    TextView playbacktext;
    ImageView direction_view, direction_land_view;

    //竖屏
    ImageView adjust_add, adjust_reduce;
    TextView replay_view;
    RelativeLayout replay_layout;

    //横屏
    ImageView shrink_view, stretch_view;
    ImageView adjust_land_add, adjust_land_reduce;

    TextView device_name, device_id, device_address;

    View decorView;
    ScreenSwitchUtils switchUtils;
    boolean isShowLand = false;

    Animation mShowAction, mCloseAction, gifAnimation;

    DeviceBean.DataBean.AppDeviceInfoBean infoBean;
    //视频解码相关
    private MediaCodec mCodec;
    Thread readStreamThread;
    boolean isInit = false;
    boolean isStop = false;

    //设备ID
    private String deviceId;
    private String nvrId;
    private String controlId;

    private boolean isNvr;

    //服务器
    private String remoteIP;

    //视频相关变量
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private final static int VIDEO_WIDTH = 1920;
    private final static int VIDEO_HEIGHT = 1080;
    private final static int TIME_INTERNAL = 30;
    private final static int HEAD_OFFSET = 512;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtil.immersiveNotificationBar(this, R.color.activity_title);
        decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_live_video);

        infoBean = (DeviceBean.DataBean.AppDeviceInfoBean) getIntent().getSerializableExtra(ConstantConfig.DEVICE_LIVE);
        isNvr = getIntent().getBooleanExtra(ConstantConfig.DEVICE_IS_NVR, false);

        switchUtils = ScreenSwitchUtils.init(this.getApplicationContext());
        initView();

        deviceId = infoBean.getDeviceId();
        nvrId = infoBean.getParentId();
        controlId = isNvr ? nvrId : deviceId;
        remoteIP = DeviceImpl.getInstance().getSipProfile().getRemoteIp();

        Log.e("111", "onCreate: "+nvrId);
        Log.e("111", "onCreate: "+deviceId);

        playVideo();

    }

    public int getRandomBetweenNumbers(int m,int n){
        return (int)(m + Math.random() * (n - m + 1));
    }

    private void playVideo() {
        //生成随机端口
        String wifi = MyUtil.getIPAddress(getApplicationContext());
        int port = getRandomBetweenNumbers(30000, 50000);
        if(port % 2 == 1){
            port ++;
        }
        if(!TextUtils.isEmpty(wifi)){
            JNIBridge.StartPullStream(wifi, port, 1920, 1080);
        }
        //发送invite
        DeviceImpl.getInstance().invite(remoteIP, port, nvrId, deviceId, isNvr);
        //开启解码器
        PoliceService.instance.runOnBackgroundDelay(new Runnable() {
            @Override
            public void run() {
                if (!isInit) {
                    initDecoder();
                    isInit = true;
                }
                isStop = true;
                readStreamThread = new Thread(readStream);
                readStreamThread.start();
            }
        }, 400);
    }

    private void initView() {
        playbacktext = findViewById(R.id.playbacktext);
        live_video_view = findViewById(R.id.live_video_view);
        live_video_view.setOnClickListener(this);
        live_video_view.setZOrderMediaOverlay(true);
        video_layout = findViewById(R.id.video_layout);
        title_layout = findViewById(R.id.base_title_layout);
        fullScreen_view = findViewById(R.id.fullScreen_view);
        fullScreen_view.setOnClickListener(this);
        screenshot_view = findViewById(R.id.screenshot_view);
        screenshot_view.setOnClickListener(this);
        screenshot_land_view = findViewById(R.id.screenshot_land_view);
        screenshot_land_view.setOnClickListener(this);
        landSpace_layout = findViewById(R.id.landSpace_layout);
        live_back = findViewById(R.id.live_back);
        live_back.setOnClickListener(this);
        gif_view = findViewById(R.id.gif_view);
        buffer_layout = findViewById(R.id.buffer_layout);
        shrink_view = findViewById(R.id.shrink_view);
        shrink_view.setOnClickListener(this);
        stretch_view = findViewById(R.id.stretch_view);
        stretch_view.setOnClickListener(this);
        replay_layout = findViewById(R.id.replay_layout);
        replay_view = findViewById(R.id.replay_view);
        replay_view.setOnClickListener(this);

        adjust_add = findViewById(R.id.adjust_add);
        adjust_reduce = findViewById(R.id.adjust_reduce);
        adjust_land_add = findViewById(R.id.adjust_land_add);
        adjust_land_reduce = findViewById(R.id.adjust_land_reduce);
        direction_view = findViewById(R.id.direction_view);
        direction_land_view = findViewById(R.id.direction_land_view);

        adjust_add.setOnTouchListener(this);
        adjust_reduce.setOnTouchListener(this);
        adjust_land_add.setOnTouchListener(this);
        adjust_land_reduce.setOnTouchListener(this);
        direction_view.setOnTouchListener(this);
        direction_land_view.setOnTouchListener(this);

        device_name = findViewById(R.id.device_name);
        device_id = findViewById(R.id.device_id);
        device_address = findViewById(R.id.device_address);

        device_name.setText(Html.fromHtml(getString(R.string.video_device_name) + "<font color='#000'>"+ infoBean.getName() + "</font>"));
        device_id.setText(Html.fromHtml(getString(R.string.video_device_id) + "<font color='#000'>"+ infoBean.getDeviceId() + "</font>"));
        if(infoBean.getAddress() != null){
            device_address.setText(!infoBean.getAddress().equals("") ? infoBean.getAddress() : "暂无");
        }else {
            device_address.setText("暂无");
        }
        if(!isNvr){
            playbacktext.setVisibility(View.GONE);
        }

        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(800);
        mCloseAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mCloseAction.setDuration(800);
        mCloseAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                landSpace_layout.setVisibility(View.GONE);
                stretch_view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        gifAnimation = AnimationUtils.loadAnimation(LiveVideoActivity.this, R.anim.rotaterepeat);
        gif_view.startAnimation(gifAnimation);
    }

    //设置上下左右的触摸事件
    public void onDirectionTouch(MotionEvent e, View view, boolean isStop) {
        // 获取触摸点的坐标 x, y
        float x = e.getX();
        float y = e.getY();
        // 目标点的坐标
        float dst[] = new float[2];
        // 获取到ImageView的matrix
        Matrix imageMatrix = view.getMatrix();
        // 创建一个逆矩阵
        Matrix inverseMatrix = new Matrix();
        // 求逆，逆矩阵被赋值
        imageMatrix.invert(inverseMatrix);
        // 通过逆矩阵映射得到目标点 dst 的值
        inverseMatrix.mapPoints(dst, new float[]{x, y});
        // 判断dstX, dstY在Bitmap上的位置即可
        int dstX = (int) dst[0];
        int dstY = (int) dst[1];
//        Log.i("OnTouchListener", "dstX:" + dstX + "+++ dstY" + dstY);
        if ((240 >= dstX && dstX >= 50)&&(80 >= dstY && dstY >= 10)) {
            DeviceImpl.getInstance().SendMessage(deviceId, controlId, remoteIP, isStop ? "A50F00000000F0A4" : "A50F0008E0A0F02C");
            Log.e("111", "onDirectionTouch: 开始向上" );
        }else if((240 >= dstX && dstX >= 70)&&(320 >= dstY && dstY >= 220)){
            DeviceImpl.getInstance().SendMessage(deviceId, controlId, remoteIP, isStop ? "A50F00000000F0A4" : "A50F0004E0A0F028");
            Log.e("111", "onDirectionTouch: 开始向下" );
        }else if((80 >= dstX && dstX >= 20)&&(270 >= dstY && dstY >= 80)){
            DeviceImpl.getInstance().SendMessage(deviceId, controlId, remoteIP, isStop ? "A50F00000000F0A4" : "A50F0002A0FCF042");
            Log.e("111", "onDirectionTouch: 开始向左" );
        }else if((320 >= dstX && dstX >= 220)&&(220 >= dstY && dstY >= 60)){
            DeviceImpl.getInstance().SendMessage(deviceId, controlId, remoteIP, isStop ? "A50F00000000F0A4" : "A50F0001A0E0F025");
            Log.e("111", "onDirectionTouch: 开始向右" );
        }
    }

    //竖屏模式
    public void setPortrait(){
        isShowLand = false;
        controlTwoView(live_back, screenshot_land_view, false);
        controlTwoView(landSpace_layout, stretch_view, false);
        controlTwoView(fullScreen_view, screenshot_view, true);
        MyUtil.showSystemUI(decorView);
        title_layout.setVisibility(View.VISIBLE);
        setViewParams(video_layout, ViewGroup.LayoutParams.MATCH_PARENT, PixelTool.dpToPx(this, 200));
    }

    //横屏模式
    public void setLandSpace(){
        isShowLand = true;
        controlTwoView(live_back, screenshot_land_view, true);
        screenshot_land_view.startAnimation(mShowAction);
        MyUtil.showAnimation(mShowAction, landSpace_layout);
        controlTwoView(fullScreen_view, screenshot_view, false);
        MyUtil.hideSystemUI(decorView);
        title_layout.setVisibility(View.GONE);
        setViewParams(video_layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        switchUtils.start(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //发送bye消息
        if(isStop) {
            Log.d("onPause", "onPause:-------------------------------- ");
            DeviceImpl.getInstance().Bye();
            JNIBridge.StopPullStream();
            isStop = false;
            isInit = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        replay_layout.setVisibility(View.VISIBLE);
        buffer_layout.setVisibility(View.GONE);
        switchUtils.togglePortrait();
        switchUtils.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (switchUtils.isPortrait()) {
            // 切换成竖屏
            setPortrait();
        } else {
            // 切换成横屏
            setLandSpace();
        }
    }

    @Override
    public void onBackPressed() {
        if(switchUtils.isPortrait()){
            finish();
        }else {
            switchUtils.togglePortrait();
        }
    }

    public void back(View view) {
        finish();
    }

    public void playback(View view) {
        Intent intent = new Intent(LiveVideoActivity.this, PlaybackVideoActivity.class);
        intent.putExtra(ConstantConfig.DEVICE_PLAYBACK, infoBean);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fullScreen_view:
                switchUtils.toggleScreen();
                break;
            case R.id.screenshot_land_view:
                break;
            case R.id.live_back:
                switchUtils.togglePortrait();
                break;
            case R.id.live_video_view:
                if(isShowLand){
                    if(screenshot_land_view.getVisibility() == View.VISIBLE){
                        controlTwoView(live_back, screenshot_land_view, false);
                    }
                    if(stretch_view.getVisibility() == View.VISIBLE){
                        stretch_view.setVisibility(View.GONE);
                    }
                }else {
                    if (!switchUtils.isPortrait()) {
                        controlTwoView(live_back, screenshot_land_view, true);
                        if(landSpace_layout.getVisibility() == View.GONE){
                            stretch_view.setVisibility(View.VISIBLE);
                        }
                    }
                }
                isShowLand = ! isShowLand;
                break;
            case R.id.stretch_view:
                MyUtil.showAnimation(mShowAction, landSpace_layout);
                stretch_view.setVisibility(View.GONE);
                break;
            case R.id.shrink_view:
                landSpace_layout.startAnimation(mCloseAction);
                break;
            case R.id.replay_view:
                replay_layout.setVisibility(View.GONE);
                buffer_layout.setVisibility(View.VISIBLE);
                playVideo();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN ://手指按下
                switch (v.getId()){
                    case R.id.adjust_add:
                    case R.id.adjust_land_add:
                        DeviceImpl.getInstance().SendMessage(deviceId, controlId, remoteIP, "A50F001000004004");
                        break;
                    case R.id.adjust_reduce:
                    case R.id.adjust_land_reduce:
                        DeviceImpl.getInstance().SendMessage(deviceId, controlId, remoteIP, "A50F002000004014");
                        break;
                    case R.id.direction_view:
                        onDirectionTouch(event, direction_view, false);
                        break;
                    case R.id.direction_land_view:
                        onDirectionTouch(event, direction_land_view, false);
                        break;
                }
                break;
            case MotionEvent.ACTION_UP ://手指抬起
                switch (v.getId()){
                    case R.id.adjust_add:
                    case R.id.adjust_reduce:
                    case R.id.adjust_land_add:
                    case R.id.adjust_land_reduce:
                        DeviceImpl.getInstance().SendMessage(deviceId, controlId, remoteIP, "A50F00000000F0A4");
                        break;
                    case R.id.direction_view:
                        onDirectionTouch(event, direction_view, true);
                        break;
                    case R.id.direction_land_view:
                        onDirectionTouch(event, direction_land_view, true);
                        break;
                }
                break;
        }
        return true;
    }

    /**
     *
    ================================================================================================================================
                                                            解码相关
    ================================================================================================================================
     **/

    public void initDecoder() {
        try {
            mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                VIDEO_WIDTH, VIDEO_HEIGHT);
        mCodec.configure(mediaFormat, live_video_view.getHolder().getSurface(),
                null, 0);
        mCodec.start();
    }

    int mCount = 0;

    public boolean onFrame(byte[] buf, int offset, int length) {
        Log.e("Media", "onFrame start");
        Log.e("Media", "onFrame Thread:" + Thread.currentThread().getId());
        // Get input buffer index
        ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        int inputBufferIndex = mCodec.dequeueInputBuffer(100);

        Log.e("Media", "onFrame index:" + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount
                    * TIME_INTERNAL, 0);
            mCount++;
        } else {
            return false;
        }

        // Get output buffer index
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100);
        while (outputBufferIndex >= 0) {
            mCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
        Log.e("Media", "onFrame end");
        return true;
    }

    /**
     * Find H264 frame head
     * @return the offset of frame head, return 0 if can not find one
     */
    static int findHead(byte[] buffer, int len) {
        int i;
        for (i = HEAD_OFFSET; i < len; i++) {
            if (checkHead(buffer, i))
                break;
        }
        if (i == len)
            return 0;
        if (i == HEAD_OFFSET)
            return 0;
        return i;
    }

    /**
     * Check if is H264 frame head
     * @return whether the src buffer is frame head
     */
    static boolean checkHead(byte[] buffer, int offset) {
        // 00 00 00 01
        if (buffer[offset] == 0 && buffer[offset + 1] == 0
                && buffer[offset + 2] == 0 && buffer[3] == 1)
            return true;
        // 00 00 01
        if (buffer[offset] == 0 && buffer[offset + 1] == 0
                && buffer[offset + 2] == 1)
            return true;
        return false;
    }

    boolean isShow = true;

    Runnable readStream = new Runnable() {
        @Override
        public void run() {
            int h264Read = 0;
            int frameOffset = 0;
            byte[] buffer = new byte[100000];
            byte[] framebuffer = new byte[200000];
            while (isStop) {
                try {
                    buffer = JNIBridge.ReceiveVideoFrame();
                    int count = buffer.length;
                    Log.i("count", "" + count);
                    h264Read += count;
                    Log.d("Read", "count:" + count + " h264Read:" + h264Read);
                    if(frameOffset == 0){
                        if(isShow){
                            isShow = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    buffer_layout.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }else {
                        if(!isShow){
                            isShow = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    buffer_layout.setVisibility(View.GONE);
                                    gif_view.clearAnimation();
                                }
                            });
                        }
                    }
//                    // Fill frameBuffer
                    if (frameOffset + count < 200000) {
                        System.arraycopy(buffer, 0, framebuffer, frameOffset, count);
                        frameOffset += count;
                    } else {
                        frameOffset = 0;
                        System.arraycopy(buffer, 0, framebuffer, frameOffset, count);
                        frameOffset += count;
                    }

                    // Find H264 head
                    int offset = findHead(framebuffer, frameOffset);
                    Log.i("find head", " Head:" + offset);
                    while (offset > 0) {
                        if (checkHead(framebuffer, 0)) {
                            // Fill decoder
                            boolean flag = onFrame(framebuffer, 0, offset);
                            if (flag) {
                                byte[] temp = framebuffer;
                                framebuffer = new byte[200000];
                                System.arraycopy(temp, offset, framebuffer, 0, frameOffset - offset);
                                frameOffset -= offset;
                                Log.e("Check", "is Head:" + offset);
                                // Continue finding head
                                offset = findHead(framebuffer, frameOffset);
                            }
                        } else {
                            offset = 0;
                        }
                    }
                    Log.d("loop", "end loop");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(TIME_INTERNAL);
                } catch (InterruptedException e) {

                }

            }
        }
    };

}
