package com.keithcassidy.finishline;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.keithcassidy.finishline.FinishLineService.LocalBinder;

public class MapActivity extends FragmentActivity
{

	private static final String TAG = MapActivity.class.getSimpleName(); 
	private GoogleMap raceMap;
	private FinishLineDataStorage dbStorage = null;
	private Polyline line = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		ApiAdapterFactory.getApiAdapter().hideTitle(this);
		setContentView(R.layout.activity_map);


		LocalBroadcastManager.getInstance(this).registerReceiver(onBroadcastServiceStatusReceived,
				new IntentFilter(Constants.SERVICE_STATUS_MESSAGE));

		if( dbStorage == null)
		{
			dbStorage = new FinishLineDataStorage(this); 
			dbStorage.open();
		}
		
		setUpMapIfNeeded();

	}


	@Override
	protected void onDestroy() 
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(onBroadcastServiceStatusReceived);

		if( dbStorage != null )
		{
			dbStorage.close();
		}

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
				setUpMap();
			}
		}
	};	




	@Override
	protected void onResume() 
	{
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() 
	{
		// Do a null check to confirm that we have not already instantiated the map.
		if (raceMap == null) 
		{
			// Try to obtain the map from the SupportMapFragment.
			raceMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (raceMap != null) 
			{
				raceMap.setMyLocationEnabled(true);
				setUpMap();
			}
		}
	}

	private void setUpMap() 
	{
		if( dbStorage != null )
		{

			Race race = dbStorage.getRace(
					PreferencesUtils.getLong(this, 
					R.string.race_id_key,
					PreferencesUtils.RACE_ID_DEFAULT));
			if( line == null )
			{
				line = raceMap.addPolyline(new PolylineOptions().add(PreferencesUtils.getBouy1(this).Position).add(PreferencesUtils.getBouy2(this).Position).color(Color.RED).width(5));
			}
			else
			{
				List<LatLng> points = line.getPoints();
				if( points.size() != 2 || !points.get(0).equals(race.getBuoy1().Position) || !points.get(1).equals(race.getBuoy2().Position))
				{
					points.clear();
					points.add(race.getBuoy1().Position);
					points.add(race.getBuoy2().Position);
				}
			}
		}
		Location loc = raceMap.getMyLocation();
		if( loc != null )
		{
			LatLng zoom = new LatLng(loc.getLatitude(), loc.getLongitude());
			CameraUpdate c = CameraUpdateFactory.newLatLngZoom(zoom, 10);
			raceMap.animateCamera(c);
		}
	}    
}
