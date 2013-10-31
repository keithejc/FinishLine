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
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class FinishLineDataStorage implements FinishLineDataInterface 
{
	private static final String TAG = FinishLineDataStorage.class.getSimpleName();
	
	private SQLiteDatabase db;
	private FinishLineDbHelper dbHelper;
	
	
	public FinishLineDataStorage(Context context)
	{
		dbHelper = new FinishLineDbHelper(context);
	
	}
	
	public void open() throws SQLException
	{
		db = dbHelper.getWritableDatabase();
	}
	
	public void close()
	{
		dbHelper.close();
	}
	
	public void addManualTime(long manTime)
	{
		ContentValues values = new ContentValues();
		values.put(FinishLineDbHelper.CROSSING_COL_TIME, manTime);
		values.put(FinishLineDbHelper.CROSSING_TYPE_MANUAL, 1);
		
		long id =  db.insert(FinishLineDbHelper.CROSSING_TABLE_NAME, null, values);
        Log.d(TAG, "Adding Crossing: " + id  );
	}
	
	public long addCrossing(Location location)
	{
		ContentValues values = new ContentValues();
		values.put(FinishLineDbHelper.CROSSING_COL_BEARING, location.getBearing());
		values.put(FinishLineDbHelper.CROSSING_COL_LATITUDE, Double.doubleToLongBits(location.getLatitude()));
		values.put(FinishLineDbHelper.CROSSING_COL_LONGITUDE, Double.doubleToLongBits(location.getLongitude()));
		values.put(FinishLineDbHelper.CROSSING_COL_TIME, location.getTime());
		values.put(FinishLineDbHelper.CROSSING_TYPE_MANUAL, 0);
		
		long id =  db.insert(FinishLineDbHelper.CROSSING_TABLE_NAME, null, values);
        Log.d(TAG, "Adding Crossing: " + id  );
        
        return id;
	}
	
	public List<Location> getCrossings()
	{
		List<Location> crossings = new ArrayList<Location>();
		
		Cursor cursor = db.query(FinishLineDbHelper.CROSSING_TABLE_NAME, FinishLineDbHelper.CROSSING_TABLE_ALL_COLS, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast())
		{
			crossings.add(cursorToCrossing(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		
		Log.v(TAG, "getting crossings " );
		return crossings;
	}
	
	private Location cursorToCrossing(Cursor cursor) 
	{
		Location crossing = new Location(LocationManager.GPS_PROVIDER);
		crossing.setTime(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_COL_TIME)));

		//manually added crossings only have a time
		boolean isManual = cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_TYPE_MANUAL)) == 1;
		if( !isManual )
		{
			crossing.setBearing((cursor.getFloat(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_COL_BEARING))));
			crossing.setLatitude( Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_COL_LATITUDE))));
			crossing.setLongitude( Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_COL_LONGITUDE))));
		}
		
		return crossing;		
	}

	public void deleteRaceCrossings() 
	{		
		db.delete(FinishLineDbHelper.CROSSING_TABLE_NAME, null, null);
		Log.d(TAG, "deleting crossings" );
	}
	
	public Buoy getBuoy(String name)
	{
		Buoy buoy = null;
		
		Cursor cursor = db.query(FinishLineDbHelper.BUOY_TABLE_NAME, FinishLineDbHelper.BUOY_TABLE_ALL_COLS, FinishLineDbHelper.BUOY_COL_NAME + " = '" + name + "'", null, null, null, null);
		
		cursor.moveToFirst();
		
		if( cursor.getCount() > 0)
		{
			buoy = cursorToBuoy(cursor);
		}
		
		cursor.close();
		
		return buoy;
	}
	
	public Buoy getBuoy(long idBuoy)
	{
		Buoy buoy = null;
		
		Cursor cursor = db.query(FinishLineDbHelper.BUOY_TABLE_NAME, FinishLineDbHelper.BUOY_TABLE_ALL_COLS, FinishLineDbHelper.BUOY_COL_ID + " = " + idBuoy, null, null, null, null);
		cursor.moveToFirst();
	
		if( cursor.getCount() > 0)
		{
			buoy = cursorToBuoy(cursor);
		}
		
		cursor.close();
		return buoy;
	}
	
	private Buoy cursorToBuoy(Cursor cursor) 
	{
		Buoy buoy = new Buoy();
		
		buoy.Id = cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.BUOY_COL_ID));
		buoy.Name = cursor.getString(cursor.getColumnIndex(FinishLineDbHelper.BUOY_COL_NAME));
		buoy.Position = new LatLng( Double.longBitsToDouble( cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.BUOY_COL_LATITUDE))),
									Double.longBitsToDouble( cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.BUOY_COL_LONGITUDE))));

		return buoy;
	}

	//the buoy name or position has changed - update from the iD
	public void updateBuoy(Buoy buoy)
	{
		ContentValues values = new ContentValues();
		values.put(FinishLineDbHelper.BUOY_COL_LATITUDE, Double.doubleToLongBits(buoy.Position.latitude));
		values.put(FinishLineDbHelper.BUOY_COL_LONGITUDE, Double.doubleToLongBits(buoy.Position.longitude));
		values.put(FinishLineDbHelper.BUOY_COL_NAME, buoy.Name);
	
		long id =  db.update(FinishLineDbHelper.BUOY_TABLE_NAME, values, FinishLineDbHelper.BUOY_COL_NAME + " = '" + buoy.Name + "'", null);
        Log.d(TAG, "Updating buoy: " + id + ", " + buoy.Name );
	}
	
	public boolean doesBuoyExist(String name)
	{
		boolean exists = false;
		
		Cursor cursor = db.query(FinishLineDbHelper.BUOY_TABLE_NAME, FinishLineDbHelper.BUOY_TABLE_ALL_COLS, FinishLineDbHelper.BUOY_COL_NAME + " = '" + name + "'", null, null, null, null);
		
		if( cursor.getCount() > 0)
		{
			exists = true;
		}
		
		cursor.close();
		
		return exists;
	}
	
	public long addBuoy(Buoy buoy)
	{
		ContentValues values = new ContentValues();
		values.put(FinishLineDbHelper.BUOY_COL_LATITUDE, Double.doubleToLongBits(buoy.Position.latitude));
		values.put(FinishLineDbHelper.BUOY_COL_LONGITUDE, Double.doubleToLongBits(buoy.Position.longitude));
		values.put(FinishLineDbHelper.BUOY_COL_NAME, buoy.Name);
	
		long id =  db.insert(FinishLineDbHelper.BUOY_TABLE_NAME, null, values);
        Log.d(TAG, "Adding buoy: " + id );
        
        return id;
	}

	

	public List<String> getAllBuoyNames()
	{
		List<String> buoys = new ArrayList<String>();
		
		Cursor cursor = db.query(FinishLineDbHelper.BUOY_TABLE_NAME, new String[] { FinishLineDbHelper.BUOY_COL_NAME}, null, null, null, null, FinishLineDbHelper.BUOY_COL_NAME);
		if( cursor != null )
		{
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{
				buoys.add(cursor.getString(cursor.getColumnIndex(FinishLineDbHelper.BUOY_COL_NAME)));
				cursor.moveToNext();
			}
			cursor.close();
		}
		
		Log.d(TAG, "getting all buoy names" );
		return buoys;
	}
	

	public List<Buoy> getAllBuoys()
	{
		List<Buoy> buoys = new ArrayList<Buoy>();
		
		Cursor cursor = db.query(FinishLineDbHelper.BUOY_TABLE_NAME, FinishLineDbHelper.BUOY_TABLE_ALL_COLS, null, null, null, null, FinishLineDbHelper.BUOY_COL_NAME);
		if( cursor != null )
		{
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{
				buoys.add(cursorToBuoy(cursor));
				cursor.moveToNext();
			}
			cursor.close();
		}
		
		Log.d(TAG, "getting all buoys" );
		return buoys;
	}
	
	public void deleteBuoy(String name)
	{
	
		db.delete(FinishLineDbHelper.BUOY_TABLE_NAME, FinishLineDbHelper.BUOY_COL_NAME
		        + " = '" + name + "'", null);

		Log.d(TAG, "Deleting buoy: " + name );
	}

	public void deleteCrossing(Location crossing) 
	{
		db.delete(FinishLineDbHelper.CROSSING_TABLE_NAME, FinishLineDbHelper.CROSSING_COL_TIME
		        + " = " + crossing.getTime(), null);

		//Log.d(TAG, "Deleting buoy: " + name );
	}
	
}
