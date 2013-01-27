package com.keithcassidy.finishline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressLint("CommitPrefEdits")
public final class PreferencesUtils 
{
	public static final int LOCATION_INTERVAL_DEFAULT = 1000;
	public static final int LOCATION_MIN_DISTANCE_DEFAULT = 0;
	public static final int MAX_ACCURACY_ALLOWED_DEFAULT = 10;
	public static final long RACE_ID_DEFAULT = -1L;
	public static final int AUTO_RESUME_RACE_CURRENT_RETRY_DEFAULT = 3;
	public static final int AUTO_RESUME_RACE_TIMEOUT_DEFAULT = 10;
	
	  private PreferencesUtils() {}

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
	  public static Double getDouble(Context context, int keyId, Double defaultValue) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
	    return Double.longBitsToDouble( sharedPreferences.getLong(getKey(context, keyId), Double.doubleToLongBits(defaultValue)));
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
	  
}
