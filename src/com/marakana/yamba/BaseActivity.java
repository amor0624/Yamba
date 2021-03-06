package com.marakana.yamba;

import com.marakana.yamba.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {
	YambaApplication yamba;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		yamba = (YambaApplication) getApplication();
	}

	// Called only once first time menu is clicked on
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	// Called every time user clicks on a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemPrefs:
			startActivity(new Intent(this, PrefsActivity.class)
			        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.itemRefresh:
			startService(new Intent(this, UpdaterService.class));
			break;
		case R.id.itemPurge:
			((YambaApplication) getApplication()).getStatusData().delete();
			Toast.makeText(this, R.string.msgAllDataPurged, Toast.LENGTH_LONG)
			        .show();
			//adding broadcast to update status change
			sendBroadcast(new Intent(UpdaterService.NEW_STATUS_INTENT));
			break;
		case R.id.itemTimeline:
			startActivity(new Intent(this, TimelineActivity.class).addFlags(
			        Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(
			        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.itemStatus:
			startActivity(new Intent(this, StatusActivity.class)
			        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		}
		return true;
	}

//	// Called every time menu is opened
//	@Override
//	public boolean onMenuOpened(int featureId, Menu menu) {
//		MenuItem toggleItem = menu.findItem(R.id.itemToggleService);
//		if (yamba.isServiceRunning()) {
//			toggleItem.setTitle(R.string.titleServiceStop);
//			toggleItem.setIcon(android.R.drawable.ic_media_pause);
//		} else {
//			toggleItem.setTitle(R.string.titleServiceStart);
//			toggleItem.setIcon(android.R.drawable.ic_media_play);
//		}
//		return true;
//	}

}
