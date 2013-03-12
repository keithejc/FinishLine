/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.keithcassidy.finishline;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * API level 8 specific implementation of the {@link ApiAdapter}.
 *
 * @author Bartlomiej Niechwiej
 */
public class Api8Adapter implements ApiAdapter {

  @Override
  public void applyPreferenceChanges(Editor editor) {
    editor.commit();
  }

  @Override
  public void enableStrictMode() {
    // Not supported
  }

  @Override
  public byte[] copyByteArray(byte[] input, int start, int end) {
    int length = end - start;
    byte[] output = new byte[length];
    System.arraycopy(input, start, output, 0, length);
    return output;
  }

  @Override
  public boolean isGeoCoderPresent() {
    return true;
  }

  @Override
  public void hideTitle(Activity activity) {
    activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
  }

  @Override
  public void configureActionBarHomeAsUp(Activity activity) {
    // Do nothing
  }

  @Override
  public void configureListViewContextualMenu(Activity activity, ListView listView,
      ContextualActionModeCallback contextualActionModeCallback) {
    activity.registerForContextMenu(listView);
  }

  @Override
  public void configureSearchWidget(Activity activity, MenuItem menuItem) {
    // Do nothing
  }

  @Override
  public boolean handleSearchMenuSelection(Activity activity) {
    activity.onSearchRequested();
    return true;
  }

  @Override
  public <T> void addAllToArrayAdapter(ArrayAdapter<T> arrayAdapter, List<T> items) {
    for (T item : items) {
      arrayAdapter.add(item);
    }
  }

  @Override
  public void invalidMenu(Activity activity) {
    // Do nothing
  }

  @Override
  public boolean handleSearchKey(MenuItem menuItem) {
    // Return false and allow the framework to handle the search key.
    return false;
  }
}
