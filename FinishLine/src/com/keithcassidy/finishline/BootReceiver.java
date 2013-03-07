package com.keithcassidy.finishline;

import static com.keithcassidy.finishline.Constants.RESUME_RACE_EXTRA_NAME;
import static com.keithcassidy.finishline.Constants.TAG;

//import com.google.android.apps.mytracks.services.RemoveTempFilesService;
import com.keithcassidy.finishline.FinishLineService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This class handles MyTracks related broadcast messages.
 *
 * One example of a broadcast message that this class is interested in,
 * is notification about the phone boot.  We may want to resume a previously
 * started tracking session if the phone crashed (hopefully not), or the user
 * decided to swap the battery or some external event occurred which forced
 * a phone reboot.
 *
 * This class simply delegates to {@link TrackRecordingService} to make a
 * decision whether to continue with the previous track (if any), or just
 * abandon it.
 * 
 * @author Bartlomiej Niechwiej
 */
public class BootReceiver extends BroadcastReceiver 
{
  @Override
  public void onReceive(Context context, Intent intent) 
  {
	  
	
    Log.w(TAG, "BootReceiver.onReceive: " + intent.getAction());
    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
    {
      Intent startIntent = new Intent(context, FinishLineService.class)
          .putExtra(RESUME_RACE_EXTRA_NAME, true);
      context.startService(startIntent);
    } 
    else 
    {
      Log.w(TAG, "BootReceiver: unsupported action");
    }
  }
}
