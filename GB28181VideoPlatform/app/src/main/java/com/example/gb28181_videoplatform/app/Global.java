package com.example.gb28181_videoplatform.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.SPUtils;

public class Global {

	private static Global global ;

	private String deviceId = PhoneUtils.getDeviceId();

	private Global() {

	}

	public static Global getInstance()
	{
		if(global == null)
		{
			synchronized (Global.class)
			{
				if(global == null)
				{
					global = new Global();
				}
			}
		}
		return global ;

	}
	public static Context context;
	public static Application application;
	public static Activity activity;

	public String getDeviceId(){
		return deviceId;
	}

}
