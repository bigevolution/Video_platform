package com.example.gb28181_videoplatform.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.blankj.utilcode.util.SPUtils;
import com.example.gb28181_videoplatform.JNIBridge;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.sip.RtpAddressMsg;
import com.example.gb28181_videoplatform.util.AvcEncoder2;
import com.example.gb28181_videoplatform.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PushActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private final static String TAG = PushActivity.class.getSimpleName();
    private final static String SP_CAM_WIDTH = "cam_width";
    private final static String SP_CAM_HEIGHT = "cam_height";
    //默认帧率
    private final static int DEFAULT_FRAME_RATE = 25;
    private final static int DEFAULT_BIT_RATE = 8500 * 1000;
    protected int mFrameRate = DEFAULT_FRAME_RATE;
    private Camera camera;
    private SurfaceHolder previewHolder;
    private byte[] previewBuffer;
    private boolean isStreaming = false;
    private AvcEncoder2 encoder;
    private Handler handler = new Handler();
    private long ssrc;
    private String ip;
    private int port;
    private int PAYLOAD_TYPE = 96;

//    static {
//        System.loadLibrary("native-lib");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮
        this.setContentView(R.layout.activity_push);

        this.findViewById(R.id.btnCamSize).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSettingsDlg();
                    }
                });

        this.findViewById(R.id.btnStream).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isStreaming) {
                            ((Button) v).setText("Stream");
                            stopStream();
                        } else {
                            showStreamDlg();
                        }
                    }
                });
        SurfaceView svCameraPreview = (SurfaceView) this.findViewById(R.id.svCameraPreview);
        this.previewHolder = svCameraPreview.getHolder();
        this.previewHolder.addCallback(this);
        PAYLOAD_TYPE = SPUtils.getInstance().getInt("payloadType");
        RtpAddressMsg rtpAddressMsg = (RtpAddressMsg) getIntent().getSerializableExtra("msg");
        if (rtpAddressMsg != null) {
            ip = rtpAddressMsg.getIp();
            port = rtpAddressMsg.getPort();
            if (rtpAddressMsg.getSsrc() != null) {
                ssrc = Long.parseLong(rtpAddressMsg.getSsrc());
                Log.e("gaozy", "ssrc====" + ssrc);
            }
            Log.e("gaozy", "推流地址：" + rtpAddressMsg.getIp() + "端口：" + rtpAddressMsg.getPort() + "payloadType==" + PAYLOAD_TYPE);
        }
        //DeviceImpl.getInstance().setDeviceHandler(pushHandler);
    }

    @Override
    protected void onResume() {
        try {
            JNIBridge.StartPushStream(
                    ip,
                    port,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    ssrc,
                    0
            );
            Log.e("gaozy", "StartPushStream_success");
            //mAudioCollector.start();
        } catch (Exception e) {
            Log.e("gaozy", "StartPushStream_error");
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        this.stopStream();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e("gaozy", "JNIBridge.endMux()");
        //JNIBridge.endMux();
        super.onDestroy();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        this.camera.addCallbackBuffer(this.previewBuffer);
        if (this.isStreaming) {
            Log.e("gaozy", "putYUVData");
            putYUVData(data, data.length);
        }
    }

    private void putYUVData(byte[] data, int length) {
        Log.e("gaozy", "YUVQueue.size() ==" + encoder.YUVQueue.size());
        if (encoder.YUVQueue.size() >= encoder.yuvqueuesize) {
            encoder.YUVQueue.poll();
        }
        encoder.addData(data);
        //Log.e("gaozy", "YUVQueue.size() == " + YUVQueue.size());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopCamera();
    }


    private void startStream(String ip, int port, int pt) {
        Log.e("gaozy", ip + port + pt);
        int width = SPUtils.getInstance().getInt(SP_CAM_WIDTH, 0);
        int height = SPUtils.getInstance().getInt(SP_CAM_HEIGHT, 0);
        this.encoder = new AvcEncoder2(width, height, mFrameRate, DEFAULT_BIT_RATE, pt);
        encoder.StartEncoderThread();
//        try {
//            JNIBridge.StartPushStream(
//                    ip,
//                    port,
//                    width,
//                    height,最终选择帧率
//                    width,
//                    height,
//                    mFrameRate,
//                    1,
//                    580000,
//                    ssrc
//            );
//            Log.e("gaozy", "StartPushStream_success");
//            //mAudioCollector.start();
//        } catch (Exception e) {
//            Log.e("gaozy", "StartPushStream_error");
//            e.printStackTrace();
//        }
        ((Button) this.findViewById(R.id.btnStream)).setText("停止推流");
        this.findViewById(R.id.btnCamSize).setEnabled(false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isStreaming = true;
            }
        }, 2500);
    }

    private void stopStream() {
        if(isStreaming){
            this.isStreaming = false;
            if (this.encoder != null)
                this.encoder.StopThread();
            this.encoder = null;
            this.findViewById(R.id.btnCamSize).setEnabled(true);
        }
    }

    private void startCamera() {
        //SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        int width = SPUtils.getInstance().getInt(SP_CAM_WIDTH, 0);
        int height = SPUtils.getInstance().getInt(SP_CAM_HEIGHT, 0);
        int pt = SPUtils.getInstance().getInt("payloadType");
        if (width == 0) {
            Camera tmpCam = Camera.open();
            Camera.Parameters params = tmpCam.getParameters();
            final List<Camera.Size> prevSizes = params.getSupportedPreviewSizes();
            int i = prevSizes.size() - 1;
            width = prevSizes.get(i).width;
            height = prevSizes.get(i).height;
            SPUtils.getInstance().put(SP_CAM_WIDTH, width);
            SPUtils.getInstance().put(SP_CAM_HEIGHT, height);
            tmpCam.release();
            tmpCam = null;
        }
        this.previewHolder.setFixedSize(width, height);

        int stride = (int) Math.ceil(width / 16.0f) * 16;
        int cStride = (int) Math.ceil(width / 32.0f) * 16;
        final int frameSize = stride * height;
        final int qFrameSize = cStride * height / 2;

        this.previewBuffer = new byte[width * height * 3 / 2];

        try {
            camera = Camera.open();
            camera.setPreviewDisplay(this.previewHolder);
            //camera.setDisplayOrientation(90);
            Camera.Parameters params = camera.getParameters();
            params.setPreviewSize(width, height);

            //获取支持的预览格式，相机始终支持NV21。自API—12，始终支持YV12
            List<Integer> previewFormats = params.getSupportedPreviewFormats();
            for (int i = 0; i < previewFormats.size()
                    ; i++) {
                Log.e("gaozy", "supportPreviewFormats====" + previewFormats.get(i));
            }
            params.setPreviewFormat(ImageFormat.YV12);
            //设置自动连续对焦
            String mode = getAutoFocusMode(params);
            if (StringUtils.isNotEmpty(mode)) {
                params.setFocusMode(mode);
            }
            //设置帧率
            setPreviewFrame(params);
            camera.setParameters(params);
            //分配一个buffer地址
            camera.addCallbackBuffer(previewBuffer);
            //setPreviewCallbackWithBuffer优化内存,通过内存复用来提高预览的效率
            camera.setPreviewCallbackWithBuffer(this);
            //camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            //TODO:
        } catch (RuntimeException e) {
            //TODO:
        }
    }

    private void setPreviewFrame(Camera.Parameters mParameters) {
        if (mParameters == null) {
            return;
        }
        List<Integer> rates = mParameters.getSupportedPreviewFrameRates();
        Log.e("MedidaRecord", "支持的帧率有 " + rates.toString());
        if (rates != null) {
            if (rates.contains(DEFAULT_FRAME_RATE)) {
                mFrameRate = DEFAULT_FRAME_RATE;
            } else {
                boolean findFrame = false;
                Collections.sort(rates);
                for (int i = rates.size() - 1; i >= 0; i--) {
                    if (rates.get(i) <= DEFAULT_FRAME_RATE) {
                        mFrameRate = rates.get(i);
                        findFrame = true;
                        break;
                    }
                }
                if (!findFrame) {
                    mFrameRate = rates.get(0);
                }
            }
        }
        mParameters.setPreviewFrameRate(mFrameRate);
        Log.e("MedidaRecord", "最终选择帧率: " + mFrameRate);
    }


    /**
     * 连续自动对焦
     */
    private String getAutoFocusMode(Camera.Parameters mParameters) {
        if (mParameters != null) {
            //持续对焦是指当场景发生变化时，相机会主动去调节焦距来达到被拍摄的物体始终是清晰的状态。
            List<String> focusModes = mParameters.getSupportedFocusModes();
            if ((Build.MODEL.startsWith("GT-I950") || Build.MODEL.endsWith("SCH-I959") || Build.MODEL.endsWith("MEIZU MX3")) && isSupported(focusModes, "continuous-picture")) {
                return "continuous-picture";
            } else if (isSupported(focusModes, "continuous-video")) {
                return "continuous-video";
            } else if (isSupported(focusModes, "auto")) {
                return "auto";
            }
        }
        return null;
    }

    /**
     * 检测是否支持指定特性
     */
    private boolean isSupported(List<String> list, String key) {
        return list != null && list.contains(key);
    }


    private void stopCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void showStreamDlg() {
        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.stream_dlg_view, null);

        if (ip.length() > 0) {
            EditText etIP = (EditText) content.findViewById(R.id.etIP);
            etIP.setText(ip);
            EditText etPort = (EditText) content.findViewById(R.id.etPort);
            etPort.setText(String.valueOf(port));
            EditText etPT = (EditText) content.findViewById(R.id.etPT);
            etPT.setText(String.valueOf(PAYLOAD_TYPE));
        }

        AlertDialog.Builder dlgBld = new AlertDialog.Builder(this);
        dlgBld.setTitle(R.string.app_name);
        dlgBld.setView(content);
        dlgBld.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText etIP = ((AlertDialog) dialog).findViewById(R.id.etIP);
                        EditText etPort = ((AlertDialog) dialog).findViewById(R.id.etPort);
                        EditText etPt = ((AlertDialog) dialog).findViewById(R.id.etPT);
                        String ip = etIP.getText().toString();
                        int port = Integer.valueOf(etPort.getText().toString());
                        int pt = Integer.valueOf(etPt.getText().toString());
                        if (ip.length() > 0 && (port >= 0 && port <= 65535) && pt >= 0) {
                            startStream(ip, port, pt);
                        } else {
                            //TODO:
                            Log.e("gaozy", "推流地址参数有误！");
                        }
                    }
                });
        dlgBld.setNegativeButton(android.R.string.cancel, null);
        dlgBld.show();
    }

    private int pos = 0;//历史选择position

    private void showSettingsDlg() {
        Camera.Parameters params = camera.getParameters();
        final List<Camera.Size> prevSizes = params.getSupportedPreviewSizes();
        String[] choiceStrItems = new String[prevSizes.size()];
        ArrayList<String> choiceItems = new ArrayList<String>();
        for (Camera.Size s : prevSizes) {
            choiceItems.add(s.width + "x" + s.height);
        }
        choiceItems.toArray(choiceStrItems);

        AlertDialog.Builder dlgBld = new AlertDialog.Builder(this);
        dlgBld.setTitle(R.string.app_name);
        dlgBld.setSingleChoiceItems(choiceStrItems, pos, null);
        dlgBld.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pos = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        Camera.Size s = prevSizes.get(pos);
                        SPUtils.getInstance().put(SP_CAM_WIDTH, s.width);
                        SPUtils.getInstance().put(SP_CAM_HEIGHT, s.height);

                        stopCamera();
                        startCamera();
                    }
                });
        dlgBld.setNegativeButton(android.R.string.cancel, null);
        dlgBld.show();
    }

    private Handler pushHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 7: {
                    stopStream();
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                    break;
                }
            }
        }
    };
    public void StopPushAndFinish() {
        stopStream();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finish();
    }
}
