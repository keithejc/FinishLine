package com.keithcassidy.finishline;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.view.MenuItem;
import android.widget.SearchView;

/**
 * API level 14 specific implementation of the {@link ApiAdapter}.
 * 
 * @author Jimmy Shih
 */
@TargetApi(14)
public class Api14Adapter extends Api11Adapter {

  @Override
  public void configureActionBarHomeAsUp(Activity activity) {
    ActionBar actionBar = activity.getActionBar();
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void configureSearchWidget(Activity activity, final MenuItem menuItem) {
    super.configureSearchWidget(activity, menuItem);
    SearchView searchView = (SearchView) menuItem.getActionView();
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        menuItem.collapseActionView();
        return false;
      }
      @Override
      public boolean onQueryTextChange(String newText) {
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

  @Override
  public boolean handleSearchKey(MenuItem menuItem) {
    menuItem.expandActionView();
    return true;
  }
}
