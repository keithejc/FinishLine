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

import com.google.android.gms.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

@SuppressLint("CommitPrefEdits")
public final class PreferencesUtils 
{
	public static final int LOCATION_INTERVAL_DEFAULT = 1000;
	public static final int LOCATION_MIN_DISTANCE_DEFAULT = 0;
	public static final String MAX_ACCURACY_ALLOWED_DEFAULT = "10";
	public static final long RACE_ID_DEFAULT = -1L;
	public static final int AUTO_RESUME_RACE_CURRENT_RETRY_DEFAULT = 3;
	public static final int AUTO_RESUME_RACE_TIMEOUT_DEFAULT = 10;
	public static final int CURRENT_FRAGMENT_DEFAULT = 0;
	public static final double METERS_FORWARD_DEFAULT = 0;
	public static final double METERS_STARBOARD_DEFAULT = 0;
	public static final float FINISHLINE_EXTENSION_DEFAULT = 20;
	public static final String LOCATION_FORMAT_DEFAULT = String.format("%d",Location.FORMAT_SECONDS); 
	public static final boolean AUTO_MAP_UPDATE_DEFAULT = true;

	private PreferencesUtils() {}

	public static boolean getAutoMapUpdate(Context context)
	{
		return getBoolean(context, R.string.auto_map_update_key, AUTO_MAP_UPDATE_DEFAULT);
	}

	public static void setAutoMapUpdate(Context context, boolean autoUpdate)
	{
		setBoolean(context, R.string.auto_map_update_key, autoUpdate);
	}
	
	public static String getBoatName(Context context)
	{
		return getString(context, R.string.boat_name_key, "");
	}

	public static int getCurrentFragment(Context context)
	{
		return getInt(context, R.string.current_fragment_key, CURRENT_FRAGMENT_DEFAULT);
	}

	public static void setCurrentFragment(Context context, int fragment)
	{
		setInt(context, R.string.current_fragment_key, fragment);
	}

	/**
	 * Gets a preference key
	 * 
	 * @param context the context
	 * @param keyId the key id
	 */
	public static String getKey(Context context, int keyId) 
	{
		return context.getString(keyId);
	}

	public static float getFinishLineExtension(Context context)
	{
		return getFloat(context, R.string.finish_line_extension_key, FINISHLINE_EXTENSION_DEFAULT);
	}

	public static Buoy getBouy1(Context context)
	{	  
		Buoy buoy = new Buoy(getLong(context, R.string.buoy_1_id_key, 0),
				getString(context, R.string.buoy_1_name_key,"" ),
				getDouble(context, R.string.buoy_1_latitude_key, 0D),
				getDouble(context, R.string.buoy_1_longitude_key, 0D) );

		return buoy;
	}

	public static Buoy getBouy2(Context context)
	{	  
		Buoy buoy = new Buoy(getLong(context, R.string.buoy_2_id_key, 0),
				getString(context, R.string.buoy_2_name_key,"" ),
				getDouble(context, R.string.buoy_2_latitude_key, 0D),
				getDouble(context, R.string.buoy_2_longitude_key, 0D) );

		return buoy;
	}

	public static void setBouy1(Context context, Buoy buoy)
	{
		setLong(context, R.string.buoy_1_id_key, buoy.Id );
		setString(context, R.string.buoy_1_name_key, buoy.Name );
		setDouble(context, R.string.buoy_1_latitude_key, buoy.Position.latitude );
		setDouble(context, R.string.buoy_1_longitude_key, buoy.Position.longitude );
	}

	public static void setBouy2(Context context, Buoy buoy)
	{
		setLong(context, R.string.buoy_2_id_key, buoy.Id );
		setString(context, R.string.buoy_2_name_key, buoy.Name );
		setDouble(context, R.string.buoy_2_latitude_key, buoy.Position.latitude );
		setDouble(context, R.string.buoy_2_longitude_key, buoy.Position.longitude );
	}

