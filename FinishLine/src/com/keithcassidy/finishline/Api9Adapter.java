package com.keithcassidy.finishline;

import com.keithcassidy.finishline.Constants;

import android.annotation.TargetApi;
import android.content.SharedPreferences.Editor;
import android.location.Geocoder;
import android.os.StrictMode;
import android.util.Log;

import java.util.Arrays;

/**
 * API level 9 specific implementation of the {@link ApiAdapter}.
 *
 * @author Rodrigo Damazio
 */
@TargetApi(9)
public class Api9Adapter extends Api8Adapter {
  
  @Override
  public void applyPreferenceChanges(Editor editor) {
    // Apply asynchronously
    editor.apply();
  }

  @Override
  public void enableStrictMode() {
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

  @Override
  public byte[] copyByteArray(byte[] input, int start, int end) {
    return Arrays.copyOfRange(input, start, end);
  }

  @Override
  public boolean isGeoCoderPresent() {
    return Geocoder.isPresent();
  }
}
