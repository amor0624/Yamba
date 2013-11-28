package com.marakana.yamba;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UpdaterService extends IntentService {
	static final String TAG = "UpdaterService";
	static final String NEW_STATUS_INTENT = "com.marakana.yamba.NEW_STATUS";
	static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	public static final String RECEIVE_TIMELINE_NOTIFICATIONS = 
			"com.marakana.yamba.RECIEVE_TIMELINE_NOTIFICATIONS";

	public UpdaterService() {
		super(TAG);
		Log.d(TAG, "Updater Service Created");
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Intent intent;
		Log.d(TAG, "onHandleIntent'ing");
		
		YambaApplication yamba = (YambaApplication) getApplication();
		int newUpdates = yamba.fetchStatusUpdates();
		if (newUpdates > 0) {
			Log.d(TAG, "We have a new status");
			intent = new Intent(NEW_STATUS_INTENT);
			intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);
			sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS);
		}
	}

}
