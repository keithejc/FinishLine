package com.keithcassidy.finishline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapFragment extends SherlockMapFragment implements TabFocusInterface
{

	private static final String TAG = MapFragment.class.getSimpleName(); 
	private GoogleMap raceMap = null;
	private Polyline line = null;
	private int viewWidth = 0;
	private int viewHeight = 0;
	private SharedPreferences sharedPreferences;
	Buoy buoy1;
	Buoy buoy2;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onBroadcastServiceStatusReceived,
				new IntentFilter(Constants.SERVICE_STATUS_MESSAGE));

		sharedPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

		//force it to run once - to get buoys
		sharedPreferenceChangeListener.onSharedPreferenceChanged(sharedPreferences, null);

	}

	/*
	 * Note that sharedPreferenceChangeListener cannot be an anonymous inner
	 * class. Anonymous inner class will get garbage collected.
	 */
	private final OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() 
	{
		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

			if ((key == null || key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_1_latitude_key)) ||
					key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_1_longitude_key))) ) 
			{
				buoy1 = PreferencesUtils.getBouy1(getActivity());
				refreshFinishLine();
			}

			if ((key == null || key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_2_latitude_key)) ||
					key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_2_longitude_key))) ) 
			{
				buoy2 = PreferencesUtils.getBouy2(getActivity());
				refreshFinishLine();
			}

		}
	};


	@Override
	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) 
	{
		View root = super.onCreateView(arg0, arg1, arg2);

		root.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() 
				{

					@Override
					public void onGlobalLayout() 
					{
						// make sure it is not called anymore 
						getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);


						viewWidth = getView().getWidth(); 
						viewHeight = getView().getHeight();

						setUpMap();
						refreshMap();
					}
				});		




		return root;
	}


	@Override
	public void onDestroy() 
	{
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onBroadcastServiceStatusReceived);

		sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

		super.onDestroy();
	}

	private BroadcastReceiver onBroadcastServiceStatusReceived = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{			
			Location newLocation = intent.getParcelableExtra(Constants.NEW_LOCATION_MESSAGE);
			if( newLocation != null )
			{
				refreshMap();
			}
		}
	};	


	private void refreshFinishLine()
	{
		if( line != null )
		{
			line.remove();
		}
		if( raceMap != null )
		{
			line = raceMap.addPolyline(new PolylineOptions().add(buoy1.Position).add(buoy2.Position).color(Color.RED).width(5));
		}
	}

	private void refreshMap()
	{
		try
		{
			if( raceMap != null)
			{

				//set map camera
				Location loc = raceMap.getMyLocation();

				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				builder.include(buoy1.Position);
				builder.include(buoy2.Position);

				if( loc != null )
				{
					builder.include(new LatLng(loc.getLatitude(), loc.getLongitude()));
				}

				if( viewWidth > 0 && viewHeight > 0 )
				{
					int padding = 40;
					CameraUpdate c = CameraUpdateFactory.newLatLngBounds(builder.build(), viewWidth, viewHeight, padding);
					raceMap.animateCamera(c);
				}
			}

		}
		catch(Exception e)
		{
			Log.e(TAG, "refreshMap " + e);

		}

	}

	private void setUpMap() 
	{
		try
		{
			//first time setup
			if (raceMap == null) 
			{
				raceMap = getMap();
				// Check if we were successful in obtaining the map.
				if (raceMap != null) 
				{
					raceMap.setMyLocationEnabled(true);
					raceMap.getUiSettings().setMyLocationButtonEnabled(false);
					raceMap.getUiSettings().setRotateGesturesEnabled(false);
					raceMap.getUiSettings().setTiltGesturesEnabled(false);
					raceMap.getUiSettings().setScrollGesturesEnabled(false);

					refreshMap();
					refreshFinishLine();
				}
				else
				{
					Toast.makeText(getActivity(), getActivity().getString(R.string.map_not_installed), Toast.LENGTH_LONG).show();
				}
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, "setUpMap " + e);
		}

	}

	@Override
	public void tabSetFocus() 
	{
		refreshMap();
	}

	@Override
	public void tabLoseFocus() 
	{
	}    
}
