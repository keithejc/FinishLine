package com.keithcassidy.finishline;

import android.os.Build;

/**
 * A factory to get the {@link ApiAdapter} for the current device.
 *
 * @author Rodrigo Damazio
 */
public class ApiAdapterFactory {

  private static ApiAdapter apiAdapter;

  /**
   * Gets the {@link ApiAdapter} for the current device.
   */
  public static ApiAdapter getApiAdapter() {
    if (apiAdapter == null) {
      if (Build.VERSION.SDK_INT >= 14) {
        apiAdapter = new Api14Adapter();
        return apiAdapter;
      } else if (Build.VERSION.SDK_INT >= 11) {
        apiAdapter = new Api11Adapter();
        return apiAdapter;
      } else if (Build.VERSION.SDK_INT >= 10) {
        apiAdapter = new Api10Adapter();
        return apiAdapter;
      } else if (Build.VERSION.SDK_INT >= 9) {
        apiAdapter = new Api9Adapter();
        return apiAdapter;
      } else {
        apiAdapter = new Api8Adapter();
        return apiAdapter;
      }
    }
    return apiAdapter;
  }
}
