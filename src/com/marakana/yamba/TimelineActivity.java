package com.marakana.yamba;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
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

public class TimelineActivity extends BaseActivity implements
        LoaderCallbacks<Cursor> {
	ListView listTimeline;
	SimpleCursorAdapter adapter;
	static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_USER,
	        StatusData.C_TEXT };
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

		// Create the Adapter
		adapter = new SimpleCursorAdapter(this, R.layout.row, null, FROM, TO, 0);
		adapter.setViewBinder(VIEW_BINDER);
		listTimeline.setAdapter(adapter);

		// create new status receiver
		receiver = new TimelineReceiver();
		filter = new IntentFilter(UpdaterService.NEW_STATUS_INTENT);

		// initialize (or reload) cursor for this activity
		getLoaderManager().initLoader(0, null, this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Register the receiver
		super.registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS,
		        null);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// UNregister the receiver
		unregisterReceiver(receiver);
	}

	// Responsible for reloading the cursor and the list;
	private void resetList() {
		// restart the cursor for this activity to get recently retrieved status
		getLoaderManager().restartLoader(0, null, this);
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, StatusProvider.CONTENT_URI, null, null,
		        null, StatusData.GET_ALL_ORDER_BY_STRING);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		// swap new cursor in
		adapter.swapCursor(newCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// called when the last cursor is provided to onLoadFinished
		// above is about to be closed. we need to make sure we are no longer using it
		adapter.swapCursor(null);
	}

	class TimelineReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			resetList();
			Log.d("TimelineReceiver", "onReceived");
		}
	}
}
