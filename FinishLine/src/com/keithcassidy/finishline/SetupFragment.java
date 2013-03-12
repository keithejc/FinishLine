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

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.model.LatLng;


public class SetupFragment extends SherlockFragment implements TabFocusInterface
{
	
	private static final String TAG = SetupFragment.class.getSimpleName();

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
		BuoyListDialog dlg = BuoyListDialog.newInstance(loadBuoyFromControls());
		//assign a new DateDialogFragmentListener
		dlg.setBuoyListDialogListener(new BuoyListDialogListener() {

			//fired when user selects buoy
			@Override
			public void buoySet(Buoy buoy) {
		        if( buoy != null )
		        {
		        	if( buoyNumber == 1)
		        	{
		        		PreferencesUtils.setBouy1(getActivity(), buoy);
		        	}
		        	else
		        	{
		        		PreferencesUtils.setBouy2(getActivity(), buoy);
		        	}
		        	setCurrentBuoy(buoy.Name);
		        	loadControlsWithBuoy(buoy);
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
			Buoy buoy = null;
	        if(data.getExtras().containsKey(getString(R.string.intent_data_buoy_name)))
	        {
	        	String buoyName = data.getStringExtra(getString(R.string.intent_data_buoy_name));
	        	buoy = dbStorage.getBuoy(buoyName);
	        }
			else if(data.getExtras().containsKey(getString(R.string.intent_data_buoy_position)))
			{
				LatLng llBuoy = data.getParcelableExtra(getString(R.string.intent_data_buoy_position));
				if( llBuoy != null)
				{
					buoy = loadBuoyFromControls();
					buoy.Position = llBuoy;
				}
			}

	        if( buoy != null )
	        {
	        	if( buoyNumber == 1)
	        	{
	        		PreferencesUtils.setBouy1(getActivity(), buoy);
	        	}
	        	else
	        	{
	        		PreferencesUtils.setBouy2(getActivity(), buoy);
	        	}
	        	setCurrentBuoy(buoy.Name);
	        	loadControlsWithBuoy(buoy);
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
	private EditText editName = null;
	private EditText editLatitude = null;
	private EditText editLongitude = null;
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

		editName = (EditText) view.findViewById(R.id.editTextBuoyName);
		editLatitude = (EditText) view.findViewById(R.id.editTextBuoyLatitude);
		editLongitude = (EditText) view.findViewById(R.id.editTextBuoyLongitude);
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
		Buoy buoy = loadBuoyFromPrefs();
		if( buoy != null )
		{
			setCurrentBuoy(buoy.Name);			
		}

		setupLatLongEdits(editLatitude, editLongitude);

		Button buttonSave = (Button) view.findViewById(R.id.buttonSaveBuoy);
		if( buttonSave != null )
		{
			buttonSave.setOnClickListener(new Button.OnClickListener() 
			{  
				public void onClick(View v)
				{
					onSaveBuoyButtonPressed();
				}
			});				
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
	}

	protected void saveBuoyToPrefs() 
	{
		try
		{
	    	Buoy buoy = loadBuoyFromControls();
	    	
	    	if( buoy != null )
	    	{
		    	if( buoyNumber == 1 )
		    	{
		    		PreferencesUtils.setBouy1(getActivity(), buoy);
		    	}
		    	else
		    	{
		    		PreferencesUtils.setBouy2(getActivity(), buoy);
		    	}
	    	}
		}
		catch(Exception e)
		{
			Log.e(TAG, "saveBuoyToPrefs" + e);
		}
	}

	private Buoy loadBuoyFromPrefs() 
	{
		try
		{
			Buoy buoy;
			if( buoyNumber == 1)
			{
				buoy = PreferencesUtils.getBouy1(getActivity());
			}
			else
			{
				buoy = PreferencesUtils.getBouy2(getActivity());
			}

			loadControlsWithBuoy(buoy);
			
			return buoy;
		}
		catch(Exception e)
		{
			Log.e(TAG, "loadBuoyFromPrefs" + e);
			return null;
		}
	}
	
	protected void onSaveBuoyButtonPressed() 
	{
		
		Buoy buoy = loadBuoyFromControls();
		if( buoy != null )
		{
			if( !buoy.Name.contentEquals(""))
			{
				//does this buoy already exist - if so edit, otherwise add new
				if( dbStorage.doesBuoyExist(buoy.Name) )
				{
					dbStorage.updateBuoy(buoy);
				}
				else
				{
					dbStorage.addBuoy(buoy);
					savedBuoyList.add(buoy.Name);
				}
	
				//show it in the spinner
				setCurrentBuoy(buoy.Name);
			}

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
		if( editName != null )
		{
			editName.setText(item.Name);			
		}

		if( editLatitude != null && !Double.isNaN( item.Position.latitude) )
		{			
			editLatitude.setText(PreferencesUtils.locationToString(getActivity(), item.Position.latitude, true));
		}
		if( editLongitude != null && !Double.isNaN( item.Position.longitude) )
		{
			editLongitude.setText(PreferencesUtils.locationToString(getActivity(), item.Position.longitude, false));
		}

	}

	protected Buoy loadBuoyFromControls()
	{
		try
		{
			Buoy buoy = new Buoy();
	
			if( editName != null )
			{
				buoy.Name = editName.getText().toString().trim();
			}
	
			if( editLatitude != null && editLongitude != null )
			{
				double lat = LocationUtils.parseDMS(editLatitude.getText().toString()); 
				double lng = LocationUtils.parseDMS(editLongitude.getText().toString());
				if( !Double.isNaN(lat) && ! Double.isNaN(lng))
				{
					buoy.Position = new LatLng(lat, lng);
				}
				else
				{
					buoy = null;
				}
			}
	
	
			return buoy;
		}
		catch (Exception e)
		{
		}
		
		return null;
	}

	private class LatLongFilter implements InputFilter 
	{
		private boolean isLatitude;
		private final DecimalFormatSymbols decSym = new DecimalFormatSymbols(); 

		public LatLongFilter(boolean isLatitude)
		{
			this.isLatitude = isLatitude;
		}

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) 
		{

			if (source instanceof SpannableStringBuilder) 
			{
				SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
				for (int i = end - 1; i >= start; i--) 
				{ 
					char currentChar = source.charAt(i);

					if( isLatitude )
					{
						if (!Character.isDigit(currentChar) &&
								!Character.isSpaceChar(currentChar) &&
								currentChar != ':' && 
								currentChar != '°' && 
								currentChar != '"' && 
								currentChar != '\'' && 
								currentChar != 'N' && 
								currentChar != 'S' && 
								currentChar != 'n' && 
								currentChar != 's' && 
								currentChar != decSym.getDecimalSeparator() &&
								currentChar != '-') 
						{    
							sourceAsSpannableBuilder.delete(i, i+1);
						}
					}
					else
					{
						if (!Character.isDigit(currentChar) &&
								!Character.isSpaceChar(currentChar) && 
								currentChar != ':' && 
								currentChar != '°' && 
								currentChar != '"' && 
								currentChar != '\'' && 
								currentChar != 'E' && 
								currentChar != 'W' && 
								currentChar != 'e' && 
								currentChar != 'w' && 
								currentChar != decSym.getDecimalSeparator() && 
								currentChar != '-') 
						{    
							sourceAsSpannableBuilder.delete(i, i+1);
						}
					}
				}
				return source;
			}
			else 
			{
				StringBuilder filteredStringBuilder = new StringBuilder();
				for (int i = 0; i < end; i++) 
				{ 
					char currentChar = source.charAt(i);

					if( isLatitude)
					{
						if (Character.isDigit(currentChar) ||
								Character.isSpaceChar(currentChar) ||
								currentChar == ':' || 
								currentChar == '°' || 
								currentChar == '"' || 
								currentChar == '\'' || 
								currentChar == 'N' || 
								currentChar == 'S' || 
								currentChar == 'n' || 
								currentChar == 's' || 
								currentChar == decSym.getDecimalSeparator()|| 
								currentChar == '-' ) 
						{    
							filteredStringBuilder.append(currentChar);
						}     
					}
					else
					{
						if (Character.isDigit(currentChar) ||
								Character.isSpaceChar(currentChar) ||
								currentChar == ':' || 
								currentChar == '°' || 
								currentChar == '"' || 
								currentChar == '\'' || 
								currentChar == 'E' || 
								currentChar == 'W' || 
								currentChar == 'e' || 
								currentChar == 'w' || 
								currentChar == decSym.getDecimalSeparator()|| 
								currentChar == '-' ) 
						{    
							filteredStringBuilder.append(currentChar);
						}     
					}
				}
				return filteredStringBuilder.toString();
			}
		}
	};


	private void setupLatLongEdits(EditText latEdit, EditText longEdit)
	{		

		if( latEdit != null )
		{
			latEdit.setFilters(new InputFilter[]{ new LatLongFilter(true)});
			latEdit.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		}

		if( longEdit != null )
		{
			longEdit.setFilters(new InputFilter[]{ new LatLongFilter(false) });
			longEdit.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		}

	}

}
