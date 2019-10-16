package com.example.gb28181_videoplatform.netty.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.example.gb28181_videoplatform.app.Global;
import com.example.gb28181_videoplatform.netty.client.FutureListener;
import com.example.gb28181_videoplatform.netty.client.NettyClient;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

@SuppressLint("NewApi")
public class PoliceService {
	Logger mLog= LoggerFactory.getLogger(PoliceService.class);

	public static PoliceService instance;

	public PoliceService() {

	}

	public Context mContext;
	String mAccount;

	/**
	 * 阿里真实环境
	 */
	public String mServerAddr="http://47.98.142.126";

	public ImplNotifier mNotifier;

	public Handler mHandler;
	private HandlerThread mBackgroundThread;
	public Handler mBackgroundHandler;
	private ConnectivityManager mConnectivityMng;
	public Timer timer;

	public boolean mIsLoginSuccess;
	private boolean mIsAppForeground;
	private String mPatrolId;

	public NettyClient mWebSocketClient;
	private boolean isFirstHeartBeat=true;
	public int mMenuIndex=1;
	public boolean mIsStart;
	private SelfCheckImpl mSelfCheck;

	public void onCreate(Context context) {
		InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
		mContext = context;
		mHandler = new Handler();
		mNotifier = new ImplNotifier(this);
		mBackgroundThread = new HandlerThread("PoliceBackground", Process.THREAD_PRIORITY_BACKGROUND);
		mBackgroundThread.start();
		mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
		this.addListener(this.mBluetoothMsgListener);
		mSelfCheck=new SelfCheckImpl(this);
		timer = new Timer();

		mWebSocketClient=new NettyClient(this);
	}

	public void startWork(){
		if(!mIsStart){
			mSelfCheck.setDeviceInfo();
			mWebSocketClient.startWork();
			mIsStart=true;
		}else {
			mWebSocketClient.startWork();
		}
		mBackgroundHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mWebSocketClient.login();
			}
		}, 1000);
	}

	public void onPause() {
		mIsAppForeground=false;
	}

	public void onResume() {
		mIsAppForeground=true;
	}

	public void onDestroy() {
		mWebSocketClient.onDestory();
	}

	private void setAccount(String account) {
		this.mAccount = account;
	}

	public boolean isLoginAuthed() {
		return mIsLoginSuccess;
	}

	public void setIsLoginSuccess(boolean isLoginSuccess) {
		this.mIsLoginSuccess = isLoginSuccess;
		if(isLoginSuccess){
			mNotifier.notifyLoginStateChanged(false);
		}else {
		}
	}

	public void addListener(PoliceServiceListener listener) {
		mNotifier.addListener(listener);
	}

	public void removeListener(PoliceServiceListener listener) {
		mNotifier.removeListener(listener);
	}

	public void runOnUI(Runnable run) {
		this.mHandler.post(run);
	}

	public void runOnUIDelay(Runnable runnable,long delayMills){
		this.mHandler.postDelayed(runnable,delayMills);
	}

	public void login(String account) {
		mAccount = account;
		this.setAccount(account);
	}

	PoliceServiceListener mBluetoothMsgListener = new PoliceServiceListener() {

		@Override
		public void onLoginResult(boolean isSuccess,boolean isFaceLogin, PoliceOfficerInfo policeOfficerInfo) {
			if(!isSuccess){
				setIsLoginSuccess(false);
				return;
			}
			if (mIsLoginSuccess) {
				return;
			}
			setIsLoginSuccess(true);
			login(policeOfficerInfo.getPoliceId());
		}

		@Override
		public void onMoveDistanceLargeThanValve() {
			if(mIsLoginSuccess){
				reportLocation();
			}else {
				mBackgroundHandler.post(new Runnable() {
					@Override
					public void run() {
						mWebSocketClient.login();
					}
				});
			}
		}
	};

	public DeviceInfo getDeviceInfo() {
		return mSelfCheck.getDeviceInfo();
	}

	public void reportLocation() {
		JsonObject location = new JsonObject();
		location.addProperty("latitude", "31.9903000000");
		location.addProperty("longitude", "118.7377400000");
		Request request=new Request();
		request.setType(CODE.PACKETYPE__REPORT_LOCATION.code);
		request.setSessionId(Global.getInstance().getDeviceId());
		request.setMessage(location.toString());
		mLog.debug(request.toString());
		sendWebSocketRequest(request, null);
	}

	public boolean isAppForeground() {
		return mIsAppForeground;
	}

	public void runOnBackgroundDelay(Runnable runnable,long delayMills){
		mBackgroundHandler.postDelayed(runnable,delayMills);
	}

	public void runOnTimer(TimerTask timerTask, long delayMills){
		timer.schedule(timerTask, delayMills);
	}

	public void runOnTimerPeriod(TimerTask timerTask, long delayMills, long period){
		timer.schedule(timerTask, delayMills, period);
	}

	public String getServerAddr() {
		return mServerAddr;
	}

	public String getPatrolId() {
		return mPatrolId;
	}

	public void sendWebSocketRequest(final Request request, final FutureListener listener){
		mBackgroundHandler.post(new Runnable() {
			@Override
			public void run() {
				mWebSocketClient.sendMessage(request,listener);
			}
		});
	}

}
