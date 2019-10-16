package com.example.gb28181_videoplatform.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.SPUtils;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.MyUtil;
import com.example.gb28181_videoplatform.util.NetWorkUtil;
import com.example.gb28181_videoplatform.util.Toasty;

import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

/**
 * Created by 吴迪 on 2019/7/10.
 * 登陆界面
 */
public class LoginActivity extends Activity {

    CheckBox rem_checkbox;
    RelativeLayout setting_view;
    EditText username, password;
    ProgressDialog loadingDialog;

    LinearLayout login_layout, logo_layout;
    private ImageView ivLogo;

    String user, pwd;
    private long firstTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtil.immersiveNotificationBar(this, R.color.login_bg);
        setContentView(R.layout.activity_login);

        initView();
        //判断是登录还是注销后重登
        boolean isShow = getIntent().getBooleanExtra(ConstantConfig.LOGIN_SHOW, true);
        if(isShow){
            login_layout.setAlpha(0);
            ivLogo.setVisibility(View.VISIBLE);
            logo_layout.setVisibility(View.GONE);
            initAnim();
        }else {
            login_layout.setAlpha(1);
            setting_view.setAlpha(1.0f);
            ivLogo.setVisibility(View.GONE);
            logo_layout.setVisibility(View.VISIBLE);
        }

    }

    private void initView() {
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        ivLogo = findViewById(R.id.iv_logo);
        rem_checkbox = findViewById(R.id.rem_checkbox);
        setting_view = findViewById(R.id.setting_view);
        login_layout = findViewById(R.id.login_layout);
        logo_layout = findViewById(R.id.logo_layout);

        user = SPUtils.getInstance().getString("username");
        if(!TextUtils.isEmpty(user)) {
            username.setText(user);
            username.setSelection(user.length());
            rem_checkbox.setChecked(true);
        }
        pwd = SPUtils.getInstance().getString("password");
        if(!TextUtils.isEmpty(pwd)) {
            password.setText(pwd);
            password.setSelection(pwd.length());
        }

        setting_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SettingPwdActivity.class);
                intent.putExtra("page", ConstantConfig.USER_SETTING);
                startActivity(intent);
            }
        });
    }

    //初始化权限申请器
    private void initPermission() {
        List<PermissionItem> permissionItems = new ArrayList<>();
        permissionItems.add(new PermissionItem(Manifest.permission.CAMERA, "照相机", R.drawable.permission_ic_camera));
        permissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, "手机信息", R.drawable.permission_ic_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "定位", R.drawable.permission_ic_location));
        permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储", R.drawable.permission_ic_storage));
        permissionItems.add(new PermissionItem(Manifest.permission.RECORD_AUDIO, "麦克风", R.drawable.permission_ic_micro_phone));

        HiPermission.create(getApplicationContext())
                .permissions(permissionItems)
                .filterColor(ResourcesCompat.getColor(getResources(), R.color.activity_title, getTheme()))
                .style(R.style.PermissionBlueStyle)
                .msg("为了您能正常使用多维感知指挥调度，需要使用以下权限，请确定")
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermission();
    }

    /**
     * 初始化logo图片以及底部注册、登录的按钮动画
     */
    private void initAnim() {
        //以控件自身所在的位置为原点，从下方距离原点200像素的位置移动到原点
        ObjectAnimator tranLogin = ObjectAnimator.ofFloat(login_layout, "translationY", 200, 0);
        ObjectAnimator tranSetting = ObjectAnimator.ofFloat(setting_view, "translationY", 0, 0);
        //将注册、登录的控件alpha属性从0变到1
        ObjectAnimator alphaLogin = ObjectAnimator.ofFloat(login_layout, "alpha", 0, 1);
        ObjectAnimator alphaSetting = ObjectAnimator.ofFloat(setting_view, "alpha", 0, 1);
        final AnimatorSet bottomAnim = new AnimatorSet();
        bottomAnim.setDuration(400);
        //同时执行控件平移和alpha渐变动画
        bottomAnim.play(tranLogin).with(alphaLogin).with(tranSetting).with(alphaSetting);

        //获取屏幕高度
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;

        //通过测量，获取ivLogo的高度
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        ivLogo.measure(w, h);
        int logoHeight = ivLogo.getMeasuredHeight();

        //初始化ivLogo的移动和缩放动画
        float transY = (screenHeight - logoHeight) * 0.8f;
        //ivLogo向上移动 transY 的距离
        ObjectAnimator tranLogo = ObjectAnimator.ofFloat(ivLogo, "translationY", 0, -transY);
        //ivLogo在X轴和Y轴上都缩放0.75倍
        ObjectAnimator scaleXLogo = ObjectAnimator.ofFloat(ivLogo, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleYLogo = ObjectAnimator.ofFloat(ivLogo, "scaleY", 1f, 0.95f);
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.setDuration(400);
        logoAnim.play(tranLogo).with(scaleXLogo).with(scaleYLogo);
        logoAnim.start();
        logoAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //待ivLogo的动画结束后,开始播放底部注册、登录按钮的动画
                bottomAnim.start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //双击退出程序
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            Toasty.success(LoginActivity.this, getString(R.string.exit_click_again)).show();
            firstTime = secondTime;
        } else {
            finish();
        }
    }

    private void showDialog(boolean isShowOrHide) {
        if(isShowOrHide){
            loadingDialog = ProgressDialog.show(this, null, "登录中...");
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingDialog.show();// 设置圆形旋转进度条
        }else {
            loadingDialog.dismiss();
        }
    }

    public void login(View view) {
        String editTextUser = username.getText().toString();
        String editTextPwd = password.getText().toString();
        if(TextUtils.isEmpty(editTextUser)) {
            Toasty.info(LoginActivity.this,getResources().getString(R.string.toast_login_account)).show();
            return;
        }
        if(TextUtils.isEmpty(editTextPwd)) {
            Toasty.info(LoginActivity.this,getResources().getString(R.string.toast_login_password)).show();
            return;
        }
        if (rem_checkbox.isChecked()) {
            SPUtils.getInstance().put("username", username.getText().toString());
            SPUtils.getInstance().put("password", password.getText().toString());
        }else {
            SPUtils.getInstance().remove("username");
            SPUtils.getInstance().remove("password");
        }
        //判断是否已经配置了sip和web
        String sip_ip = SPUtils.getInstance().getString("remoteIp");
        String web_ip = SPUtils.getInstance().getString("webIp");
        String local_id = SPUtils.getInstance().getString("mySipName");
        if(!TextUtils.isEmpty(sip_ip) && !TextUtils.isEmpty(web_ip) && !TextUtils.isEmpty(local_id)){
            //判断是否有网络
            Log.d("网络链接", "netWork: " + NetWorkUtil.isNetworkConnected(getApplicationContext()) + "wifi:" + NetWorkUtil.isWifiConnected(getApplicationContext()));

            if(!NetWorkUtil.isNetworkConnected(getApplicationContext()) || !NetWorkUtil.isWifiConnected(getApplicationContext())){
                Toasty.info(LoginActivity.this, "请检查网络连接").show();
            }else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }else {
            showNormalDialog();
        }
    }

    private void showNormalDialog(){
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        normalDialog.setTitle("提示");
        normalDialog.setIcon(R.mipmap.point);
        normalDialog.setMessage("使用前需要配置SIP服务和WEB服务,否则应用将无法正常使用");
        normalDialog.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(LoginActivity.this,SettingPwdActivity.class);
                        intent.putExtra("page", ConstantConfig.USER_SETTING);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        normalDialog.create().show();
    }

}
