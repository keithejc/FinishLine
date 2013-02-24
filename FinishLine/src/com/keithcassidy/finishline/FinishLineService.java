package com.keithcassidy.finishline;

import java.util.Date;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class FinishLineService extends Service 
{
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();	
    
    public class LocalBinder extends Binder {
    	FinishLineService getService() 
    	{
            // Return this instance of LocalService so clients can call public methods
            return FinishLineService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	
	private static final String INTENT_RACE_ID = "RaceId";

	private static final String TAG = FinishLineService.class.getSimpleName();

	// One second in milliseconds
	private static final long ONE_SECOND = 1000;
	// One minute in milliseconds
	private static final long ONE_MINUTE = 60 * ONE_SECOND;

	static final int MAX_AUTO_RESUME_TRACK_RETRY_ATTEMPTS = 3;

	//set in onCreate:
	private Context context;
	private FinishLineDataStorage finishLineDataStorage;
	private LocationManager locationManager;
	private SharedPreferences sharedPreferences;
	private long raceId;
	private Race race = null;
	private long locationPollingInterval;
	private long minDistance;
	private long autoResumeRaceTimeout;

	//created when race starts
	private WakeLock wakeLock;

	private LineCrossHandler lineCrossHandler;

	protected int maxAccuracyAllowed;	
	/*
	 * Note that sharedPreferenceChangeListener cannot be an anonymous inner
	 * class. Anonymous inner class will get garbage collected.
	 */
	private final OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() 
	{
		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

			if (key == null || key.equals(PreferencesUtils.getKey(context, R.string.location_interval_key))) 
			{
				locationPollingInterval = PreferencesUtils.getInt(context, 
						R.string.location_interval_key, 
						PreferencesUtils.LOCATION_INTERVAL_DEFAULT);
			}

			if (key == null || key.equals(PreferencesUtils.getKey(context, R.string.race_id_key))) 
			{
				raceId = PreferencesUtils.getLong(context, R.string.race_id_key, PreferencesUtils.RACE_ID_DEFAULT);
			}
			
			if (key == null || key.equals(PreferencesUtils.getKey(context, R.string.max_accuracy_allowed_key))) 
			{
				maxAccuracyAllowed = (PreferencesUtils.getInt(context, R.string.max_accuracy_allowed_key, 
						PreferencesUtils.MAX_ACCURACY_ALLOWED_DEFAULT));
				
				if( lineCrossHandler != null)
				{
					lineCrossHandler.setMaxAccuracyAllowed(maxAccuracyAllowed);
				}
			}
			
			if (key == null || key.equals(PreferencesUtils.getKey(context, R.string.auto_resume_race_timeout_key))) 
			{
				autoResumeRaceTimeout = PreferencesUtils.getInt(context, R.string.auto_resume_race_timeout_key,
						PreferencesUtils.AUTO_RESUME_RACE_TIMEOUT_DEFAULT);
			}
			
			if (key == null || key.equals(PreferencesUtils.getKey(context, R.string.min_distance_key))) 
			{
				minDistance = PreferencesUtils.getInt(context, R.string.min_distance_key, 
						PreferencesUtils.LOCATION_MIN_DISTANCE_DEFAULT);
			}

			if (race != null &&( key.equals(PreferencesUtils.getKey(context, R.string.buoy_1_latitude_key)) ||
							   key.equals(PreferencesUtils.getKey(context, R.string.buoy_1_longitude_key))) ) 
			{
				Buoy buoy1 = PreferencesUtils.getBouy1(context);
				race.setBuoy1(buoy1);
				finishLineDataStorage.updateRace(race);
			}

			if (race != null &&( key.equals(PreferencesUtils.getKey(context, R.string.buoy_2_latitude_key)) ||
					key.equals(PreferencesUtils.getKey(context, R.string.buoy_2_longitude_key))) ) 
			{
				Buoy buoy2 = PreferencesUtils.getBouy2(context);
				race.setBuoy2(buoy2);
				finishLineDataStorage.updateRace(race);
			}
			
		}
	};

	private void sendLocalBroadcast(String action, boolean data) 
	{
		Intent intent = new Intent(Constants.SERVICE_STATUS_MESSAGE).putExtra(action, data);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	
	private LocationListener locationListener = new LocationListener() 
	{
		@Override
		public void onProviderDisabled(String provider) 
		{
			sendLocalBroadcast(Constants.GPS_NOT_ENABLED_MESSAGE, true);
		}

		@Override
		public void onProviderEnabled(String provider) 
		{
			sendLocalBroadcast(Constants.GPS_ENABLED_MESSAGE, true);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) 
		{
			// Do nothing
		}

		@Override
		public void onLocationChanged(final Location location) 
		{
			if( lineCrossHandler != null)
	    	{
				lineCrossHandler.handleLocationData(location);
	    	}
		}
	};

	@Override
	public void onCreate() 
	{
		super.onCreate();
		context = this;
		finishLineDataStorage = new FinishLineDataStorage(context);
		finishLineDataStorage.open();
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		sharedPreferences = getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

		// onSharedPreferenceChanged might not set recordingTrackId.
		raceId = PreferencesUtils.RACE_ID_DEFAULT;

		// Require announcementExecutor and splitExecutor to be created.
		sharedPreferenceChangeListener.onSharedPreferenceChanged(sharedPreferences, null);


		/*
		 * Try to restart the previous recording track in case the service has been
		 * restarted by the system, which can sometimes happen.
		 */
		race = finishLineDataStorage.getRace(raceId);
		
		lineCrossHandler = new LineCrossHandler();
		lineCrossHandler.setContext(this);
		lineCrossHandler.setFinishLineDataStorage(finishLineDataStorage);
		lineCrossHandler.setMaxAccuracyAllowed(maxAccuracyAllowed);
		lineCrossHandler.initialise();

		acquireWakeLock();
		registerLocationListener();
		
		if (race != null) 
		{
			restartRace(race);
		} 
		else 
		{
			if (isRacing()) 
			{
				Log.w(TAG, "race is null, but raceId not -1L. " + raceId);
				updateRacingState(PreferencesUtils.RACE_ID_DEFAULT);
			}
			showNotification();
		}
		
	}

	@Override
	public void onStart(Intent intent, int startId) 
	{
		handleRestartOnBoot(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		handleRestartOnBoot(intent, startId);
		return START_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		showNotification();

		sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
		unregisterLocationListener();


		// Make sure we have no indirect references to this service.
		finishLineDataStorage.close();
		finishLineDataStorage = null;

		// This should be the next to last operation
		releaseWakeLock();

		super.onDestroy();
	}

	public boolean isRacing() 
	{
		return raceId != PreferencesUtils.RACE_ID_DEFAULT;
	}


	protected void startForegroundService(Notification notification) 
	{
		startForeground(1, notification);
	}

	protected void stopForegroundService() 
	{
		stopForeground(true);
	}

	private void handleRestartOnBoot(Intent intent, int startId) 
	{
		// Check if the service is called to resume track (from phone reboot)
		if (intent != null && intent.getBooleanExtra(Constants.RESUME_RACE_EXTRA_NAME, false)) 
		{
			race = finishLineDataStorage.getRace(raceId);
			
			if (!shouldResumeRace(race)) 
			{
				Log.i(TAG, "Stop resume track.");
				updateRacingState(PreferencesUtils.RACE_ID_DEFAULT);
				stopSelfResult(startId);
				return;
			}
			else
			{
				restartRace(race);
			}
		}
	}

	private boolean shouldResumeRace(Race race) 
	{

		if (race == null) 
		{
			Log.d(TAG, "Not resuming. Track is null.");
			return false;
		}

		int retries = PreferencesUtils.getInt(this, R.string.auto_resume_race_current_retry_key, PreferencesUtils.AUTO_RESUME_RACE_CURRENT_RETRY_DEFAULT);
		if (retries >= MAX_AUTO_RESUME_TRACK_RETRY_ATTEMPTS) 
		{
			Log.d(TAG, "Not resuming. Exceeded maximum retry attempts.");
			return false;
		}
		PreferencesUtils.setInt(this, R.string.auto_resume_race_current_retry_key, retries + 1);

		long stopTime = race.getStopTime();
		return stopTime > 0
				&& (System.currentTimeMillis() - stopTime) <= autoResumeRaceTimeout * ONE_MINUTE;
	}


	public long startNewRace() 
	{
		if (isRacing()) 
		{
			Log.d(TAG, "Ignore startNewRace. Already racing.");
			return -1L;
		}

		// create a race
		race = new Race();
		race.setBuoy1(PreferencesUtils.getBouy1(context));
		race.setBuoy2(PreferencesUtils.getBouy2(context));
		raceId = finishLineDataStorage.addRace(race);
		race.setId(raceId);

		
		//store current race in case service is restarted
		updateRacingState(raceId);
		
		//resume race retries to 0
		PreferencesUtils.setInt(this, R.string.auto_resume_race_current_retry_key, 0);

		startRacing(race, true);
		return raceId;
	}


	private void restartRace(Race race) 
	{
		Log.d(TAG, "Restarting race: " + raceId);
		startRacing(race, false);
	}

	private void startRacing(Race race, boolean raceStarted) 
	{

		// Update instance variables
		lineCrossHandler.initialise();
		lineCrossHandler.setRace(race);

		// Send notifications
		showNotification();
		
		Toast.makeText(getBaseContext(), raceStarted ? getString(R.string.starting_race)
				: getString(R.string.resuming_race), Toast.LENGTH_LONG).show();
    	
		if( raceStarted)
    	{
			PlaySounds.playStartRace(this);
    	}
	}

	public void stopRace()
	{
		// Update shared preferences
		updateRacingState(PreferencesUtils.RACE_ID_DEFAULT);


		if (race != null) 
		{
			Date date = new Date();		    	
			race.setStopTime(date.getTime());
			finishLineDataStorage.updateRace(race);
		}
		lineCrossHandler.setRace(null);
		
		showNotification();
		
    	Toast.makeText(getBaseContext(), getString(R.string.stopping_race), Toast.LENGTH_LONG).show();
		
    	PlaySounds.playEndRace(this);
	}



	private void updateRacingState(long updatingRaceId) 
	{
		raceId = updatingRaceId;
		PreferencesUtils.setLong(this, R.string.race_id_key, raceId);
	}


	private void registerLocationListener() 
	{
		unregisterLocationListener();

		try 
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationPollingInterval, minDistance, locationListener);
		} 
		catch (RuntimeException e) 
		{
			Log.e(TAG, "Could not request Location Updates.", e);
		}
	}

	private void unregisterLocationListener() 
	{
		if (locationManager == null) 
		{
			Log.e(TAG, "locationManager is null.");
			return;
		}
		locationManager.removeUpdates(locationListener);
	}

	private void acquireWakeLock() 
	{
		try 
		{
			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			if (powerManager == null) 
			{
				Log.e(TAG, "powerManager is null.");
				return;
			}

			if (wakeLock == null) 
			{
				wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
				if (wakeLock == null) 
				{
					Log.e(TAG, "wakeLock is null.");
					return;
				}
			}

			if (!wakeLock.isHeld()) 
			{
				wakeLock.acquire();
				if (!wakeLock.isHeld()) 
				{
					Log.e(TAG, "Unable to hold wakeLock.");
				}
			}

		} 
		catch (RuntimeException e) 
		{
			Log.e(TAG, "Caught RuntimeException exception in acquireWakeLock", e);
		}
	}

	private void releaseWakeLock() 
	{
		if (wakeLock != null && wakeLock.isHeld()) 
		{
			wakeLock.release();
			wakeLock = null;
		}
	}

	private void showNotification() 
	{
		if (isRacing())
		{
			Intent intent = NewIntent(this, HomeActivity.class).putExtra(INTENT_RACE_ID, raceId);
			
			TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
			taskStackBuilder.addNextIntent(intent);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentIntent(
					taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT))
					.setContentText(getString(R.string.race_notification))
					.setContentTitle(getString(R.string.app_name_is_running)).setOngoing(true)
					.setSmallIcon(R.drawable.finish_line_notification_icon).setWhen(System.currentTimeMillis());
			
			startForegroundService(builder.build());
		}
		else
		{
			stopForegroundService();
		}
	}


	public static final Intent NewIntent(Context context, Class<?> cls) 
	{
		return new Intent(context, cls).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	}
	
		
}