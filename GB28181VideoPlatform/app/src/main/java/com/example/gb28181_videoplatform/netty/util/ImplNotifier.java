package com.example.gb28181_videoplatform.netty.util;

import java.util.concurrent.CopyOnWriteArrayList;

public class ImplNotifier {

	PoliceService mParent;
	CopyOnWriteArrayList<PoliceServiceListener> mListeners = new CopyOnWriteArrayList<>();
	
	public ImplNotifier(PoliceService parent){
		mParent = parent;
	}
	
	
	public void addListener(PoliceServiceListener listener){
		synchronized (this){
			if (mListeners.contains(listener)){
				return;
			}
			mListeners.add(listener);
		}
	}
	
	public synchronized void removeListener(PoliceServiceListener listener){
		if(mListeners.contains(listener)){
			mListeners.remove(listener);
		}
	}

	public void notifyPushFly(final String msg){
		mParent.mHandler.post(new Runnable(){
			@Override
			public void run() {
				for (PoliceServiceListener l : mListeners){
					l.onPushFly(msg);
				}
			}			
		});	
	}

	public void notifyUpdateDevice(final String msg){
		mParent.mHandler.post(new Runnable(){
			@Override
			public void run() {
				for (PoliceServiceListener l : mListeners){
					l.onUpdateDevice(msg);
				}
			}
		});
	}

	public void notifyLoginResultChanged(final boolean isSucccess,final boolean isFaceLogin, final PoliceOfficerInfo info) {
		mParent.mHandler.post(new Runnable(){
			@Override
			public void run() {
				for (PoliceServiceListener l : mListeners){
					l.onLoginResult(isSucccess,isFaceLogin,info);
				}
			}
		});

	}

	public void notifyLoginStateChanged(final boolean isSucccess) {
		mParent.mHandler.post(new Runnable(){
			@Override
			public void run() {
				for (PoliceServiceListener l : mListeners){
					l.onLogining(isSucccess);
				}
			}
		});

	}

	public void notifyMoveDistanceLargeThanValve(){
		mParent.mHandler.post(new Runnable(){
			@Override
			public void run() {
				for (PoliceServiceListener l : mListeners){
					l.onMoveDistanceLargeThanValve();
				}
			}
		});
	}

	public void notifyExitApplication(){
		mParent.mHandler.post(new Runnable(){
			@Override
			public void run() {
				for (PoliceServiceListener l : mListeners){
					l.onExitApplication();
				}
			}
		});
	}

}
