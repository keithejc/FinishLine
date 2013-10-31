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
import java.util.Arrays;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

public class BuoyListDialog extends DialogFragment 
{
	private static final String TAG = "BuoyListDialog";

	Buoy current;
	BuoyDialogListener dialogListener;

	static BuoyListDialog newInstance(Buoy current) 
	{
		BuoyListDialog f = new BuoyListDialog();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putParcelable("current", current);
		f.setArguments(args);

		return f;
	}

	public void setBuoyListDialogListener(BuoyDialogListener listener)
	{
		dialogListener = listener;
	}



	private ListView mListView;
	private ArrayAdapter<Buoy> mAdapter;
	private FinishLineDataStorage dbStorage = null;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL + DialogFragment.STYLE_NO_TITLE, R.style.Theme_Sherlock_DialogWithCorners);

		current = getArguments().getParcelable("current");
		

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View v = inflater.inflate(R.layout.buoy_list, container, false);
		getDialog().setCanceledOnTouchOutside(true);
		return v;
	}	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) 
	{
		super.onViewCreated(view, savedInstanceState);
		mListView = (ListView) view.findViewById(R.id.ListViewBuoys);

		dbStorage = new FinishLineDataStorage(view.getContext()); 
		dbStorage.open();

		ArrayList<Buoy> buoys = (ArrayList<Buoy>) dbStorage.getAllBuoys();
		mAdapter = new BuoyAdapter(view.getContext(), R.layout.buoy_list_item, buoys);

		SwipeDismissAdapter swipeDismissAdapter = new SwipeDismissAdapter(mAdapter, new MyOnDismissCallback(mAdapter));
		swipeDismissAdapter.setListView(getListView());

		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setAdapter(swipeDismissAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) 
			{
				//provide feedback that the item was clicked
				mListView.setItemChecked(position, true);

				//dismiss dialog after the item has been drawn clicked
				final int listPos = position;
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() 
					{
						dialogListener.buoySet((Buoy)mAdapter.getItem(listPos));
						dismiss();
					}}, 1);
			}});

		int selPos = mAdapter.getPosition(current);
		if(selPos >=0 )
		{
			mListView.setItemChecked(selPos, true);
		}
	}

	private int posToDelete;
	private class MyOnDismissCallback implements OnDismissCallback {

		private ArrayAdapter<Buoy> mAdapter;

		public MyOnDismissCallback(ArrayAdapter<Buoy> adapter) {
			mAdapter = adapter;
		}

		@Override
		public void onDismiss(ListView listView, int[] reverseSortedPositions) {
			for (int position : reverseSortedPositions) 
			{
				posToDelete = position;
				Buoy buoy = mAdapter.getItem(posToDelete);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(getString(R.string.confirm_delete_buoy) + buoy.Name).setPositiveButton(getString(R.string.yes), dialogClickListener)
				.setNegativeButton(getString(R.string.no), dialogClickListener).show();
				
			}
		}
	}

	//delete buoy?
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case DialogInterface.BUTTON_POSITIVE:
				Buoy buoy = mAdapter.getItem(posToDelete);
				dbStorage.deleteBuoy(buoy.Name);
				mAdapter.remove(buoy);
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				//No button clicked
				break;
			}
		}
	};	
	

	@Override
	public void onDestroy() 
	{
		dbStorage.close();
		super.onDestroy();
	}



	public ListView getListView() 
	{
		return mListView;
	}

	public class BuoyAdapter extends ArrayAdapter<Buoy> 
	{

		@Override
		public int getPosition(Buoy item) 
		{
			int itemNo = 0;
			for(Buoy buoy : items)
			{
				if( buoy.Name.equals(item.Name))
				{
					return itemNo;
				}
				itemNo++;
			}
			return -1;
		}

		private ArrayList<Buoy> items;
		private BuoyViewHolder buoyHolder;
		final ListView list = getListView();

		private class BuoyViewHolder 
		{
			CheckedTextView name;
			TextView location; 
		}

		public BuoyAdapter(Context context, int tvResId, ArrayList<Buoy> items) 
		{
			super(context, tvResId, items);
			this.items = items;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) 
		{
			
			View v = convertView;
			if (v == null) 
			{
				LayoutInflater inflater =  (LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.buoy_list_item, null);
				buoyHolder = new BuoyViewHolder();
				buoyHolder.name = (CheckedTextView)v.findViewById(R.id.buoy_name);
				buoyHolder.location = (TextView)v.findViewById(R.id.buoy_location);
				v.setTag(buoyHolder);
			} 
			else 
			{
				buoyHolder = (BuoyViewHolder)v.getTag(); 
			}

			Buoy buoy = items.get(pos);
			if (buoy != null) 
			{
				if( list.isItemChecked(pos) )
				{
					buoyHolder.name.setTextColor(getResources().getColor(R.color.abs__background_holo_dark));
					buoyHolder.location.setTextColor(getResources().getColor(R.color.abs__background_holo_dark));
					((View) buoyHolder.name.getParent()).setBackgroundColor(getResources().getColor(R.color.finishLineHighlight));
				}
				else
				{
					buoyHolder.name.setTextColor(getResources().getColor(R.color.abs__primary_text_holo_dark));
					buoyHolder.location.setTextColor(getResources().getColor(R.color.abs__primary_text_holo_dark));
					((View) buoyHolder.name.getParent()).setBackgroundColor(getResources().getColor(R.color.abs__background_holo_dark));
				}
				

				buoyHolder.name.setText(buoy.Name);
				buoyHolder.location.setText(
						PreferencesUtils.locationToString(getContext(), buoy.Position.latitude, true) +
						" " + 
						PreferencesUtils.locationToString(getContext(), buoy.Position.longitude, true) );
			}

			return v;
		}
	}


}
