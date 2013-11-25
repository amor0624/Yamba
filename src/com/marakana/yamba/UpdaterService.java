package com.marakana.yamba;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
	static final String TAG = "UpdaterService";
	static final String NEW_STATUS_INTENT = "com.marakana.yamba.NEW_STATUS";
	static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.RECEIVE_TIMELINE_NOTIFICATIONS";
	static final int DELAY = 60000; //a minute
	private boolean runFlag = false;
	private Updater updater;
	private YambaApplication yamba;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		updater = new Updater();
		yamba = (YambaApplication) getApplication();
		
		Log.d(TAG, "onCreated");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		this.runFlag = false;
		this.updater.interrupt();
		this.updater = null;
		this.yamba.setServiceRunning(false);
		Log.d(TAG, "onDestroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//super.onStartCommand(intent, flags, startId);
		if (!runFlag){
			this.runFlag = true;
			this.updater.start();
			//this.yamba.setServiceRunning(true);
			((YambaApplication) super.getApplication()).setServiceRunning(true);
			
			Log.d(TAG, "onStarted");
		}

		return START_STICKY;
	}
	
	/*
	 * Thread that performs the actual update from the online service
	 */
	private class Updater extends Thread {
		Intent intent;
		
		public Updater() {
			super("UpdaterService-Updater");
		}
		
		public void run() {
			UpdaterService updaterService = UpdaterService.this;
			while (updaterService.runFlag) {
				Log.d(TAG, "Running background thread");
				try {
					YambaApplication yamba = (YambaApplication) updaterService.getApplication();
					int newUpdates = yamba.fetchStatusUpdates();
					if (newUpdates > 0) {
						Log.d(TAG, "We have a new status");
						intent = new Intent(NEW_STATUS_INTENT);
						intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);
						updaterService.sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS);
					}
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	}

}