	/**
	 * Gets a double preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param defaultValue the default value
	 */
	public static Double getDouble(Context context, int keyId, Double defaultValue) 
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		return Double.longBitsToDouble( sharedPreferences.getLong(getKey(context, keyId), Double.doubleToLongBits(defaultValue)));
	}

	public static String locationToString(Context context, LatLng latLng)
	{
		return locationToString(context, latLng.latitude, true) + " " + locationToString(context, latLng.longitude, false); 
		
	}
	public static String locationToString(Context context, double latLng, boolean isLatitude)
	{
		String formatString = getString(context, R.string.location_format_key, LOCATION_FORMAT_DEFAULT);
		int formatCode = Integer.parseInt(formatString); 
		String ret = "?";
		if( !Double.isNaN( latLng) ) 
		{
			ret = Location.convert(latLng, formatCode);
		}

		if( formatCode != Location.FORMAT_DEGREES )
		{
			if( isLatitude )
			{
				if( !ret.contains("-"))
				{
					ret = "N" + ret;
				}
				else
				{
					ret = ret.replace('-', 'S');
				}
			}
			else
			{
				if( !ret.contains("-"))
				{
					ret = "E" + ret;
				}
				else
				{
					ret = ret.replace('-', 'W');
				}
			}

		}

		return ret;
	}


	/**
	 * Sets a Double preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param value the value
	 */
	public static void setDouble(Context context, int keyId, Double value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putLong(getKey(context, keyId), Double.doubleToLongBits(value));
		ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	}


	/**
	 * Gets a Float preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param defaultValue the default value
	 */
	public static Float getFloat(Context context, int keyId, Float defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getFloat(getKey(context, keyId), defaultValue);
	}

	/**
	 * Sets a Float preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param value the value
	 */
	public static void setFloat(Context context, int keyId, Float value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putFloat(getKey(context, keyId), value);
		ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	}

	/**
	 * Gets a boolean preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param defaultValue the default value
	 */
	public static boolean getBoolean(Context context, int keyId, boolean defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(getKey(context, keyId), defaultValue);
	}

	/**
	 * Sets a boolean preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param value the value
	 */
	public static void setBoolean(Context context, int keyId, boolean value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(getKey(context, keyId), value);
		ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	}

	/**
	 * Gets an integer preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param defaultValue the default value
	 */
	public static int getInt(Context context, int keyId, int defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getInt(getKey(context, keyId), defaultValue);
	}

	/**
	 * Sets an integer preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param value the value
	 */
	public static void setInt(Context context, int keyId, int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putInt(getKey(context, keyId), value);
		ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	}

	/**
	 * Gets a long preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 */
	public static long getLong(Context context, int keyId, long defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getLong(getKey(context, keyId), defaultValue);
	}

	/**
	 * Sets a long preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param value the value
	 */
	public static void setLong(Context context, int keyId, long value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putLong(getKey(context, keyId), value);
		ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	}

	/**
	 * Gets a string preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param defaultValue default value
	 */
	public static String getString(Context context, int keyId, String defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getString(getKey(context, keyId), defaultValue);
	}

	/**
	 * Sets a string preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param value the value
	 */
	public static void setString(Context context, int keyId, String value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(getKey(context, keyId), value);
		ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	}

	public static int getMaxAccuracyAllowed(Context context) 
	{
		String maxAccuracyAllowedString = getString(context, R.string.max_accuracy_allowed_key, 
				PreferencesUtils.MAX_ACCURACY_ALLOWED_DEFAULT);

		return Integer.parseInt(maxAccuracyAllowedString);
	}


	public static long getLastRaceStopTime(Context context)  
	{
		return getLong(context, R.string.last_race_stop_time_key, 0);
	}

	public static void setLastRaceStopTime(Context context, long lastTime)  
	{
		if( context != null)
		{
			setLong(context, R.string.last_race_stop_time_key, lastTime);
		}
	}

	public static boolean hasEulaBeenShown(Context context) 
	{
		return getBoolean(context, R.string.has_eula_shown_key, false);
	}

	public static void setEulaShown(Context context) 
	{
		setBoolean(context, R.string.has_eula_shown_key, true);
	}
}
