package com.keithcassidy.finishline;

import java.text.DecimalFormatSymbols;

import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class BuoyEditDialog extends DialogFragment 
{
	private static final String TAG = "BuoyEditDialog";
	Buoy buoy;
	private EditText editName = null;
	private EditText editLatitude = null;
	private EditText editLongitude = null;
	private ImageView errorName = null;
	private ImageView errorLatitude = null;
	private ImageView errorLongitude = null;
	private Button buttonOK = null;

	BuoyDialogListener dialogListener;

	static BuoyEditDialog newInstance(Buoy buoy) 
	{
		BuoyEditDialog f = new BuoyEditDialog();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putParcelable("buoy", buoy);
		f.setArguments(args);

		return f;
	}

	public void setBuoyDialogListener(BuoyDialogListener listener)
	{
		dialogListener = listener;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL + DialogFragment.STYLE_NO_TITLE, R.style.Theme_Sherlock_DialogWithCorners);

		buoy = getArguments().getParcelable("buoy");
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View v = inflater.inflate(R.layout.buoy_edit, container, false);
		getDialog().setCanceledOnTouchOutside(true);
		return v;
	}	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) 
	{
		
		buttonOK = (Button)view.findViewById(R.id.buttonOK);
		Button buttonCancel = (Button)view.findViewById(R.id.buttonCancel);
		
		if( buttonOK != null )
		{
			buttonOK.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) 
				{
					dialogListener.buoySet(loadBuoyFromControls());
					dismiss();
				}});
		}

		if( buttonCancel != null )
		{
			buttonCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) 
				{
					dismiss();
				}});
		}
		
		editName = (EditText) view.findViewById(R.id.editTextBuoyName);
		editLatitude = (EditText) view.findViewById(R.id.editTextBuoyLatitude);
		editLongitude = (EditText) view.findViewById(R.id.editTextBuoyLongitude);
		errorName = (ImageView) view.findViewById(R.id.errorName);
		errorLatitude = (ImageView) view.findViewById(R.id.errorLatitude);
		errorLongitude = (ImageView) view.findViewById(R.id.errorLongitude);

		setupLatLongEdits(editLatitude, editLongitude);
		
		editName.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				validateDialog();
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {			}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {				
			}});

		editLatitude.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				validateDialog();
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {			}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {				
			}});
		
		editLongitude.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				validateDialog();
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {			}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {				
			}});

		
		if( buoy != null) 
		{
			loadControlsWithBuoy(buoy);
		}
	}	
	
	protected void validateDialog()
	{
		boolean valid = true; 
		if( editName != null && errorName != null)
		{
			if( editName.getText().length() == 0 )
			{
				errorName.setVisibility(View.VISIBLE);
				valid = false;
			}
			else
			{
				errorName.setVisibility(View.INVISIBLE);
			}
		}
		
		if( editLatitude != null )
		{
			double lat = LocationUtils.parseDMS(editLatitude.getText().toString());
			if( Double.isNaN(lat) )
			{
				errorLatitude.setVisibility(View.VISIBLE);
				valid = false;
			}
			else
			{
				errorLatitude.setVisibility(View.INVISIBLE);
			}		
		}

		if( editLongitude != null )
		{
			double lat = LocationUtils.parseDMS(editLongitude.getText().toString());
			if( Double.isNaN(lat) )
			{
				errorLongitude.setVisibility(View.VISIBLE);
				valid = false;
			}
			else
			{
				errorLongitude.setVisibility(View.INVISIBLE);
			}		
		}
		
		if( buttonOK != null )
		{
			buttonOK.setEnabled(valid);
		}
		
		
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
