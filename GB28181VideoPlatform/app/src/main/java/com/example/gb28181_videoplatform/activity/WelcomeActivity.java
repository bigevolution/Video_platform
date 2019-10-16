package com.example.gb28181_videoplatform.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;

import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.MyUtil;

/**
 * Created by 吴迪 on 2019/7/10.
 * 启动页
 */
public class WelcomeActivity extends Activity {

    private Handler handler = new Handler();
    TextView version_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //获取版本号展示
        version_name = findViewById(R.id.version_name);
        String version = "V " + MyUtil.getVersionName(getApplicationContext());
        version_name.setText(version);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gotoLogin();
            }
        }, 500);
    }

    /**
     * 前往登录页
     */
    private void gotoLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(ConstantConfig.LOGIN_SHOW, true);
        startActivity(intent);
        finish();
        //取消界面跳转时的动画，使启动页的logo图片与注册、登录主页的logo图片完美衔接
        overridePendingTransition(0, 0);
    }

    /**
     * 屏蔽物理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            //If token is null, all callbacks and messages will be removed.
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

}