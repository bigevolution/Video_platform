package com.example.gb28181_videoplatform.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.blankj.utilcode.util.Utils;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.netty.util.PoliceService;
import com.example.gb28181_videoplatform.util.Toasty;

/**
 * Created by 吴迪 on 2019/7/11.
 * 初始化
 */
public class GB28181Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //bugly上报
//        CrashReport.initCrashReport(getApplicationContext(), "b31cc64bb7", false);

        Utils.init(this);

        PoliceService.instance = new PoliceService();
        PoliceService.instance.onCreate(this);

        Toasty.Config.getInstance().
                setSuccessColor(getResources().getColor(R.color.activity_title)).
                setInfoColor(getResources().getColor(R.color.login_bg)).
                setErrorColor(getResources().getColor(R.color.colorToastError)).apply();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
