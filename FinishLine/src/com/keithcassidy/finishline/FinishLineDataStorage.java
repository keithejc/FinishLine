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
	
	
	public Race getRace(long idRace) 
	{
		Race race = null;

		Cursor cursor = db.query(FinishLineDbHelper.RACE_TABLE_NAME, FinishLineDbHelper.RACE_TABLE_ALL_COLS, FinishLineDbHelper.RACE_COL_ID + " = " + idRace, null, null, null, null);
		
		if( cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			race = cursorToRace(cursor);
		}		
		
		cursor.close();
		
		Log.d(TAG, "getting race: " + idRace );
		
		return race;
	}

	private Race cursorToRace(Cursor cursor) 
	{
		Race race = new Race();
		race.setId(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_ID)));
		race.setStopTime(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_STOPTIME)));
		
		Buoy buoy1 = new Buoy();
		buoy1.Id = cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_BUOY_1_ID));
		buoy1.Name = cursor.getString(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_BUOY_1_NAME));
		buoy1.Position = new LatLng(Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_BUOY_1_LATITUDE))),
									Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_BUOY_1_LONGITUDE))));
		race.setBuoy1(buoy1);
		
		Buoy buoy2 = new Buoy();
		buoy2.Id = cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_BUOY_2_ID));
		buoy2.Name = cursor.getString(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_BUOY_2_NAME));
		buoy2.Position = new LatLng(Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_BUOY_2_LATITUDE))),
									Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.RACE_COL_BUOY_2_LONGITUDE))));
		race.setBuoy2(buoy2);
		
		race.setLineCrossings(getCrossingsForRace(race.getId()));
		
		return race;
	}

	public long addRace(Race race) 
	{				
		ContentValues values = new ContentValues();
		values.put(FinishLineDbHelper.RACE_COL_STOPTIME, System.currentTimeMillis());
		values.put(FinishLineDbHelper.RACE_COL_BUOY_1_ID, race.getBuoy1().Id);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_1_LATITUDE, race.getBuoy1().Position.latitude);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_1_LONGITUDE, race.getBuoy1().Position.longitude);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_1_NAME, race.getBuoy1().Name);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_2_ID, race.getBuoy2().Id);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_2_LATITUDE, race.getBuoy2().Position.latitude);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_2_LONGITUDE, race.getBuoy2().Position.longitude);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_2_NAME, race.getBuoy2().Name);
	
		long id =  db.insert(FinishLineDbHelper.RACE_TABLE_NAME, null, values);
        Log.d(TAG, "Adding race: " + id );
        
        return id;
	}

	public void deleteRace(long id)
	{
		deleteRaceCrossings(id);
		
		db.delete(FinishLineDbHelper.RACE_TABLE_NAME, FinishLineDbHelper.RACE_COL_ID
		        + " = " + id, null);

		Log.d(TAG, "Deleting race: " + id );
	}
	
	public void updateRace(Race race) 
	{
		ContentValues values = new ContentValues();
		values.put(FinishLineDbHelper.RACE_COL_STOPTIME, race.getStopTime());
		values.put(FinishLineDbHelper.RACE_COL_BUOY_1_ID, race.getBuoy1().Id);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_1_LATITUDE, race.getBuoy1().Position.latitude);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_1_LONGITUDE, race.getBuoy1().Position.longitude);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_1_NAME, race.getBuoy1().Name);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_2_ID, race.getBuoy2().Id);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_2_LATITUDE, race.getBuoy2().Position.latitude);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_2_LONGITUDE, race.getBuoy2().Position.longitude);
		values.put(FinishLineDbHelper.RACE_COL_BUOY_2_NAME, race.getBuoy2().Name);
		
		db.update(FinishLineDbHelper.RACE_TABLE_NAME, values, FinishLineDbHelper.RACE_COL_ID + " = " + race.getId(), null);
		
		Log.d(TAG, "Updating race: " + race.getId() );
	
	}

	public long addCrossing(long idRace, Location location)
	{
		ContentValues values = new ContentValues();
		values.put(FinishLineDbHelper.CROSSING_COL_BEARING, location.getBearing());
		values.put(FinishLineDbHelper.CROSSING_COL_LATITUDE, Double.doubleToLongBits(location.getLatitude()));
		values.put(FinishLineDbHelper.CROSSING_COL_LONGITUDE, Double.doubleToLongBits(location.getLongitude()));
		values.put(FinishLineDbHelper.CROSSING_COL_RACE_ID, idRace);
		values.put(FinishLineDbHelper.CROSSING_COL_TIME, location.getTime());
		
		long id =  db.insert(FinishLineDbHelper.CROSSING_TABLE_NAME, null, values);
        Log.d(TAG, "Adding Crossing: " + id + " to race: " + idRace );
        
        return id;
	}
	
	public List<Location> getCrossingsForRace(long idRace)
	{
		List<Location> crossings = new ArrayList<Location>();
		
		Cursor cursor = db.query(FinishLineDbHelper.CROSSING_TABLE_NAME, FinishLineDbHelper.CROSSING_TABLE_ALL_COLS, FinishLineDbHelper.CROSSING_COL_RACE_ID + " = " + idRace, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast())
		{
			crossings.add(cursorToCrossing(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		
		Log.d(TAG, "getting crossings for race: " + idRace );
		return crossings;
	}
	
	private Location cursorToCrossing(Cursor cursor) 
	{
		Location crossing = new Location(LocationManager.GPS_PROVIDER);
		
		crossing.setTime(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_COL_TIME)));
		crossing.setBearing((cursor.getFloat(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_COL_BEARING))));
		crossing.setLatitude( Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_COL_LATITUDE))));
		crossing.setLongitude( Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex(FinishLineDbHelper.CROSSING_COL_LONGITUDE))));
		
		return crossing;		
	}

	private void deleteRaceCrossings(long idRace) 
	{		
		db.delete(FinishLineDbHelper.CROSSING_TABLE_NAME, FinishLineDbHelper.CROSSING_COL_RACE_ID
		        + " = " + idRace, null);
		Log.d(TAG, "deleting crossings for race: " + idRace );
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
	
	public Cursor getAllBuoysCursor()
	{
		List<Buoy> buoys = new ArrayList<Buoy>();
		
		Cursor cursor = db.query(FinishLineDbHelper.BUOY_TABLE_NAME, FinishLineDbHelper.BUOY_TABLE_ALL_COLS, null, null, null, null, null);
		if( cursor != null )
		{
			cursor.moveToFirst();
		}
		Log.d(TAG, "getting all buoys" );
		return cursor;
	}

	public void deleteBuoy(String name)
	{
	
		db.delete(FinishLineDbHelper.BUOY_TABLE_NAME, FinishLineDbHelper.BUOY_COL_NAME
		        + " = '" + name + "'", null);

		Log.d(TAG, "Deleting buoy: " + name );
	}
	
}
