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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Geocoder;
import android.location.Location;
import android.os.StrictMode;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

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
	public static final String LOCATION_FORMAT_DEFAULT = String.format(Locale.getDefault(), "%d",Location.FORMAT_SECONDS); 
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
		applyPreferenceChanges(editor);
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
		applyPreferenceChanges(editor);
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
		applyPreferenceChanges(editor);
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
		applyPreferenceChanges(editor);
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
		applyPreferenceChanges(editor);
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
		applyPreferenceChanges(editor);
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
	
	  /**
	   * Applies all the changes done to a given preferences editor. Changes may or
	   * may not be applied immediately.
	   * <p>
	   * Due to changes in API level 9.
	   * 
	   * @param editor the editor
	   */
		  public static void applyPreferenceChanges(Editor editor) {
			    // Apply asynchronously
			    editor.apply();
			  }

	  /**
	   * Enables strict mode where supported, only if this is a development build.
	   * <p>
	   * Due to changes in API level 9.
	   */
		  public static void enableStrictMode() {
			    Log.d(Constants.TAG, "Enabling strict mode");
			    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
			        .detectAll()
			        .penaltyLog()
			        .build());
			    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
			        .detectAll()
			        .penaltyLog()
			        .build());
			  }

	  /**
	   * Copies elements from an input byte array into a new byte array, from
	   * indexes start (inclusive) to end (exclusive). The end index must be less
	   * than or equal to the input length.
	   * <p>
	   * Due to changes in API level 9.
	   *
	   * @param input the input byte array
	   * @param start the start index
	   * @param end the end index
	   * @return a new array containing elements from the input byte array.
	   */
		  public static byte[] copyByteArray(byte[] input, int start, int end) {
			    return Arrays.copyOfRange(input, start, end);
			  }

	  /**
	   * Returns true if GeoCoder is present.
	   * <p>
	   * Due to changes in API level 9.
	   */
		  public static boolean isGeoCoderPresent() {
			    return Geocoder.isPresent();
			  }


	  /**
	   * Hides the title. If the platform supports the action bar, do nothing.
	   * Ideally, with the action bar, we would like to collapse the navigation tabs
	   * into the action bar. However, collapsing is not supported by the
	   * compatibility library.
	   * <p>
	   * Due to changes in API level 11.
	   * 
	   * @param activity the activity
	   */
	  public static void hideTitle(Activity activity) {
		    // Do nothing
		  }

	  /**
	   * Configures the action bar with the Home button as an Up button. If the
	   * platform doesn't support the action bar, do nothing.
	   * <p>
	   * Due to changes in API level 11.
	   *
	   * @param activity the activity
	   */
	   public static void configureActionBarHomeAsUp(Activity activity) {
	    ActionBar actionBar = activity.getActionBar();
	    actionBar.setHomeButtonEnabled(true);
	    actionBar.setDisplayHomeAsUpEnabled(true);
	  }

	  /**
	   * Configures the list view context menu.
	   * <p>
	   * Due to changes in API level 11.
	   *
	   * @param activity the activity
	   * @param listView the list view
	   * @param contextualActionModeCallback the callback when an item is selected
	   *          in the contextual action mode
	   */
	   public static void configureListViewContextualMenu(final Activity activity, ListView listView,
			      final ContextualActionModeCallback contextualActionModeCallback) {
			    listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			      ActionMode actionMode;
			      @Override
			      public boolean onItemLongClick(
			          AdapterView<?> parent, View view, final int position, final long id) {
			        if (actionMode != null) {
			          return false;
			        }
			        actionMode = activity.startActionMode(new ActionMode.Callback() {
			          @Override
			          public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			            mode.getMenuInflater().inflate(R.menu.list_context_menu, menu);
			            return true;
			          }
			          @Override
			          public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			            // Return false to indicate no change.
			            return false;
			          }
			          @Override
			          public void onDestroyActionMode(ActionMode mode) {
			            actionMode = null;
			          }
			          @Override
			          public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			            mode.finish();
			            return contextualActionModeCallback.onClick(item.getItemId(), position, id);
			          }
			        });
			        TextView textView = (TextView) view.findViewById(R.id.list_item_name);
			        if (textView != null) {
			          actionMode.setTitle(textView.getText());
			        }
			        view.setSelected(true);
			        return true;
			      }
			    });
			  };

	  /**
	   * Configures the search widget.
	   * <p>
	   * Due to changes in API level 11.
	   * 
	   * @param activity the activity
	   * @param menuItem the search menu item
	   */
	  public static void configureSearchWidget(Activity activity, final MenuItem menuItem) 
	  {
	    SearchView searchView = (SearchView) menuItem.getActionView();
	    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() 
	    {
	      @Override
	      public boolean onQueryTextSubmit(String query) 
	      {
	        menuItem.collapseActionView();
	        return false;
	      }
	      @Override
	      public boolean onQueryTextChange(String newText) 
	      {
	        return false;
	      }
	    });
	    searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
	        @Override
	      public boolean onSuggestionSelect(int position) {
	        return false;
	      }
	      @Override
	      public boolean onSuggestionClick(int position) {
	        menuItem.collapseActionView();
	        return false;
	      }
	    });
	  }
	 
	  /**
	   * Handles the search menu selection. Returns true if handled.
	   * <p>
	   * Due to changes in API level 11.
	   * 
	   * @param activity the activity
	   */
	  public static boolean handleSearchMenuSelection(Activity activity) {
		    // Returns false to allow the platform to expand the search widget.
		    return false;
		  }
	  
	  /**
	   * Adds all items to an array adapter.
	   * <p>
	   * Due to changes in API level 11.
	   *
	   * @param arrayAdapter the array adapter
	   * @param items list of items
	   */
	  public static <T> void addAllToArrayAdapter(ArrayAdapter<T> arrayAdapter, List<T> items) {
		    arrayAdapter.addAll(items);
		  }

	  /**
	   * Invalidates the menu.
	   * <p>
	   * Due to changes in API level 11.
	   */
	  public static void invalidMenu(Activity activity) {
		    activity.invalidateOptionsMenu();
		  }

	  /**
	   * Handles the search key press. Returns true if handled.
	   * <p>
	   * Due to changes in API level 14.
	   * 
	   * @param menu the search menu
	   */
	  public static boolean handleSearchKey(MenuItem menuItem) {
		    menuItem.expandActionView();
		    return true;
		  }
	
}
