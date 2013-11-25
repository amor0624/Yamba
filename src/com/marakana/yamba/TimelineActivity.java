package com.marakana.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineActivity extends BaseActivity {
	Cursor cursor;
	ListView listTimeline;
	SimpleCursorAdapter adapter;
	static final String[] FROM = { StatusData.C_CREATED_AT,
	        StatusData.C_USER, StatusData.C_TEXT };
	static final int[] TO = { R.id.textCreatedAt, R.id.textUser, R.id.textText };
	static final String SEND_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.SEND_TIMELINE_NOTIFICATIONS";
	TimelineReceiver receiver;
	IntentFilter filter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);

		// Check whether preferences have been set
		if (yamba.getPrefs().getString("username", null) == null) {
			startActivity(new Intent(this, PrefsActivity.class));
			Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG)
			        .show();
		}
		// Find your views
		listTimeline = (ListView) findViewById(R.id.listTimeline);
		
		//create new status receiver
		receiver = new TimelineReceiver();
		filter = new IntentFilter(UpdaterService.NEW_STATUS_INTENT);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// close the database
		yamba.getStatusData().close();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// setup list
		this.setupList();
		
		//Register the receiver
		super.registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS, null);
	}

	@Override
    protected void onPause() {
	    super.onPause();
	    
	    //UNregister the receiver
	    unregisterReceiver(receiver);;
    }

	// Responsible for fetching data and setting up the list and the adapter
	private void setupList() {
		// Get the data from the database
		cursor = yamba.getStatusData().getStatusUpdates();
		startManagingCursor(cursor);

		// Create the Adapter
		adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listTimeline.setAdapter(adapter);
	}

	// View binder constant to inject business logic that converts a timestamp
	// to relative time
	static final ViewBinder VIEW_BINDER = new ViewBinder() {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view.getId() != R.id.textCreatedAt)
				return false;

			// Update the created at text to relative time
			long timestamp = cursor.getLong(columnIndex);
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(
			        view.getContext(), timestamp);
			((TextView) view).setText(relTime);
			return true;
		}
	};
	
	class TimelineReceiver extends BroadcastReceiver {

		@Override
        public void onReceive(Context context, Intent intent) {
	        cursor.requery();
	        adapter.notifyDataSetChanged();
	        Log.d("TimelineReceiver", "onReceived");
        }
		
	}

}
