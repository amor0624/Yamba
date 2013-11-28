package com.marakana.yamba;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class UpdaterService extends IntentService {
	static final String TAG = "UpdaterService";
	static final String NEW_STATUS_INTENT = "com.marakana.yamba.NEW_STATUS";
	static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	public static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.RECIEVE_TIMELINE_NOTIFICATIONS";

	private NotificationManager notiManager;
	private Notification noti;

	public UpdaterService() {
		super(TAG);
		Log.d(TAG, "Updater Service Created");
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Intent intent;
		this.notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		this.noti = new Notification(android.R.drawable.stat_notify_chat, "", 0);
		Log.d(TAG, "onHandleIntent'ing");

		YambaApplication yamba = (YambaApplication) getApplication();
		int newUpdates = yamba.fetchStatusUpdates();
		if (newUpdates > 0) {
			Log.d(TAG, "We have a new status");
			intent = new Intent(NEW_STATUS_INTENT);
			intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);
			sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS);
			sendTimelineNotification(newUpdates);
		}
	}

	/**
	 * Creates a notification in the notification bar telling user there are new
	 * messages
	 * 
	 * @param timelineUpdateCount
	 *            : Number of new statuses
	 */
	private void sendTimelineNotification(int timelineUpdateCount) {
		Log.d(TAG, "sendTimelineNotification'ing");

		PendingIntent pendingIntent = PendingIntent.getActivity(this, -1,
		        new Intent(this, TimelineActivity.class),
		        PendingIntent.FLAG_UPDATE_CURRENT);
		this.noti.when = System.currentTimeMillis();
		this.noti.flags |= Notification.FLAG_AUTO_CANCEL;
		CharSequence notiTitle = this.getText(R.string.msgNotificationTitle);
		CharSequence notiSummary = this.getString(R.string.msgNotificationMessage, timelineUpdateCount);
		
		this.noti.setLatestEventInfo(this, notiTitle, notiSummary, pendingIntent);
		this.notiManager.notify(0, this.noti);
		
		Log.d(TAG, "sendNotificationed");
	}

}
