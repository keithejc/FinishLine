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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.google.analytics.tracking.android.EasyTracker;


public class HomeFragment extends SherlockFragment implements TabFocusInterface , ServiceStatusInterface
{

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{


		super.onCreateOptionsMenu(menu, inflater);
	}

	@SuppressWarnings("unused")
	private static final String TAG = HomeFragment.class.getSimpleName(); 

	ShowcaseView sv;
	ShowcaseView.ConfigOptions mOptions = new ShowcaseView.ConfigOptions();


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) 
	{
		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;

		//sv = ShowcaseView.insertShowcaseView(R.id.buttonStart, getActivity(),
		//		"Start A Race", "When racing, FinishLine will log finish times", co);

		super.onViewCreated(view, savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{

		//final FrameLayout overlayFramelayout = new FrameLayout(getActivity().getApplicationContext());		
		//getActivity().setContentView(overlayFramelayout);

		View view = inflater.inflate(R.layout.home, container, false);
		//final View overlay_view = inflater.inflate(R.layout.setup, ((ViewGroup) getActivity().getWindow().getDecorView()),false);

		//((ViewGroup) getActivity().getWindow().getDecorView()).addView(overlay_view);		

		//        overlayFramelayout.addView(view);
		//      overlayFramelayout.addView(overlay_view);

		final Button start = (Button)view.findViewById(R.id.buttonStart);
		final Button stop = (Button)view.findViewById(R.id.buttonStop);

		if( start != null && stop != null)
		{
			start.setOnClickListener(new Button.OnClickListener() 
			{  
				public void onClick(View v)
				{
					startRace();
					setupButtons(true);
				}
			});				
			stop.setOnClickListener(new Button.OnClickListener() 
			{  
				public void onClick(View v)
				{
					stopRace();
					setupButtons(false);
				}
			});				
		}		
		return view;

	}


	@Override
	public void onStart() 
	{
		super.onStart();
		setStatus(R.string.status_gps_waiting);
		setupButtons(PreferencesUtils.getBoolean(getActivity(), R.string.is_racing_key, false));
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}


	private float[] mGravity;
	private float[] mGeomagnetic;
	String bearing = "";
	@SuppressWarnings("unused")
	private void doCompass()
	{
		getActivity();
		SensorManager sman = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

		Sensor magnetfield = sman.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor accelerometer = sman.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Third, implement a SensorEventListener class
		SensorEventListener magnetlistener = new SensorEventListener() 
		{
			public void onAccuracyChanged(Sensor sensor, int accuracy) 
			{
				// do things if you're interested in accuracy changes
			}
			public void onSensorChanged(SensorEvent event) 
			{ 
				try
				{
					if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
						mGravity = event.values.clone();
					if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
						mGeomagnetic = event.values.clone();
					if (mGravity != null && mGeomagnetic != null) {
						float rot[] = new float[9];
						float I[] = new float[9];
						boolean success = SensorManager.getRotationMatrix(rot, I, mGravity, mGeomagnetic);
						if (success) {
							float orientation[] = new float[3];
							SensorManager.getOrientation(rot, orientation);
							double floatBearing = Math.toDegrees(orientation[0]); 
							if (floatBearing < 0) floatBearing += 360;

							bearing = String.format("%.0f", floatBearing);
						}
					}
				}
				catch(Exception e)
				{
				}
			}
		};


		// Finally, register your listener
		sman.registerListener(magnetlistener, magnetfield, SensorManager.SENSOR_DELAY_UI);
		sman.registerListener(magnetlistener, accelerometer, SensorManager.SENSOR_DELAY_UI);
	}

	private void setStatus(int resId, String extra)
	{
		setStatus(getActivity().getString(resId) + extra);
	}

	private void setStatus(String status)
	{
		TextView textViewStatus = (TextView)getActivity().findViewById(R.id.textViewStatus);
		if( textViewStatus != null )
		{
			textViewStatus.setText(status);
		}
	}
	private void setStatus(int resId)
	{
		setStatus((getActivity().getString(resId)));
	}


	private void startRace() 
	{
		EasyTracker.getTracker().sendEvent("category", "action", "label", 42L);
		

		
		
		Intent startIntent = new Intent(getActivity(), FinishLineService.class)
		.putExtra(Constants.START_RACE_EXTRA_NAME, true);
		getActivity().startService(startIntent);
	}

	private void stopRace()
	{
		int s = 0;
		float d = 10/s;
		Intent stopIntent = new Intent(getActivity(), FinishLineService.class)
		.putExtra(Constants.STOP_RACE_EXTRA_NAME, true);
		getActivity().startService(stopIntent);
	}

	private void setupButtons(boolean isRacing) 
	{
		final Button start = (Button)getView().findViewById(R.id.buttonStart);
		final Button stop = (Button)getView().findViewById(R.id.buttonStop);

		if( start != null && stop != null)
		{			
			start.setEnabled(!isRacing);
			stop.setEnabled(isRacing);
		}		
	}

	@Override
	public void tabSetFocus() 
	{
	}

	@Override
	public void tabLoseFocus() 
	{
	}


	@Override
	public void onReceiveServiceStatus(Context context, Intent intent) 
	{
		if( intent.getBooleanExtra(Constants.LOCATION_INACCURATE_MESSAGE, false))
		{
			setStatus(R.string.status_inaccurate_location, String.format("( > %dm)", PreferencesUtils.getMaxAccuracyAllowed(getActivity())));
		}
		else if( intent.getBooleanExtra(Constants.LOCATION_INVALID_MESSAGE, false))
		{
			setStatus(R.string.status_invalid_location);
		}
		else if( intent.getBooleanExtra(Constants.GPS_ENABLED_MESSAGE, false))
		{
			setStatus(R.string.status_gps_waiting);
		}
		else if( intent.getBooleanExtra(Constants.GPS_NOT_ENABLED_MESSAGE, false))
		{
			setStatus(R.string.status_gps_not_enabled);
		}		

		Location newLocation = intent.getParcelableExtra(Constants.NEW_LOCATION_MESSAGE);
		if( newLocation != null )
		{
			setStatus(R.string.status_location_ok);

			TextView textViewLocation = (TextView)getActivity().findViewById(R.id.textViewLocation);
			TextView textViewBearing = (TextView)getActivity().findViewById(R.id.textViewBearing);

			if( textViewLocation!= null )
			{
				textViewLocation.setText(
						PreferencesUtils.locationToString(getActivity(), newLocation.getLatitude(), true) + " " +
								PreferencesUtils.locationToString(getActivity(), newLocation.getLongitude(), false));
			}

			if( textViewBearing != null)
			{
				textViewBearing.setText(String.format("%.0f", newLocation.getBearing()));
			}

		}

		TextView textViewDistance = (TextView)getActivity().findViewById(R.id.textViewDistance);
		if( textViewDistance != null)
		{
			float distance = intent.getFloatExtra(Constants.FINISHLINE_DISTANCE_MESSAGE, Float.POSITIVE_INFINITY);
			if( Float.isInfinite(distance) )
			{
				textViewDistance.setText(getActivity().getString(R.string.distance_infinite));
			}
			else
			{
				textViewDistance.setText(String.format("%.0f m", distance));
			}
		}



	}

}
