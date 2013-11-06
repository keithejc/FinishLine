package com.keithcassidy.finishline;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;


import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BuoyManageActivity extends FragmentActivity 
{

	private ListView mListView;
	private ArrayAdapter<Buoy> mAdapter;
	private FinishLineDataStorage dbStorage = null;
	private int currentListPos = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buoy_manage);

		mListView = (ListView) findViewById(R.id.ListViewBuoys);

		dbStorage = new FinishLineDataStorage(this); 
		dbStorage.open();

		ArrayList<Buoy> buoys = (ArrayList<Buoy>) dbStorage.getAllBuoys();
		mAdapter = new BuoyAdapter(this, R.layout.buoy_list_item, buoys);

		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) 
			{
				currentListPos = position;
				//provide feedback that the item was clicked
				mListView.setItemChecked(position, true);

			}});

	}
	
	public void add(View v)
	{
		Buoy newBuoy = new Buoy();
		newBuoy.Name = getResources().getString(R.string.blankBuoyName);
		newBuoy.Position = new LatLng(0, 0);
		BuoyEditDialog dlg = BuoyEditDialog.newInstance(newBuoy);
		dlg.setBuoyDialogListener(new BuoyDialogListener() 
		{
			@Override
			public void buoySet(Buoy selected) 
			{				
				dbStorage.addBuoy(selected);
				mAdapter.add(selected);
				currentListPos = mAdapter.getCount() - 1;
				mListView.setItemChecked(currentListPos, true);
			}
		});
		dlg.show(getSupportFragmentManager(), "bouy edit dialog fragment");
	}
	
	public void copy(View v)
	{
		if( currentListPos != -1 )
		{
			Buoy copyBuoy = mAdapter.getItem(currentListPos);
	
			Buoy newBuoy = new Buoy();
			newBuoy.Name = getResources().getString(R.string.blankBuoyName);
			newBuoy.Position = new LatLng(copyBuoy.Position.latitude, copyBuoy.Position.longitude);
	
			BuoyEditDialog dlg = BuoyEditDialog.newInstance(newBuoy);
			dlg.setBuoyDialogListener(new BuoyDialogListener() 
			{
				@Override
				public void buoySet(Buoy selected) 
				{				
					dbStorage.addBuoy(selected);
					mAdapter.add(selected);
					currentListPos = mAdapter.getCount() - 1;
					mListView.setItemChecked(currentListPos, true);
				}
			});
			dlg.show(getSupportFragmentManager(), "bouy edit dialog fragment");
		}
	}
		
	
	public void edit(View v)
	{
		if( currentListPos != -1 )
		{
			final Buoy editBuoy = mAdapter.getItem(currentListPos);
			BuoyEditDialog dlg = BuoyEditDialog.newInstance(editBuoy);
			dlg.setBuoyDialogListener(new BuoyDialogListener() 
			{
				@Override
				public void buoySet(Buoy selected) 
				{				
					dbStorage.updateBuoy(selected);
					editBuoy.Name = selected.Name;
					editBuoy.Position = new LatLng(selected.Position.latitude, selected.Position.longitude);
				}
			});
			dlg.show(getSupportFragmentManager(), "bouy edit dialog fragment");
		}
	}
	
	public void delete(View v)
	{
		if( currentListPos != -1 )
		{
			Buoy buoy = mAdapter.getItem(currentListPos);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.confirm_delete_buoy) + buoy.Name).setPositiveButton(getString(R.string.yes), dialogClickListener)
			.setNegativeButton(getString(R.string.no), dialogClickListener).show();
		}
	}
	
	
	//delete buoy?
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case DialogInterface.BUTTON_POSITIVE:
				Buoy buoy = mAdapter.getItem(currentListPos);
				dbStorage.deleteBuoy(buoy.Name);
				mAdapter.remove(buoy);
				currentListPos = -1;
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
				LayoutInflater inflater =  (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
					buoyHolder.name.setTextColor(getResources().getColor(R.color.vpi__background_holo_dark));
					buoyHolder.location.setTextColor(getResources().getColor(R.color.vpi__background_holo_dark));
					((View) buoyHolder.name.getParent()).setBackgroundColor(getResources().getColor(R.color.finishLineHighlight));
				}
				else
				{
					buoyHolder.name.setTextColor(getResources().getColor(R.color.finishLineHighlight));
					buoyHolder.location.setTextColor(getResources().getColor(R.color.finishLineHighlight));
					((View) buoyHolder.name.getParent()).setBackgroundColor(getResources().getColor(R.color.vpi__background_holo_dark));
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
