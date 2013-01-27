package com.keithcassidy.finishline;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class FinishLineDbHelper extends SQLiteOpenHelper
{
	private static final String TAG = FinishLineDbHelper.class.getSimpleName();
	
	private static final String DATABASE_NAME = "FinishLine.db";
	private static final int DATABASE_VERSION = 1;
	
    public static final String RACE_TABLE_NAME = "Races";
    public static final String RACE_COL_ID = "_id";
    public static final String RACE_COL_BUOY_1_NAME = "Bouy1Name";
    public static final String RACE_COL_BUOY_2_NAME = "Bouy2Name";
    public static final String RACE_COL_BUOY_1_ID = "Bouy1Id";
    public static final String RACE_COL_BUOY_2_ID = "Bouy2Id";
    public static final String RACE_COL_BUOY_1_LATITUDE = "Bouy1Latitude";
    public static final String RACE_COL_BUOY_2_LATITUDE = "Bouy2Latitude";
    public static final String RACE_COL_BUOY_1_LONGITUDE = "Bouy1Longitude";
    public static final String RACE_COL_BUOY_2_LONGITUDE = "Bouy2Longitude";
    public static final String RACE_COL_STOPTIME = "StopTime";
    public static final String[] RACE_TABLE_ALL_COLS = {RACE_COL_ID, 
											    		RACE_COL_BUOY_1_NAME,
											    		RACE_COL_BUOY_2_NAME,
											    		RACE_COL_BUOY_1_ID,
											    		RACE_COL_BUOY_2_ID,
											    		RACE_COL_BUOY_1_LATITUDE,
											    		RACE_COL_BUOY_2_LATITUDE,
											    		RACE_COL_BUOY_1_LONGITUDE,
											    		RACE_COL_BUOY_2_LONGITUDE,
											    		RACE_COL_STOPTIME};
    		
    		
    private static final String RACE_TABLE_CREATE =
                "CREATE TABLE " + RACE_TABLE_NAME + " (" +
                		RACE_COL_ID + " INTEGER primary key autoincrement, " +
                		RACE_COL_STOPTIME + " INTEGER," +
                		RACE_COL_BUOY_1_NAME + " TEXT," +
                		RACE_COL_BUOY_2_NAME + " TEXT," +
                		RACE_COL_BUOY_1_ID + " INTEGER," +
                		RACE_COL_BUOY_2_ID + " INTEGER," +
                		RACE_COL_BUOY_1_LATITUDE + " INTEGER," +
                		RACE_COL_BUOY_2_LATITUDE + " INTEGER," +
                		RACE_COL_BUOY_1_LONGITUDE + " INTEGER," +
                		RACE_COL_BUOY_2_LONGITUDE + " INTEGER" +
                				");";

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
    public static final String CROSSING_COL_RACE_ID = "RaceId";
    public static final String CROSSING_COL_LATITUDE = "Latitude";
    public static final String CROSSING_COL_LONGITUDE = "Longitude";
    public static final String CROSSING_COL_TIME = "Time";
    public static final String CROSSING_COL_BEARING = "Bearing";
    public static final String[] CROSSING_TABLE_ALL_COLS = { CROSSING_COL_ID,
    														CROSSING_COL_RACE_ID,
    														CROSSING_COL_LATITUDE,
    														CROSSING_COL_LONGITUDE,
    														CROSSING_COL_TIME,
    														CROSSING_COL_BEARING}; 
    	
    	
    private static final String CROSSING_TABLE_CREATE =
            "CREATE TABLE " + CROSSING_TABLE_NAME + " (" +
            		CROSSING_COL_ID + " INTEGER primary key autoincrement, " +
            		CROSSING_COL_RACE_ID + " INTEGER, " +
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
        db.execSQL(RACE_TABLE_CREATE);
        db.execSQL(BUOY_TABLE_CREATE);
        db.execSQL(CROSSING_TABLE_CREATE);

        Log.d(TAG, "Creating database tables");
    }
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		
	}
		
	
}