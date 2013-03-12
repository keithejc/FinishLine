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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class FinishLineDbHelper extends SQLiteOpenHelper
{
	private static final String TAG = FinishLineDbHelper.class.getSimpleName();
	
	private static final String DATABASE_NAME = "FinishLine.db";
	private static final int DATABASE_VERSION = 2;

    public static final String BUOY_TABLE_NAME = "Bouys";
    public static final String BUOY_COL_ID = "_id";
    public static final String BUOY_COL_NAME = "Name";
    public static final String BUOY_COL_LATITUDE = "Latitude";
    public static final String BUOY_COL_LONGITUDE = "Longitude";
    public static final String[] BUOY_TABLE_ALL_COLS = {BUOY_COL_ID,
    													BUOY_COL_NAME,
    													BUOY_COL_LATITUDE,
    													BUOY_COL_LONGITUDE}; 

    private static final String BUOY_TABLE_CREATE =
            "CREATE TABLE " + BUOY_TABLE_NAME + " (" +
            		BUOY_COL_ID + " INTEGER primary key autoincrement, " +
            		BUOY_COL_NAME + " TEXT," +
            		BUOY_COL_LATITUDE + " INTEGER," +
            		BUOY_COL_LONGITUDE + " INTEGER );";
    
    public static final String CROSSING_TABLE_NAME = "Crossings";
    public static final String CROSSING_COL_ID = "_id";
    public static final String CROSSING_COL_LATITUDE = "Latitude";
    public static final String CROSSING_COL_LONGITUDE = "Longitude";
    public static final String CROSSING_COL_TIME = "Time";
    public static final String CROSSING_COL_BEARING = "Bearing";
    public static final String[] CROSSING_TABLE_ALL_COLS = { CROSSING_COL_ID,
    														CROSSING_COL_LATITUDE,
    														CROSSING_COL_LONGITUDE,
    														CROSSING_COL_TIME,
    														CROSSING_COL_BEARING}; 
    	
    	
    private static final String CROSSING_TABLE_CREATE =
            "CREATE TABLE " + CROSSING_TABLE_NAME + " (" +
            		CROSSING_COL_ID + " INTEGER primary key autoincrement, " +
            		CROSSING_COL_LATITUDE + " INTEGER, " +
            		CROSSING_COL_LONGITUDE + " INTEGER, " +
            		CROSSING_COL_TIME + " INTEGER, " +
            		CROSSING_COL_BEARING + " FLOAT );"; 
            		
    
    FinishLineDbHelper(Context context) 
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) 
    {
        db.execSQL(BUOY_TABLE_CREATE);
        db.execSQL(CROSSING_TABLE_CREATE);

        Log.d(TAG, "Creating database tables");
    }
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		//delete and start again
		if( oldVersion == 1 && newVersion == 2)
		{
	        Log.d(TAG, "upgrading 1 to 2 database");

	        db.execSQL("DROP TABLE IF EXISTS Crossings");
			db.execSQL("DROP TABLE IF EXISTS Races");
	        db.execSQL(CROSSING_TABLE_CREATE);
		}
	}
		
	
}
