package com.example.gb28181_videoplatform.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.adapter.TabPagerAdapter;
import com.example.gb28181_videoplatform.bean.DeviceBean;
import com.example.gb28181_videoplatform.fragment.DeviceFragment;
import com.example.gb28181_videoplatform.netty.service.entity.PushFlyBean;
import com.example.gb28181_videoplatform.netty.util.NetworkStateReceiver;
import com.example.gb28181_videoplatform.netty.util.PoliceService;
import com.example.gb28181_videoplatform.netty.util.PoliceServiceListener;
import com.example.gb28181_videoplatform.sip.RtpAddressMsg;
import com.example.gb28181_videoplatform.sip.impl.DeviceImpl;
import com.example.gb28181_videoplatform.sip.impl.SipProfile;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.MyUtil;
import com.example.gb28181_videoplatform.util.Toasty;
import com.example.gb28181_videoplatform.widget.CommonDialog;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.example.gb28181_videoplatform.util.MyUtil.closeAnimation;

/**
 * Created by 吴迪 on 2019/7/10.
 * 主界面Activity
 */

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    EditText search_text;
    ImageView person_view, search_view, closeImg;

    DrawerLayout drawer_layout;
    TextView user_pwd, cancel, confirm, user_setting;
    Button exit_btn;
    TabLayout tab_layout;
    ViewPager fragment_pager;
    LinearLayout search_layout;

    List<String> tabIndicators = new ArrayList<>();
    List<Fragment> fragments = new ArrayList<>();
    AlertDialog dialog;

    Animation mShowAction, mCloseAction;

    private long firstTime = 0;

    private NetworkStateReceiver mNetworkStateReceiver;

    private Gson gson = new Gson();

    /**
     * 循环发送心跳
     */
    private Handler handler_heart = new Handler();
    private Runnable runnable_heart = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //发送心跳
            DeviceImpl.getInstance().Heart();
            handler_heart.postDelayed(this,  SPUtils.getInstance().getInt("heartTime"));
        }
    };
    private int heartErrorCount = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(MainActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    handler_heart.postDelayed(runnable_heart,1500);//发送心跳
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "注销成功", Toast.LENGTH_SHORT).show();
                    handler_heart.removeCallbacks(runnable_heart);//停止心跳
                    break;
                case 3:
                    showPushFlowDialog((RtpAddressMsg) msg.obj);
                    break;
                case 4:
                    String error_register = (String) msg.obj;
                    Toast.makeText(MainActivity.this, error_register + " 注册失败！", Toast.LENGTH_SHORT).show();
                    handler_heart.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceImpl.getInstance().Register(0);//每过五秒尝试再注册
                        }
                    }, 5000);
                    break;

                case 5:
                    String error_msg = (String) msg.obj;
                    Toast.makeText(MainActivity.this, error_msg + " 心跳异常！", Toast.LENGTH_SHORT).show();
                    heartErrorCount++;
                    if (heartErrorCount == 2) {
                        Log.e("gaozy", "开始重新注册...");
                        handler_heart.removeCallbacks(runnable_heart);//停止心跳
                        DeviceImpl.getInstance().Register(0);//重新注册
                        heartErrorCount = 0;
                    }
                    break;
                case 6:
                    Log.e("gaozy", "心跳正常...");
                    heartErrorCount = 0;
                    break;
                case 7: {
                    if (ActivityUtils.isActivityExistsInStack(PushActivity.class)) {
                        ((PushActivity) ActivityUtils.getTopActivity()).StopPushAndFinish();
                        Log.e("gaozy", "收到bye请求，关闭推流！");
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtil.immersiveNotificationBar(this, R.color.activity_title);
        setContentView(R.layout.activity_main);

        initSip();
        initView();
        setAnimation();

        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkStateReceiver=new NetworkStateReceiver();
        registerReceiver(mNetworkStateReceiver,netIntentFilter);

    }

    //初始化sip协议栈
    private void initSip() {
        DeviceImpl.getInstance().setSipProfile(new SipProfile());
        initializeSipFromPreferences();
        HashMap<String, String> customHeaders = new HashMap<>();
        customHeaders.put("customHeader1", "customValue1");
        customHeaders.put("customHeader2", "customValue2");
        DeviceImpl.getInstance().Initialize(getApplication(), DeviceImpl.getInstance().getSipProfile(), customHeaders);
        DeviceImpl.getInstance().setDeviceHandler(handler);
        DeviceImpl.getInstance().Register(0);

    }

    //初始化sip参数
    private void initializeSipFromPreferences() {
        DeviceImpl.getInstance().getSipProfile().setLocalIp(MyUtil.getIPAddress(getApplicationContext()));
        DeviceImpl.getInstance().getSipProfile().setLocalPort(SPUtils.getInstance().getInt("localPort"));
        DeviceImpl.getInstance().getSipProfile().setRemoteIp(SPUtils.getInstance().getString("remoteIp"));
        DeviceImpl.getInstance().getSipProfile().setRemotePort(SPUtils.getInstance().getInt("remotePort"));
        DeviceImpl.getInstance().getSipProfile().setRemoteSipNum(SPUtils.getInstance().getString("sipName"));
        DeviceImpl.getInstance().getSipProfile().setSipUserName(SPUtils.getInstance().getString("accountName"));
        DeviceImpl.getInstance().getSipProfile().setMySipNum(SPUtils.getInstance().getString("mySipName"));
        DeviceImpl.getInstance().getSipProfile().setSipPassword(SPUtils.getInstance().getString("accountPassword"));
    }

    private void initView() {
        search_text = findViewById(R.id.text_search);
        drawer_layout = findViewById(R.id.drawer_layout);
        tab_layout = findViewById(R.id.tab_layout);
        fragment_pager = findViewById(R.id.fragment_pager);

        tabIndicators.add(getString(R.string.tab_ALL));
        tabIndicators.add(getString(R.string.tab_IPC));
        tabIndicators.add(getString(R.string.tab_Intelligent));
        tabIndicators.add(getString(R.string.tab_NVR));
        fragments.add(DeviceFragment.newInstance(getString(R.string.tab_ALL), ConstantConfig.DEVICE_ALL_NUM));
        fragments.add(DeviceFragment.newInstance(getString(R.string.tab_IPC), ConstantConfig.DEVICE_IPC_NUM));
        fragments.add(DeviceFragment.newInstance(getString(R.string.tab_Intelligent), ConstantConfig.DEVICE_SMART_NUM));
        fragments.add(DeviceFragment.newInstance(getString(R.string.tab_NVR), ConstantConfig.DEVICE_NVR_NUM));

        tab_layout.setTabMode(TabLayout.MODE_SCROLLABLE);
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        tabPagerAdapter.setTabFragments(fragments);
        tabPagerAdapter.setTabIndicators(tabIndicators);
        fragment_pager.setAdapter(tabPagerAdapter);
        fragment_pager.setOffscreenPageLimit(fragments.size());
        tab_layout.setupWithViewPager(fragment_pager);

//        user_setting = findViewById(R.id.user_setting);
//        user_setting.setOnClickListener(this);
        user_pwd = findViewById(R.id.user_pwd);
        user_pwd.setOnClickListener(this);
        exit_btn = findViewById(R.id.exit_btn);
        exit_btn.setOnClickListener(this);
        person_view = findViewById(R.id.person_view);
        person_view.setOnClickListener(this);
        search_view = findViewById(R.id.search_view);
        search_view.setOnClickListener(this);
        search_layout = findViewById(R.id.search_layout);

        //输入法右下角点击直接进行搜索
        search_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    closeAnimation(mCloseAction, search_layout);
                    showOrHide(false);
                    Intent intent = new Intent(MainActivity.this, SearchDeviceActivity.class);
                    intent.putExtra(ConstantConfig.DEVICE_SEARCH, search_text.getText().toString());
                    startActivity(intent);
                }
                return false;
            }
        });

        //软键盘弹出时点击返回键消失 et同步消失
        search_text.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_BACK)) {
                    closeAnimation(mCloseAction, search_layout);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        PoliceService.instance.addListener(listener);
        PoliceService.instance.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        DeviceImpl.getInstance().Register(1);
        unregisterReceiver(mNetworkStateReceiver);
        handler_heart.removeCallbacks(runnable_heart);
        PoliceService.instance.removeListener(listener);
        PoliceService.instance.onPause();
        super.onDestroy();
    }

    PoliceServiceListener listener = new PoliceServiceListener(){
        @Override
        public void onPushFly(String msg) {
            //收到飞投消息进行处理
            if(msg != null){
                PushFlyBean bean = gson.fromJson(msg, PushFlyBean.class);
                DeviceBean.DataBean.AppDeviceInfoBean infoBean = new DeviceBean.DataBean.AppDeviceInfoBean();
                infoBean.setDeviceId(bean.getDeviceId());
                infoBean.setName(bean.getName());
                infoBean.setAddress(bean.getAddress());
                infoBean.setParentId(bean.getParentId());
                showPushFlyDialog(infoBean, !bean.getParentId().equals("0"));
            }
        }

        @Override
        public void onExitApplication() {
            if(PoliceService.instance.isAppForeground()){
                PoliceService.instance.removeListener(listener);
                PoliceService.instance.onDestroy();
                try {
                    System.exit(0);
                } catch (Exception e) {
                }
            }
        }
    };

    private void showPushFlyDialog(final DeviceBean.DataBean.AppDeviceInfoBean infoBean, final boolean isNvr) {
        final CommonDialog dialog = new CommonDialog(ActivityUtils.getTopActivity());
        dialog.setTitle("系统提示")
                .setMessage("你有一条新的飞投视频消息")
                .setPositive("播放")
                .setSingle(false)
                .setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick() {
                Intent intent = new Intent(ActivityUtils.getTopActivity(), LiveVideoActivity.class);
                intent.putExtra(ConstantConfig.DEVICE_LIVE, infoBean);
                intent.putExtra(ConstantConfig.DEVICE_IS_NVR, isNvr);
                startActivity(intent);
                if(ActivityUtils.getTopActivity().getClass() == PushActivity.class){
                    ActivityUtils.getTopActivity().finish();
                }
                dialog.dismiss();
            }

            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        }).show();
    }

    private void showPushFlowDialog(final RtpAddressMsg rtpMsg) {
        final CommonDialog dialog = new CommonDialog(ActivityUtils.getTopActivity());
        dialog.setTitle("系统提示")
                .setMessage("管理平台需要查看你的视频，\n是否接受")
                .setPositive("同意")
                .setSingle(false)
                .setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick() {
                if (!ActivityUtils.isActivityExistsInStack(PushActivity.class)) {
                    Intent intent = new Intent();
                    intent.putExtra("msg", rtpMsg);
                    intent.setClass(MainActivity.this, PushActivity.class);
                    startActivity(intent);
                }
                dialog.dismiss();
            }

            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onBackPressed() {
        //双击退出程序
        if(search_layout.getVisibility() == View.VISIBLE){
            closeAnimation(mCloseAction, search_layout);
        }else if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toasty.success(MainActivity.this, getString(R.string.exit_click_again)).show();
                firstTime = secondTime;
            } else {
                finish();
                System.exit(0);
            }
        }
    }

    private void showPushFlyDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.exit_dialog_layout, null);
        closeImg = view.findViewById(R.id.close);
        cancel = view.findViewById(R.id.cancel);
        confirm = view.findViewById(R.id.confirm);
        closeImg.setOnClickListener(this);
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);//去掉圆角背景背后的棱角
        dialog.setCanceledOnTouchOutside(true);   //失去焦点dismiss
        dialog.show();
    }

    public void setAnimation() {
        //设置显示时的动画
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(200);
        //设置隐藏时的动画，监听动画结束后隐藏选择框
        mCloseAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mCloseAction.setDuration(200);
    }

    //如果输入法在窗口上已经显示，则隐藏，反之则显示
    public void showOrHide(boolean isShow) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            if(isShow){
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }else {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.person_view:
                drawer_layout.openDrawer(GravityCompat.START);
                break;
//            case R.id.user_setting:
//                Intent intent_set = new Intent(this,SettingPwdActivity.class);
//                intent_set.putExtra("page",ConstantConfig.USER_SETTING);
//                startActivity(intent_set);
//                break;
            case R.id.user_pwd:
                Intent intent_pwd = new Intent(this,SettingPwdActivity.class);
                intent_pwd.putExtra("page",ConstantConfig.USER_PASSWORD);
                startActivity(intent_pwd);
                break;
            case R.id.exit_btn:
                showPushFlyDialog();
                break;
            case R.id.search_view:
                if(search_layout.getVisibility() == View.GONE) {
                    search_layout.setVisibility(View.VISIBLE);
                    search_layout.startAnimation(mShowAction);
                    search_text.requestFocus();
                    showOrHide(true);
                }
                break;
            case R.id.close:
                dialog.dismiss();
                break;
            case R.id.cancel:
                dialog.dismiss();
                break;
            case R.id.confirm:
                dialog.dismiss();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(ConstantConfig.LOGIN_SHOW, false);
                startActivity(intent);
                finish();
                System.exit(0);
                break;
            default:
                break;
        }
    }

}
