package com.marakana.yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class YambaApplication extends Application implements
        OnSharedPreferenceChangeListener {
	private static final String TAG = YambaApplication.class.getSimpleName();
	public Twitter twitter;
	private SharedPreferences prefs;
	private boolean serviceRunning;
	private StatusData statusData;

//	private boolean inTimeline;

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		this.statusData = new StatusData(this);
		Log.i(TAG, "onCreated");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		this.statusData.close();
		Log.i(TAG, "onTerminated");
	}

	// CHECKPOINT
	// public boolean startOnBoot(){
	// return this.prefs.getBoolean("startOnBoot", false);
	// }

	public SharedPreferences getPrefs() {
		return prefs;
	}

	public StatusData getStatusData() {
		return statusData;
	}

	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

//CHECKPOINT
//	public boolean isInTimeline(){
//		return inTimeline;
//	}
//	
//	public void setInTimeline(boolean inTimeline){
//		this.inTimeline = inTimeline;
//	}

	public synchronized Twitter getTwitter() {
		if (this.twitter == null) {
			String username, password, apiRoot;
			username = prefs.getString("username", null);
			password = prefs.getString("password", null);
			apiRoot = prefs.getString("apiRoot",
			        "http://yamba.marakana.com/api");

			if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)
			        && !TextUtils.isEmpty(apiRoot)) {
				// connect to twitter.com
				twitter = new Twitter(username, password);
				twitter.setAPIRootUrl(apiRoot);
			}
		}
		return twitter;
	}

	// Connects to the online service and puts the latest statuses into DB.
	// Returns the count of new statuses
	public synchronized int fetchStatusUpdates() {
		Log.d(TAG, "Fetching Status Updates");
		Twitter twitter = this.getTwitter();
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return 0;
		}
		try {
			List<Status> statusUpdates = twitter.getHomeTimeline();
			long latestStatusCreatedAtTime = this.getStatusData()
			        .getLatestStatusCreatedAtTime();
			int count = 0;

			// loop over the timeline and print it out
			ContentValues values = new ContentValues();
			for (Status status : statusUpdates) {
				// insert into database
				//values.clear();
				values.put(StatusData.C_ID, status.getId());
				long createdAt = status.getCreatedAt().getTime();
				values.put(StatusData.C_CREATED_AT, createdAt);
				values.put(StatusData.C_TEXT, status.getText());
				values.put(StatusData.C_USER, status.getUser().getName());
				Log.d(TAG, "Got UpdaterService with id " + status.getId()
				        + ". Saving");
				this.getStatusData().insertOrIgnore(values);
				if (latestStatusCreatedAtTime < createdAt) {
					count++;
				}
			}
			Log.d(TAG, count > 0 ? "Got " + count + " status upates"
			        : "No new status updates");
			return count;
		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch status updates", e);
			return 0;
		}
	}

	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences arg0,
	        String arg1) {
		this.twitter = null;
	}

}
