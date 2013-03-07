package com.keithcassidy.finishline;

import java.util.ArrayList;
import java.util.Arrays;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

public class BuoyListDialog extends DialogFragment 
{

	Buoy current;
	BuoyListDialogListener dialogListener;

	static BuoyListDialog newInstance(Buoy current) 
	{
		BuoyListDialog f = new BuoyListDialog();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putParcelable("current", current);
		f.setArguments(args);

		return f;
	}

	public void setBuoyListDialogListener(BuoyListDialogListener listener)
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
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) 
			{
				dialogListener.buoySet((Buoy)mAdapter.getItem(position));
				dismiss();
			}});
		
		int selPos = mAdapter.getPosition(current);
		if(selPos >=0 )
		{
			mListView.setItemChecked(selPos, true);
		}
	}

	private class MyOnDismissCallback implements OnDismissCallback {

		private ArrayAdapter<Buoy> mAdapter;

		public MyOnDismissCallback(ArrayAdapter<Buoy> adapter) {
			mAdapter = adapter;
		}

		@Override
		public void onDismiss(ListView listView, int[] reverseSortedPositions) {
			for (int position : reverseSortedPositions) 
			{
				Buoy buoy = mAdapter.getItem(position);
				dbStorage.deleteBuoy(buoy.Name);
				mAdapter.remove(buoy);
			}
		}
	}
	
	
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
					buoyHolder.name.setBackgroundColor(getResources().getColor(R.color.finishLineHighlight));
				}
				else
				{
					buoyHolder.name.setBackgroundColor(getResources().getColor(R.color.abs__background_holo_dark));
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
