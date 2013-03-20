/*******************************************************************************
 * Copyright 2013 Keith Cassidy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.keithcassidy.finishline;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.maps.model.LatLng;
import com.keithcassidy.finishline.R.id;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SetFromGPSActivity extends Activity 
{
	private static final String TAG = SetFromGPSActivity.class.getSimpleName();

	private double metersStarboard;
	private double metersForward;
	private WakeLock wakeLock;
	private ExecutorService executorService;
	private LocationManager locationManager;
	private Location lastLocation;
	private long timeOfNowClick;
	private float bearingOfNowClick;
	private LatLng rawBuoyPos = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_from_gps);

		metersStarboard = PreferencesUtils.getDouble(this, R.string.meters_starboard_key, PreferencesUtils.METERS_STARBOARD_DEFAULT);
		metersForward = PreferencesUtils.getDouble(this, R.string.meters_forward_key, PreferencesUtils.METERS_FORWARD_DEFAULT);

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		executorService = Executors.newSingleThreadExecutor();

		timeOfNowClick = 0;
		lastLocation = null;

		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )) 
		{ 
			updateGPSReady(false, false);
		}
		else
		{
			updateGPSReady(false, true);
		}
		updateDistanceControls();
		acquireWakeLock();
		registerLocationListener();

	}


	@Override
	protected void onDestroy() 
	{
		unregisterLocationListener();
		releaseWakeLock();
		executorService.shutdown();
		super.onDestroy();
	}

	private void registerLocationListener() 
	{
		unregisterLocationListener();

		try 
		{
			Log.e(TAG, "registerLocationListener.");
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);
		} 
		catch (RuntimeException e) 
		{
			Log.e(TAG, "Could not request Location Updates.", e);
		}
	}

	private void unregisterLocationListener() 
	{
		if (locationManager == null) 
		{
			Log.e(TAG, "locationManager is null.");
			return;
		}
		locationManager.removeUpdates(locationListener);
	}

	private void acquireWakeLock() 
	{
		try 
		{
			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			if (powerManager == null) 
			{
				Log.e(TAG, "powerManager is null.");
				return;
			}

			if (wakeLock == null) 
			{
				wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
				if (wakeLock == null) 
				{
					Log.e(TAG, "wakeLock is null.");
					return;
				}
			}

			if (!wakeLock.isHeld()) 
			{
				wakeLock.acquire();
				if (!wakeLock.isHeld()) 
				{
					Log.e(TAG, "Unable to hold wakeLock.");
				}
			}

		} 
		catch (RuntimeException e) 
		{
			Log.e(TAG, "Caught RuntimeException exception in acquireWakeLock", e);
		}
	}

	private void releaseWakeLock() 
	{
		if (wakeLock != null && wakeLock.isHeld()) 
		{
			wakeLock.release();
			wakeLock = null;
		}
	}

	private void updateDistanceControls()
	{
		TextView forward = (TextView)findViewById(id.textViewForeAft);
		if( forward != null)
		{
			String message = getString(R.string.meters_forward);
			if( metersForward < 0 )
			{
				message = getString(R.string.meters_astern);
			}
			forward.setText(String.format("%.1f %s", Math.abs(metersForward), message));
		}

		TextView starboard = (TextView)findViewById(id.textViewPortStarboard);
		if( starboard != null)
		{
			String message = getString(R.string.meters_starboard);
			if( metersStarboard < 0 )
			{
				message = getString(R.string.meters_port);
			}
			starboard.setText(String.format("%.1f %s", Math.abs(metersStarboard), message));
		}

		showBuoyPosition();
	}

	public void onNudgeForward(View v)
	{
		metersForward += 0.5;
		PreferencesUtils.setDouble(this, R.string.meters_forward_key, metersForward );
		updateDistanceControls();
	}

	public void onNudgeAstern(View v)
	{
		metersForward -= 0.5;
		PreferencesUtils.setDouble(this, R.string.meters_forward_key, metersForward );
		updateDistanceControls();
	}

	public void onNudgePort(View v)
	{
		metersStarboard -= 0.5;
		PreferencesUtils.setDouble(this, R.string.meters_starboard_key, metersStarboard );
		updateDistanceControls();
	}

	public void onNudgeStarboard(View v)
	{
		metersStarboard += 0.5;
		PreferencesUtils.setDouble(this, R.string.meters_starboard_key, metersStarboard );
		updateDistanceControls();
	}

	public void onSetPositionNowClick(View v)
	{		
		timeOfNowClick = System.currentTimeMillis();

		Log.d(TAG, "onSetPositionNowClick " + Long.toString(timeOfNowClick));
	}


	private void updateGPSReady(boolean isReady, boolean isEnabled)
	{
		TextView textViewGPSReady = (TextView)findViewById(id.textViewGPSStatus);
		Button getPos = (Button)findViewById(id.buttonSetGPSNow);
		TextView accuracy = (TextView)findViewById(id.textViewAccuracy);
		TextView accuracyLabel = (TextView)findViewById(id.textViewAccuracyLabel);
		if( getPos != null && textViewGPSReady != null && accuracy!= null && accuracyLabel != null)
		{
			getPos.setEnabled(isReady);

			if( isReady )
			{
				textViewGPSReady.setText(getString(R.string.gps_status_ready));
				accuracy.setVisibility(TextView.VISIBLE);
				accuracyLabel.setVisibility(TextView.VISIBLE);
			}
			else
			{
				if( isEnabled )
				{
					textViewGPSReady.setText(getString(R.string.gps_status_waiting));
				}
				else
				{
					textViewGPSReady.setText(getString(R.string.gps_status_disabled));
				}
				accuracy.setVisibility(TextView.INVISIBLE);
				accuracyLabel.setVisibility(TextView.INVISIBLE);
			}

		}
	}

	private void SetBuoyLocationFromClick(Location location)
	{
		Location nextLocation = location;
		nextLocation.setTime(System.currentTimeMillis());
		bearingOfNowClick = lastLocation.getBearing();
		rawBuoyPos = LocationUtils.getIntermediatePosition(timeOfNowClick, lastLocation, nextLocation);
		showBuoyPosition();

		Log.d(TAG, "Location times, first, now, next " + lastLocation.getTime() + " " + timeOfNowClick + " " + nextLocation.getTime());
		Log.d(TAG, "Latitudes, first, now, next " + lastLocation.getLatitude() + " " + rawBuoyPos.latitude + " " + nextLocation.getLatitude());
		Log.d(TAG, "longitudes, first, now, next " + lastLocation.getLongitude() + " " + rawBuoyPos.longitude + " " + nextLocation.getLongitude());
		Log.d(TAG, "bearing " + lastLocation.getBearing());
	}

	public void closeAndReturnBuoy(View view)
	{
		LatLng adjustedBuoyPos = LocationUtils.adjustPositionByOffsets(rawBuoyPos, metersForward, metersStarboard, bearingOfNowClick);

		Intent returnIntent = getIntent();
		returnIntent.putExtra(getString(R.string.intent_data_buoy_position), adjustedBuoyPos);
		setResult(RESULT_OK, returnIntent);
		finish();
	}	

	private void showBuoyPosition() 
	{
		if( rawBuoyPos != null )
		{
			//adjust by offsets
			LatLng adjustedBuoyPos = LocationUtils.adjustPositionByOffsets(rawBuoyPos, metersForward, metersStarboard, bearingOfNowClick);
			setPositionText(true, adjustedBuoyPos);
		}
		else
		{
			setPositionText(false, null);
		}
	}


	private void setPositionText(boolean show, LatLng pos)
	{
		TextView textPosition = (TextView)findViewById(R.id.textViewLocation);
		TextView textPositionLabel = (TextView)findViewById(R.id.textViewLocationLabel);
		Button buttonSave = (Button)findViewById(R.id.buttonSave);
		if( buttonSave != null && textPosition != null && textPositionLabel != null && show)
		{			
			textPosition.setText(Location.convert(pos.latitude, Location.FORMAT_SECONDS) + " " +
					Location.convert(pos.longitude, Location.FORMAT_SECONDS));

			textPositionLabel.setVisibility(TextView.VISIBLE);
			textPosition.setVisibility(TextView.VISIBLE);

			buttonSave.setVisibility(Button.VISIBLE);
		}
		else if( buttonSave != null && textPosition != null && textPositionLabel != null )
		{
			textPositionLabel.setVisibility(TextView.INVISIBLE);
			textPosition.setVisibility(TextView.INVISIBLE);
			
			buttonSave.setVisibility(Button.INVISIBLE);
		}

	}

	private LocationListener locationListener = new LocationListener() 
	{
		@Override
		public void onProviderDisabled(String provider) 
		{
			updateGPSReady(false, false);
			// Do nothing
		}

		@Override
		public void onProviderEnabled(String provider) 
		{
			updateGPSReady(false, true);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) 
		{
			// Do nothing
		}

		@Override
		public void onLocationChanged(final Location location) 
		{
			//is the the next location after the button is clicked?
			//if so interpolate the location it was clicked
			if( timeOfNowClick != 0 && lastLocation.getTime() != location.getTime())
			{
				SetBuoyLocationFromClick(location);
				timeOfNowClick = 0;
			}


			lastLocation = location;
			//we need to get the position between 2 locations - this is determined 
			//at the time the button is clicked - so we need to go by the system time not gps time
			//since we can't get the gps time when the button is clicked 
			lastLocation.setTime(System.currentTimeMillis());

			updateGPSReady(true, true);

			TextView textViewAccuracy = (TextView)findViewById(id.textViewAccuracy);
			if( textViewAccuracy!= null )
			{
				if (!LocationUtils.isValidLocation(location)) 
				{
					textViewAccuracy.setText(getString(R.string.gps_pos_invalid));
				}
				else
				{
					textViewAccuracy.setText(String.format("%.0f m", location.getAccuracy()));
				}
			}
		}
	};

}
