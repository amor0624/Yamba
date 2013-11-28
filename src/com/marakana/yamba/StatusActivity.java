package com.marakana.yamba;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends BaseActivity implements OnClickListener,
		TextWatcher, LocationListener {
	private static final String TAG = "StatusActivity";
	private static final long LOCATION_MIN_TIME = 3600000; // ONE HOUR
	private static final float LOCATION_MIN_DISTANCE = 1000; // one kilometer
	EditText editText;
	Button updateButton;
	TextView textCount;
	LocationManager locManager;
	Location loc;
	String provider;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);
		// Find views
		editText = (EditText) findViewById(R.id.editText);
		updateButton = (Button) findViewById(R.id.buttonUpdate);
		updateButton.setOnClickListener(this);

		textCount = (TextView) findViewById(R.id.textCount);
		textCount.setText(Integer.toString(140));
		textCount.setTextColor(Color.GREEN);
		editText.addTextChangedListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (locManager != null)
			locManager.removeUpdates(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Set up location information
		provider = yamba.getProvider();
		if (!YambaApplication.LOCATION_PROVIDER_NONE.equals(provider)) {
			locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		}
		if (locManager != null) {
			loc = locManager.getLastKnownLocation(provider);
			locManager.requestLocationUpdates(provider, LOCATION_MIN_TIME,
					LOCATION_MIN_DISTANCE, this);
			Log.d(TAG, String.format("Lat: %f\nLong: %f", loc.getLatitude(), loc.getLongitude()));
		}
	}

	// Called when button is clicked
	public void onClick(View v) {
		// previously
		String status = editText.getText().toString();
		new PostToTwitter().execute(status);
		Log.d(TAG, "onClicked");
		// try {
		// getTwitter().setStatus(editText.getText().toString());
		// } catch (TwitterException e) {
		// Log.d(TAG, "Twitter setStatus failed: " + e);
		// }
	}

	// Asynchronously posts to twitter
	class PostToTwitter extends AsyncTask<String, Integer, String> {
		// Called to initiate the background activity
		@Override
		protected String doInBackground(String... statuses) {
			try {
				// Check if we have the location
				if (loc != null) {
					double latlong[] = { loc.getLatitude(), loc.getLongitude() };
					yamba.getTwitter().setMyLocation(latlong);
				}
				Twitter.Status status = yamba.getTwitter().updateStatus(
						statuses[0]);
				return status.text;
			} catch (TwitterException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
				return "Failed to post";
			}
		}

		// Called when there's a status to be updated
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// Not used in this case
		}

		// Called once the background activity has completed
		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG)
					.show();
		}
	}

	// TextWatcher methods
	public void afterTextChanged(Editable statusText) {
		int count = 140 - statusText.length();
		textCount.setText(Integer.toString(count));
		textCount.setTextColor(Color.GREEN);

		if (count < 10)
			textCount.setTextColor(Color.YELLOW);
		if (count < 0)
			textCount.setTextColor(Color.RED);
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void onLocationChanged(Location loc) {
		this.loc = loc;
		Log.d(TAG, String.format("Lat: %f\nLong: %f", loc.getLatitude(), loc.getLongitude()));
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (this.provider.equals(provider))
			locManager.removeUpdates(this);
		Log.d(TAG, provider + " disabled"); 
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (this.provider.equals(provider))
			locManager.requestLocationUpdates(this.provider, LOCATION_MIN_TIME,
					LOCATION_MIN_DISTANCE, this);
		Log.d(TAG, provider + " enabled"); 
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

}
