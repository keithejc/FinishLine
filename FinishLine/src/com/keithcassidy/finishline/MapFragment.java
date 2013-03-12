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

import java.util.ArrayList;

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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
	private boolean autoMapUpdate;
	private MenuItem autoUpdateMenuItem;
	private boolean isFirstMapUpdate;
	private ArrayList<Marker> markers;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		isFirstMapUpdate = true;

		super.onCreate(savedInstanceState);
		super.setHasOptionsMenu(true);

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onBroadcastServiceStatusReceived,
				new IntentFilter(Constants.SERVICE_STATUS_MESSAGE));

		sharedPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

		//force it to run once - to get buoys
		sharedPreferenceChangeListener.onSharedPreferenceChanged(sharedPreferences, null);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{

		autoUpdateMenuItem = menu.add(R.string.menu_autozoom);
		autoUpdateMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		setAutoZoomIcon();
	}

	private void setAutoZoomIcon()
	{
		if( autoUpdateMenuItem != null )
		{
			if( autoMapUpdate )
			{
				autoUpdateMenuItem.setIcon(R.drawable.ic_action_zoom_on);
			}
			else
			{
				autoUpdateMenuItem.setIcon(R.drawable.ic_action_zoom_off);
			}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		try
		{
			autoMapUpdate = !autoMapUpdate;

			//sets the menu icon
			setAutoZoomIcon();
			//redraws the menu
			super.getSherlockActivity().supportInvalidateOptionsMenu();

			PreferencesUtils.setAutoMapUpdate(getActivity(), autoMapUpdate);
			refreshMap();			

		}
		catch(Exception e)
		{
			Log.e(TAG, "onMenuItemClick " + e);
		}
		return true;
	}

	/*
	 * Note that sharedPreferenceChangeListener cannot be an anonymous inner
	 * class. Anonymous inner class will get garbage collected.
	 */
	private final OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() 
	{
		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

			if ((key == null || 
					key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_1_name_key)) ||
					key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_1_latitude_key)) ||
					key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_1_longitude_key))) ) 
			{
				buoy1 = PreferencesUtils.getBouy1(getActivity());
				refreshFinishLine();
				refreshMap();
			}

			if ((key == null ||
					key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_2_name_key)) ||
					key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_2_latitude_key)) ||
					key.equals(PreferencesUtils.getKey(getActivity(), R.string.buoy_2_longitude_key))) ) 
			{
				buoy2 = PreferencesUtils.getBouy2(getActivity());
				refreshFinishLine();
				refreshMap();
			}

			if ((key == null || key.equals(PreferencesUtils.getKey(getActivity(), R.string.auto_map_update_key)))) 
			{
				autoMapUpdate = PreferencesUtils.getAutoMapUpdate(getActivity());
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

		refreshMarkers();
	}


	private void refreshMarkers()
	{
		if( markers == null && buoy1 != null && buoy2 != null && raceMap != null)
		{
			markers = new ArrayList<Marker>(2);
			markers.add(raceMap.addMarker(new MarkerOptions()
				.position(buoy1.Position)
				.title(buoy1.Name)
				.snippet(PreferencesUtils.locationToString(getActivity(), buoy1.Position))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) ));
			markers.add(raceMap.addMarker(new MarkerOptions()
				.position(buoy2.Position)
				.title(buoy2.Name)
				.snippet(PreferencesUtils.locationToString(getActivity(), buoy2.Position))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) ));
		}
		else if(buoy1 != null && buoy2 != null && raceMap != null)
		{
			for(Marker marker: markers)
			{
				marker.remove();
			}
			markers.clear();
			markers.add(raceMap.addMarker(new MarkerOptions()
				.position(buoy1.Position)
				.title(buoy1.Name)
				.snippet(PreferencesUtils.locationToString(getActivity(), buoy1.Position))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) ));
			markers.add(raceMap.addMarker(new MarkerOptions()
				.position(buoy2.Position)
				.title(buoy2.Name)
				.snippet(PreferencesUtils.locationToString(getActivity(), buoy2.Position))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) ));
		}
	}

	private void refreshMap()
	{
		try
		{
			if( raceMap != null)
			{

				raceMap.getUiSettings().setMyLocationButtonEnabled(!autoMapUpdate);
				raceMap.getUiSettings().setRotateGesturesEnabled(!autoMapUpdate);
				raceMap.getUiSettings().setTiltGesturesEnabled(!autoMapUpdate);
				raceMap.getUiSettings().setScrollGesturesEnabled(!autoMapUpdate);
				raceMap.getUiSettings().setZoomControlsEnabled(!autoMapUpdate);

				if(autoMapUpdate || isFirstMapUpdate) 
				{
					isFirstMapUpdate = false;

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
						int padding = 80;
						CameraUpdate c = CameraUpdateFactory.newLatLngBounds(builder.build(), viewWidth, viewHeight, padding);
						raceMap.animateCamera(c);
					}
				}
			}

		}
		catch(Exception e)
		{
			Log.e(TAG, "refreshMap " + e);

		}

	}

	private boolean onMarkerClicked(Marker marker)
	{
		if( marker.isInfoWindowShown() )
		{
			marker.hideInfoWindow();
			
			return true;
		}
		
		return false;
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
					raceMap.setOnMapClickListener(new OnMapClickListener() {

						@Override
						public void onMapClick(LatLng location) 
						{
							onMapClicked();
							
						}});
					raceMap.setOnMarkerClickListener(new OnMarkerClickListener() {

						@Override
						public boolean onMarkerClick(Marker marker) 
						{
							return onMarkerClicked(marker);
						}});
					
					raceMap.setMyLocationEnabled(true);
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

	protected void onMapClicked() 
	{
		
		if( autoMapUpdate )
		{
			isFirstMapUpdate = true;
			refreshMap();
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
