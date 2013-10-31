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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.model.LatLng;


public class SetupFragment extends SherlockFragment implements TabFocusInterface, ServiceStatusInterface
{
	
	private static final String TAG = SetupFragment.class.getSimpleName();
	private static Buoy currentBuoy;

	@Override
	public void tabSetFocus() 
	{
		loadBuoyFromPrefs();
	}

	@Override
	public void tabLoseFocus() 
	{
		saveBuoyToPrefs();
	}

	public SetupFragment() 
	{
		super();
	}

	public void onBuoyListClicked()
	{
		BuoyListDialog dlg = BuoyListDialog.newInstance(currentBuoy);
		//assign a new DateDialogFragmentListener
		dlg.setBuoyListDialogListener(new BuoyDialogListener() {

			//fired when user selects buoy
			@Override
			public void buoySet(Buoy buoy) {
		        if( buoy != null )
		        {
		        	currentBuoy = buoy;
		        	if( buoyNumber == 1)
		        	{
		        		PreferencesUtils.setBouy1(getActivity(), buoy);
			        	setCurrentBuoy(buoy.Name);
			        	loadControlsWithBuoy(buoy);
		        	}
		        	else if( buoyNumber == 2)
		        	{
		        		PreferencesUtils.setBouy2(getActivity(), buoy);
			        	setCurrentBuoy(buoy.Name);
			        	loadControlsWithBuoy(buoy);
		        	}
		        }
				
			}
		});
		dlg.show(getActivity().getSupportFragmentManager(), "bouy list dialog fragment");
		
	}
	
	@Override
	//either get pos from gps, or buoy list has returned.
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if( data != null )
		{
	        if(data.getExtras().containsKey(getString(R.string.intent_data_buoy_position)))
			{
				LatLng llBuoy = data.getParcelableExtra(getString(R.string.intent_data_buoy_position));
				if( llBuoy != null)
				{
					
					currentBuoy.Position = llBuoy;
				
		        	if( buoyNumber == 1)
		        	{
						currentBuoy.Name = getString(R.string.buoy_1_gps);
		        		PreferencesUtils.setBouy1(getActivity(), currentBuoy);
			        	setCurrentBuoy("");
			        	loadControlsWithBuoy(currentBuoy);
		        	}
		        	else if( buoyNumber == 2)
		        	{
						currentBuoy.Name = getString(R.string.buoy_2_gps);
		        		PreferencesUtils.setBouy2(getActivity(), currentBuoy);
			        	setCurrentBuoy("");
			        	loadControlsWithBuoy(currentBuoy);
		        	}

		        	
					Log.d(TAG, "##################### saving buoy number " + buoyNumber + " #############################");
					
				}
			}

		}
		
	}

	@Override
	public void onStop() 
	{
		super.onStop();
		saveBuoyToPrefs();
	}


	private FinishLineDataStorage dbStorage = null;
	private TextView buoyList = null;
	private int buoyNumber;
	ArrayList<String> savedBuoyList;
	
	public void setBuoyNumber(int buoyNumber) 
	{
		this.buoyNumber = buoyNumber;
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		if( dbStorage == null )
		{
			dbStorage = new FinishLineDataStorage(getActivity()); 
			dbStorage.open();
			savedBuoyList = (ArrayList<String>) dbStorage.getAllBuoyNames();
		}


		View view = inflater.inflate(R.layout.setup, container, false);

		return view;

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) 
	{
		super.onViewCreated(view, savedInstanceState);

		buoyList = (TextView)view.findViewById(R.id.textViewBuoySelect);
		if( buoyList != null)
		{
			buoyList.setOnClickListener(new TextView.OnClickListener() 
			{  
				public void onClick(View v)
				{
					onBuoyListClicked();
				}
			});				

		}
		
		//load current buoy
		loadBuoyFromPrefs();
		if( currentBuoy != null )
		{
			setCurrentBuoy(currentBuoy.Name);			
		}

	
		Button buttonGPS = (Button)view.findViewById(R.id.buttonSetWithGPS);
		if( buttonGPS != null )
		{
			buttonGPS.setOnClickListener(new Button.OnClickListener() 
			{  
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(), SetFromGPSActivity.class).putExtra(Constants.INTENT_BUOY_NUMBER_EXTRA, buoyNumber);
			        startActivityForResult(intent, 1);
				}
			}); 
		}
		
		
		Button buttonManageList = (Button) view.findViewById(R.id.buttonManageBuoys);
		if( buttonManageList != null )
		{
			buttonManageList.setOnClickListener(new Button.OnClickListener() 
			{  
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(), BuoyManageActivity.class);
			        startActivityForResult(intent, 1);

				}
			});	
		}
		
	}

	protected void saveBuoyToPrefs() 
	{
		try
		{
	    	if( currentBuoy != null )
	    	{
		    	if( buoyNumber == 1 )
		    	{
		    		PreferencesUtils.setBouy1(getActivity(), currentBuoy);
		    	}
		    	else if( buoyNumber == 2 )
		    	{
		    		PreferencesUtils.setBouy2(getActivity(), currentBuoy);
		    	}
	    	}
		}
		catch(Exception e)
		{
			Log.e(TAG, "saveBuoyToPrefs" + e);
		}
	}

	private void loadBuoyFromPrefs() 
	{
		try
		{
			if( buoyNumber == 1)
			{
				currentBuoy = PreferencesUtils.getBouy1(getActivity());
				loadControlsWithBuoy(currentBuoy);
			}
			else if( buoyNumber == 2)
			{
				currentBuoy = PreferencesUtils.getBouy2(getActivity());
				loadControlsWithBuoy(currentBuoy);
			}			
		}
		catch(Exception e)
		{
			Log.e(TAG, "loadBuoyFromPrefs" + e);
		}
	}
		

	private void setCurrentBuoy(String name) 
	{
		if( buoyList != null )
		{
			if( savedBuoyList.contains(name))
			{
				buoyList.setText(name);
			}
			else
			{
				buoyList.setText("");
			}
		}		
	}

	@Override
	public void onDestroy() 
	{
		if( dbStorage != null )
		{
			dbStorage.close();
		}
		super.onDestroy();
	}


	protected void loadControlsWithBuoy(Buoy item) 
	{
		if( item != null )
		{
			TextView buoyName = (TextView) getActivity().findViewById(R.id.buoyName);
			if( buoyName != null )
			{
				buoyName.setText(item.Name); 
			}
			
			TextView buoyPos = (TextView) getActivity().findViewById(R.id.buoyPos);
			if( buoyPos != null )
			{
				buoyPos.setText(PreferencesUtils.locationToString(getActivity(), item.Position.latitude, true) + " " + PreferencesUtils.locationToString(getActivity(), item.Position.longitude, false)); 
			}
		}
	}


	@Override
	public void onReceiveServiceStatus(Context context, Intent intent) {
		// do nothing
		
	}

}
