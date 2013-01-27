package com.keithcassidy.finishline;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A set of methods that may be implemented differently depending on the Android
 * API level.
 *
 * @author Bartlomiej Niechwiej
 */
public interface ApiAdapter {

  /**
   * Applies all the changes done to a given preferences editor. Changes may or
   * may not be applied immediately.
   * <p>
   * Due to changes in API level 9.
   * 
   * @param editor the editor
   */
  public void applyPreferenceChanges(SharedPreferences.Editor editor);

  /**
   * Enables strict mode where supported, only if this is a development build.
   * <p>
   * Due to changes in API level 9.
   */
  public void enableStrictMode();

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
  public byte[] copyByteArray(byte[] input, int start, int end);

  /**
   * Returns true if GeoCoder is present.
   * <p>
   * Due to changes in API level 9.
   */
  public boolean isGeoCoderPresent();


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
  public void hideTitle(Activity activity);

  /**
   * Configures the action bar with the Home button as an Up button. If the
   * platform doesn't support the action bar, do nothing.
   * <p>
   * Due to changes in API level 11.
   *
   * @param activity the activity
   */
  public void configureActionBarHomeAsUp(Activity activity);

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
  public void configureListViewContextualMenu(Activity activity, ListView listView,
      ContextualActionModeCallback contextualActionModeCallback);

  /**
   * Configures the search widget.
   * <p>
   * Due to changes in API level 11.
   * 
   * @param activity the activity
   * @param menuItem the search menu item
   */
  public void configureSearchWidget(Activity activity, MenuItem menuItem);
 
  /**
   * Handles the search menu selection. Returns true if handled.
   * <p>
   * Due to changes in API level 11.
   * 
   * @param activity the activity
   */
  public boolean handleSearchMenuSelection(Activity activity);
  
  /**
   * Adds all items to an array adapter.
   * <p>
   * Due to changes in API level 11.
   *
   * @param arrayAdapter the array adapter
   * @param items list of items
   */
  public <T> void addAllToArrayAdapter(ArrayAdapter<T> arrayAdapter, List<T> items);

  /**
   * Invalidates the menu.
   * <p>
   * Due to changes in API level 11.
   */
  public void invalidMenu(Activity activity);

  /**
   * Handles the search key press. Returns true if handled.
   * <p>
   * Due to changes in API level 14.
   * 
   * @param menu the search menu
   */
  public boolean handleSearchKey(MenuItem menu);  
}
