package com.example.gb28181_videoplatform.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
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
import android.widget.Toast;

import com.example.gb28181_videoplatform.JNIBridge;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.adapter.TimeRecyclerAdapter;
import com.example.gb28181_videoplatform.bean.DeviceBean;
import com.example.gb28181_videoplatform.bean.TimeBean;
import com.example.gb28181_videoplatform.netty.util.PoliceService;
import com.example.gb28181_videoplatform.sip.impl.DeviceImpl;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.MyUtil;
import com.example.gb28181_videoplatform.util.NetWorkUtil;
import com.example.gb28181_videoplatform.util.PixelTool;
import com.example.gb28181_videoplatform.util.ScreenSwitchUtils;
import com.example.gb28181_videoplatform.util.Toasty;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.gb28181_videoplatform.util.MyUtil.closeAnimation;
import static com.example.gb28181_videoplatform.util.MyUtil.controlTwoView;
import static com.example.gb28181_videoplatform.util.MyUtil.setViewParams;

/**
 * Created by Aaron on 2019/7/10.
 * 录像回放Activity
 */
public class PlaybackVideoActivity extends Activity implements CalendarView.OnCalendarInterceptListener,
        CalendarView.OnCalendarRangeSelectListener, CalendarView.OnMonthChangeListener,View.OnClickListener{
    SmartRefreshLayout refresh_Layout;
    SurfaceView play_back_view;
    ImageView back_fullScreen_view, back_screenshot_view, screenshot_land_view;
    ImageView live_back, back_gif_view;
    TextView search_start_time;
    TextView search_end_time;
    TextView current_date;
    TextView text_prograss;
//    LinearLayout landSpace_layout;
    RelativeLayout back_video_layout, back_title_layout;

    private String sdate = "";
    private String edate = "";
    private String PlayingSdate = "";
    private String PlayingEdate = "";

    LinearLayout calendar_layout, null_layout,prograss_layout;
    CalendarView mCalendarView;

    RecyclerView time_list;
    List<TimeBean> video = new ArrayList<TimeBean>();
    List<TimeBean> pageList = new ArrayList<>();
    TimeRecyclerAdapter time_Recycler_Adapter;

    ScreenSwitchUtils switchUtils;
    //视频解码相关
    private MediaCodec mCodec;
    Thread readStreamThread;
    boolean isInit = false;
    boolean isStart = false;

    Animation mShowAction, mCloseAction,gifAnimation;

    boolean isShowOrClose_start = false;

    DeviceBean.DataBean.AppDeviceInfoBean infoBean;
    private String deviceId;
    private String nvrId;
    private String remoteIP;
    private String controlId;

    private boolean isNvr;
    boolean isShowLand = false;
    boolean isInitAdapter = false;

    //视频相关变量
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private final static int VIDEO_WIDTH = 1920;
    private final static int VIDEO_HEIGHT = 1080;
    private final static int TIME_INTERNAL = 30;
    private final static int HEAD_OFFSET = 512;


    RelativeLayout back_buffer_layout;

    //竖屏
    ImageView adjust_add, adjust_reduce;
    TextView back_replay_view;
    RelativeLayout back_replay_layout;

    //横屏
    ImageView shrink_view, stretch_view;
    ImageView play,stop;

    View decorView;
    int flag = 0;
    int xmlPurse = 0;
    int sumNum;
    int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtil.immersiveNotificationBar(this, R.color.activity_title);
        decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_playback_video);

        infoBean = (DeviceBean.DataBean.AppDeviceInfoBean) getIntent().getSerializableExtra(ConstantConfig.DEVICE_PLAYBACK);
        isNvr = getIntent().getBooleanExtra(ConstantConfig.DEVICE_IS_NVR, false);
        deviceId = infoBean.getDeviceId();
        nvrId = infoBean.getParentId();
        controlId = isNvr ? nvrId : deviceId;
        Log.e("PlaybackVideoActivity", "deviceId: " + deviceId + "nvrId: " + nvrId + isNvr);

        remoteIP = DeviceImpl.getInstance().getSipProfile().getRemoteIp();

        DeviceImpl.getInstance().setTimeHandler(timehandler);
        switchUtils = ScreenSwitchUtils.init(this.getApplicationContext());
        initView();
        setAnimation();
    }

    private void initView() {
        back_video_layout = findViewById(R.id.back_video_layout);
        back_title_layout = findViewById(R.id.back_title_layout);
        play_back_view = findViewById(R.id.back_video_view);
        play_back_view.setOnClickListener(this);
        play_back_view.setZOrderMediaOverlay(true);
        back_buffer_layout = findViewById(R.id.back_buffer_layout);
        refresh_Layout = findViewById(R.id.refreshLayout);
        current_date = findViewById(R.id.current_date);
        search_start_time = findViewById(R.id.search_start_time);
        search_end_time = findViewById(R.id.search_end_time);

        back_fullScreen_view = findViewById(R.id.back_fullScreen_view);
        back_fullScreen_view.setOnClickListener(this);
        screenshot_land_view = findViewById(R.id.screenshot_land_view);
        screenshot_land_view.setOnClickListener(this);
        live_back = findViewById(R.id.live_back);
        live_back.setOnClickListener(this);
        back_gif_view = findViewById(R.id.back_gif_view);
        stretch_view = findViewById(R.id.stretch_view);
        stretch_view.setOnClickListener(this);
//        landSpace_layout = findViewById(R.id.landSpace_layout);
        shrink_view = findViewById(R.id.shrink_view);
        shrink_view.setOnClickListener(this);
        back_replay_view = findViewById(R.id.back_replay_view);
        back_replay_view.setOnClickListener(this);
        back_replay_layout = findViewById(R.id.back_replay_layout);
        back_screenshot_view = findViewById(R.id.back_screenshot_view);
        back_screenshot_view.setOnClickListener(this);

        text_prograss = findViewById(R.id.text_prograss);

        play = findViewById(R.id.play);
        stop = findViewById(R.id.stop);

//        date_layout = findViewById(R.id.date_layout);
        null_layout = findViewById(R.id.null_layout);
        prograss_layout = findViewById(R.id.prograss_layout);
        calendar_layout = findViewById(R.id.calendar_layout);
        mCalendarView = findViewById(R.id.calendar_view);
        mCalendarView.setOnCalendarRangeSelectListener(this);
        mCalendarView.setOnMonthChangeListener(this);
        //设置日期拦截事件，当前有效
        mCalendarView.setOnCalendarInterceptListener(this);

        gifAnimation = AnimationUtils.loadAnimation(PlaybackVideoActivity.this, R.anim.rotaterepeat);
        back_gif_view.startAnimation(gifAnimation);

        mCalendarView.setRange(2000, 1, 1,
                mCalendarView.getCurYear(), mCalendarView.getCurMonth(), mCalendarView.getCurDay());
        mCalendarView.scrollToCalendar(mCalendarView.getCurYear(), mCalendarView.getCurMonth(), mCalendarView.getCurDay());

        current_date.setText(mCalendarView.getCurYear() + "年"  + mCalendarView.getCurMonth() + "月");

        time_list = findViewById(R.id.time_list);
        time_list.setLayoutManager(new LinearLayoutManager(this));
        time_Recycler_Adapter = new TimeRecyclerAdapter(pageList,rec_Handler);
        time_list.setAdapter(time_Recycler_Adapter);

        refresh_Layout.setEnableRefresh(false);
        //下拉加载刷新
//        refresh_Layout.setOnRefreshListener(new OnRefreshListener() {
//            @Override
//            public void onRefresh(RefreshLayout refreshlayout) {
//                refreshlayout.finishRefresh(500/*,false*/);//传入false表示刷新失败
//                flag--;
//                initAdapter();
//            }
//        });

        //上拉加载监听
        refresh_Layout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(500/*,false*/);//传入false表示加载失败
                flag++;
                initAdapter();
            }
        });
    }

    private void initAdapter() {
        if(flag<sumNum ) {
            if(flag <= xmlPurse) {
                for (int i = (flag - 1) * 10; i < flag * 10; i++) {
                    pageList.add(video.get(i));
                    isInitAdapter = true;
                }
            }else{
                flag--;
                Toasty.info(PlaybackVideoActivity.this,"后台正在解析数据，请稍后上拉加载");
                return;
            }
        }else if(flag==sumNum){
            for (int i = (flag-1)*10; i < total; i++) {
                pageList.add(video.get(i));
                isInitAdapter = true;
            }
        }else if(flag > sumNum){
            flag--;
            Toasty.info(PlaybackVideoActivity.this,"已无更多录像记录").show();
            return;
        }
//        else if(flag <= 0){
//            flag++;
//            Toasty.info(PlaybackVideoActivity.this,"已是第一页").show();
//            return;
//        }
//        time_Recycler_Adapter = new TimeRecyclerAdapter(pageList,rec_Handler);
//        time_list.setAdapter(time_Recycler_Adapter);
        time_Recycler_Adapter.settList(pageList);
        time_Recycler_Adapter.notifyDataSetChanged();
    }

    @SuppressLint("HandlerLeak")
    private Handler rec_Handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            TimeBean time = (TimeBean)msg.obj;
            Log.e("list", "handleMessage: ------------------点击-----" );
            if(NetWorkUtil.isNetworkConnected(getApplicationContext())) {
                String startTime = String.valueOf(getStringToDate(time.getStartTime(), "yyyy-MM-dd hh:mm:ss"));
                String endTime = String.valueOf(getStringToDate(time.getEndTime(), "yyyy-MM-dd hh:mm:ss"));
                text_prograss.setText(time.getStartTime().substring(5) + "--" + time.getEndTime().substring(5));
                PlayingSdate = startTime;
                PlayingEdate = endTime;
                if (play.getVisibility() == View.VISIBLE) {
                    play.setVisibility(View.GONE);
                    stop.setVisibility(View.VISIBLE);
                }
                if (back_replay_layout.getVisibility() == View.VISIBLE) {
                    back_replay_layout.setVisibility(View.GONE);
                    back_buffer_layout.setVisibility(View.VISIBLE);
                }
                if (!isStart) {
                    playBackVideo(startTime, endTime);
                } else {
                    DeviceImpl.getInstance().Bye();
                    JNIBridge.StopPullStream();
                    isStart = false;
                    playBackVideo(startTime, endTime);
                }
            }else{
                Toasty.error(PlaybackVideoActivity.this, "网络连接异常，请检查网络!").show();
            }
        }
    };

    //时间戳
    public static String getStringToDate(String dateString, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();
        try{
            date = dateFormat.parse(dateString);
        } catch(ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.e("time", "getStringToDate:----------------------- "+date.getTime());
        return String.valueOf((date.getTime())/1000);
    }

    public int getRandomBetweenNumbers(int m,int n){
        return (int)(m + Math.random() * (n - m + 1));
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
    //竖屏模式
    public void setPortrait(){
        isShowLand = false;
        controlTwoView(live_back, screenshot_land_view, false);
//        controlTwoView(landSpace_layout, stretch_view, false);
        controlTwoView(back_fullScreen_view, back_screenshot_view, true);
        MyUtil.showSystemUI(decorView);
        back_title_layout.setVisibility(View.VISIBLE);
        setViewParams(back_video_layout, ViewGroup.LayoutParams.MATCH_PARENT, PixelTool.dpToPx(this, 200));
    }

    //横屏模式
    public void setLandSpace(){
        isShowLand = true;
        controlTwoView(live_back, screenshot_land_view, true);
        screenshot_land_view.startAnimation(mShowAction);
//        MyUtil.showAnimation(mShowAction, landSpace_layout);
        controlTwoView(back_fullScreen_view, back_screenshot_view, false);
        MyUtil.hideSystemUI(decorView);
        back_title_layout.setVisibility(View.GONE);
        setViewParams(back_video_layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
    @Override
    protected void onStart() {
        super.onStart();
        switchUtils.start(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.e("pause", "onStop: ---------------------------------");
        if(PlayingSdate != ""){
            back_replay_layout.setVisibility(View.VISIBLE);
            back_buffer_layout.setVisibility(View.GONE);
            switchUtils.togglePortrait();
            switchUtils.stop();
            stop.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
            isInit = false;
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("list", "onDestroy:-------------------------- " );
        timehandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_fullScreen_view:
                if(isStart) {
                    switchUtils.toggleScreen();
                }else{
                    Toasty.warning(this,"请先选择录像视频").show();
                }
                break;
            case R.id.screenshot_land_view:
                break;
            case R.id.live_back:
                switchUtils.togglePortrait();
                break;
            case R.id.back_video_view:
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
//                        if(landSpace_layout.getVisibility() == View.GONE){
//                            stretch_view.setVisibility(View.VISIBLE);
//                        }
                    }
                }
                isShowLand = ! isShowLand;
                break;
            case R.id.stretch_view:
//                MyUtil.showAnimation(mShowAction, landSpace_layout);
                stretch_view.setVisibility(View.GONE);
                break;
            case R.id.shrink_view:
//                landSpace_layout.startAnimation(mCloseAction);
                break;
            case R.id.back_replay_view:
                if(NetWorkUtil.isNetworkConnected(getApplicationContext())) {
                    back_replay_layout.setVisibility(View.GONE);
                    back_buffer_layout.setVisibility(View.VISIBLE);
                    if (play.getVisibility()==View.VISIBLE){
                        play.setVisibility(View.GONE);
                        stop.setVisibility(View.VISIBLE);
                    }
                    playBackVideo(PlayingSdate, PlayingEdate);
                }else{
                    Toasty.error(PlaybackVideoActivity.this, "网络连接异常，请检查网络!").show();
                }
                Log.e("test", "bofang: 重新播放"+PlayingSdate+"----------------------"+PlayingEdate );
                break;
            default:
                break;
        }
    }

    public void bofang(View view) {
        if(NetWorkUtil.isNetworkConnected(getApplicationContext())) {
            if(PlayingSdate.equals("") && isInitAdapter){
                time_Recycler_Adapter = new TimeRecyclerAdapter(pageList,rec_Handler);
                time_list.setAdapter(time_Recycler_Adapter);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View v = time_list.getLayoutManager().findViewByPosition(0);
                        v.performClick();
                    }
                },200);
            }else if (PlayingSdate.equals("")) {
                Toasty.warning(this, "请先选择录像视频").show();
            } else if (isStart) {
                DeviceImpl.getInstance().Bye();
                JNIBridge.StopPullStream();
                if (back_replay_layout.getVisibility() == View.GONE) {
                    back_replay_layout.setVisibility(View.VISIBLE);
                    back_buffer_layout.setVisibility(View.GONE);
                }
                isStart = false;
                if (stop.getVisibility() == View.VISIBLE) {
                    stop.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                }
                Log.e("test", "bofang: 暂停" + PlayingSdate + "----------------------" + PlayingEdate);
            } else {
                if (play.getVisibility() == View.VISIBLE) {
                    play.setVisibility(View.GONE);
                    stop.setVisibility(View.VISIBLE);
                }
                back_replay_layout.setVisibility(View.GONE);
                back_buffer_layout.setVisibility(View.VISIBLE);
                playBackVideo(PlayingSdate, PlayingEdate);
                Log.e("test", "bofang: 播放" + PlayingSdate + "----------------------" + PlayingEdate);
            }
        }else{
            Toasty.error(PlaybackVideoActivity.this, "网络连接异常，请检查网络!").show();
        }
    }

    private void playBackVideo(String startTime,String endTime) {
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
        DeviceImpl.getInstance().backInvite(remoteIP, port, nvrId, deviceId,startTime,endTime);
        //开启解码器
        PoliceService.instance.runOnBackgroundDelay(new Runnable() {
            @Override
            public void run() {
                if (!isInit) {
                    initDecoder();
                    Log.d("初始化：", "------------初始化解码器----------- ");
                    isInit = true;
                }
                isStart = true;
                readStreamThread = new Thread(readStream);
                readStreamThread.start();
            }
        }, 400);
    }

    public void setAnimation() {
        //设置显示时的动画
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(400);
        //设置隐藏时的动画，监听动画结束后隐藏选择框
        mCloseAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        mCloseAction.setDuration(400);
    }

    public void search(View view) {
        if (NetWorkUtil.isNetworkConnected(getApplicationContext())) {
            if (sdate.equals("")){
                Toasty.warning(this,"请选择开始日期").show();
            }else if (edate.equals("")){
                Toasty.warning(this,"请选择结束日期").show();
            }else {
                if (prograss_layout.getVisibility() == View.VISIBLE) {
                    Toasty.warning(this, "正在搜索，请勿重复点击").show();
                }else if (xmlPurse < sumNum) {
                    Toasty.warning(this, "后台正在接收数据，请勿重复搜索").show();
                    return;
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            video.clear();
                            pageList.clear();
                            flag = 0;
                            xmlPurse = 0;
                            sendRequest(sdate, edate);
                        }
                    }).start();
                }

                if (time_list.getVisibility() == View.VISIBLE) {
                    time_list.setVisibility(View.GONE);
                    refresh_Layout.setVisibility(View.GONE);
                    prograss_layout.setVisibility(View.VISIBLE);
                }
                if (calendar_layout.getVisibility() == View.GONE) {
                    null_layout.setVisibility(View.GONE);
                    prograss_layout.setVisibility(View.VISIBLE);
                }
            }
        }else{
            Toasty.error(PlaybackVideoActivity.this, "网络连接异常，请检查网络!").show();
        }
    }
    @SuppressLint("HandlerLeak")
    private Handler timehandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.e("timeHandler", "handleMessage: "+ String.valueOf(msg.obj));
                    if(String.valueOf(msg.obj).indexOf("<Item>") > 0){
                        xmlBackParse(String.valueOf(msg.obj));
                        Log.e("list", "handleMessage: ----------------------------正在解析" );
                    }else{
                        isInitAdapter = false;
                        prograss_layout.setVisibility(View.GONE);
                        null_layout.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void xmlBackParse(String content){
        String startTime = "";
        String endTime;
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            //2,初始化解析器，设置要解析的流数据，并设置编码方式
            xmlPullParser.setInput(new StringReader(content));
            //3,循环解析
            int type = xmlPullParser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                //如果是开始标签
                if (type == XmlPullParser.START_TAG) {
                    if(flag == 0) {
                        if ("SumNum".equals(xmlPullParser.getName())) {
                            sumNum = Integer.parseInt(xmlPullParser.nextText());
                            total = sumNum;
                            if (sumNum%10==0){
                                sumNum = sumNum/10;
                            }else {
                                sumNum = sumNum/10 + 1;
                            }
                        }
                    }
                    if ("StartTime".equals(xmlPullParser.getName())) {
                        //获取开始时间
                        startTime = xmlPullParser.nextText().replace("T"," ").replaceAll(" : ",":");

                        Log.e("Aaron", "startTime-----------------------------------------------------===" +startTime);
                    } else if ("EndTime".equals(xmlPullParser.getName())) {
                        endTime = xmlPullParser.nextText().replace("T"," ").replaceAll(" : ",":");//获取结束时间
                        TimeBean list = new TimeBean();
                        list.setStartTime(startTime);
                        list.setEndTime(endTime);
                        video.add(list);
                        Log.e("Aaron", "endTime=--------------------------------------------------------==" + endTime);
                    }
                }
                //让解析器移动到下一个
                type = xmlPullParser.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        xmlPurse++;
        if (flag == 0){
            flag++;
            prograss_layout.setVisibility(View.GONE);
            time_list.setVisibility(View.VISIBLE);
            refresh_Layout.setVisibility(View.VISIBLE);
            time_list.startAnimation(mShowAction);
            initAdapter();
        }
    }

    public void sendRequest(String sdate,String edate){
//        SipProfile sipProfile = new SipProfile();
//        mySipManager = new MySipManager(sipProfile);
//        infoBean = (DeviceBean.DataBean.AppDeviceInfoBean) getIntent().getSerializableExtra(ConstantConfig.DEVICE_LIVE);
//        Log.e("zhy", "sendRequest: To id:"+infoBean.getDeviceId() );
//        String targetId = infoBean.getDeviceId();
//        String nvrId = infoBean.getParentId();
//        DeviceImpl.getInstance().getPlayBackList(targetId,start_time,end_time);
        DeviceImpl.getInstance().SendTimeMsg(deviceId, nvrId, remoteIP, sdate,edate);
    }

    public void back(View view) {
        finish();
    }

    public void close(View view) {
        closeAnimation(mCloseAction, calendar_layout);
        isShowOrClose_start = false;
    }

    public void showCalendarView(View view) {
        if(isShowOrClose_start){
            closeAnimation(mCloseAction, calendar_layout);
        }else {
            calendar_layout.setVisibility(View.VISIBLE);
            calendar_layout.startAnimation(mShowAction);
            calendar_layout.bringToFront();
        }
        isShowOrClose_start = !isShowOrClose_start;
    }

    @Override
    protected void onPause() {
        //发送bye消息
        if (isStart){
            DeviceImpl.getInstance().Bye();
            JNIBridge.StopPullStream();
            isStart = false;
            if (play.getVisibility()==View.VISIBLE){
                play.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);
            }
//        isInit = false;
            Log.e("pause", "onPause: --------------------------发bye--" );
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(switchUtils.isPortrait()){
            if(calendar_layout.getVisibility() == View.VISIBLE){
                isShowOrClose_start = false;
                closeAnimation(mCloseAction, calendar_layout);
            }else {
                Log.e("list", "onBackPressed: ------------------fanhui" );
                finish();
            }
        }else {
            switchUtils.togglePortrait();
        }
    }

    /**
     * 屏蔽某些不可点击的日期，可根据自己的业务自行修改
     * 如 calendar > 2018年1月1日 && calendar <= 2020年12月31日
     *
     * @param calendar calendar
     * @return 是否屏蔽某些不可点击的日期，MonthView和WeekView有类似的API可调用
     */
    @Override
    public boolean onCalendarIntercept(Calendar calendar) {
        return calendar.hasScheme();
    }

    @Override
    public void onCalendarInterceptClick(Calendar calendar, boolean isClick) {
        Toast.makeText(this,
                calendar.toString() + (isClick ? "拦截不可点击" : "拦截设定为无效日期"),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMonthChange(int year, int month) {
        Log.e("onMonthChange", "  -- " + year + "  --  " + month);
        current_date.setText(year + "年"  + month + "月");
    }

    @Override
    public void onCalendarSelectOutOfRange(Calendar calendar) {
        // TODO: 2018/9/13 超出范围提示
    }

    @Override
    public void onSelectOutOfRange(Calendar calendar, boolean isOutOfMinRange) {
        Toast.makeText(this,
                calendar.toString() + (isOutOfMinRange ? "小于最小选择范围" : "超过最大选择范围"),
                Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCalendarRangeSelect(Calendar calendar, boolean isEnd) {
        if (!isEnd) {
            sdate = String.valueOf(calendar.getYear())+"-"+addZero(calendar.getMonth())+"-"+addZero(calendar.getDay())+"T00:00:00";
            edate = "";
            search_start_time.setText(calendar.getMonth() + "月" + calendar.getDay() + "日");
            search_end_time.setText("结束时间");
        } else {
            edate = String.valueOf(calendar.getYear())+"-"+addZero(calendar.getMonth())+"-"+addZero(calendar.getDay())+"T23:59:59";
            search_end_time.setText(calendar.getMonth() + "月" + calendar.getDay() + "日");
            closeAnimation(mCloseAction, calendar_layout);
            isShowOrClose_start = false;
        }
    }

    public void lastMonth(View view) {
        mCalendarView.scrollToPre();
    }

    public void nextMonth(View view) {
        mCalendarView.scrollToNext();
    }
    //单位数月份日期补0
    public String addZero(int data){
        String dates = "0";
        if (data<10){
            dates = dates + data;
        }else {
            dates = String.valueOf(data);
        }
        return dates;
    }



    /**
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
        mCodec.configure(mediaFormat, play_back_view.getHolder().getSurface(),
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
    int num0 = 0;
    Runnable readStream = new Runnable() {
        @Override
        public void run() {
            int h264Read = 0;
            int frameOffset = 0;
            byte[] buffer = new byte[100000];
            byte[] framebuffer = new byte[200000];
            while (isStart) {
                try {
                    buffer = JNIBridge.ReceiveVideoFrame();
                    int count = buffer.length;
//                    Log.i("count", "" + count);
                    h264Read += count;
                    Log.d("Read", "count:" + count + " h264Read:" + h264Read + "FrameOffset:" + frameOffset);

                    if(frameOffset == 0){
                        if(isShow){ //第一次显示转圈
                            isShow = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    back_buffer_layout.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }else { //frameoffset不为0，除第一次以外走这
                        if(!isShow){ //只有第二次请求的时候isShow = false,才会走这，取消转圈
                            isShow = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    back_buffer_layout.setVisibility(View.GONE);
                                    back_gif_view.clearAnimation();
                                }
                            });
                        } else{// 开始播放以后
                            if(count == 0 && num0>= 20){
                                DeviceImpl.getInstance().Bye();
                                JNIBridge.StopPullStream();
                                num0 = 0;
                                isStart = false;
                                Log.d("网络状态：", "network: " + NetWorkUtil.isNetworkConnected(getApplicationContext()) + " wifi:" + NetWorkUtil.isWifiConnected(getApplicationContext()));

                                Log.d("stop:","-------------------------Stop playing----------------------");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        back_replay_layout.setVisibility(View.VISIBLE);
                                        back_buffer_layout.setVisibility(View.GONE);
                                        stop.setVisibility(View.GONE);
                                        play.setVisibility(View.VISIBLE);
                                        if(!NetWorkUtil.isNetworkConnected(getApplicationContext())){
                                            //无网络连接时停止播放并提示
                                            Toasty.error(PlaybackVideoActivity.this, "网络连接已断开!").show();
                                        }
                                    }
                                });
                            }else if(count == 0 && num0<20){
                                num0++;
                            }else{
                                num0 = 0;
                            }
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