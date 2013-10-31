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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LineCrossHandler 
{
	private static final String TAG = LineCrossHandler.class.getSimpleName();

	private Location lastLocation = null;
	private int maxAccuracyAllowed = 0;
	private Context context = null;
	private FinishLineDataInterface finishLineDataStorage;
	//float lastDistanceToFinish = Float.POSITIVE_INFINITY;
	private float finishLineExtension;
	private boolean isRacing;


	private Buoy buoy1;

	private Buoy buoy2;

	public void setFinishLineExtension(float finishLineExtension) 
	{
		this.finishLineExtension = finishLineExtension;
	}

	public void setBouys(Buoy buoy1, Buoy buoy2) 
	{
		this.buoy1 = buoy1;
		this.buoy2 = buoy2;
	}

	public void setFinishLineDataStorage(FinishLineDataInterface finishLineDataStorage) 
	{
		this.finishLineDataStorage = finishLineDataStorage;
	}

	public void setContext(Context context) 
	{
		this.context = context;
	}

	public int getMaxAccuracyAllowed() 
	{
		return maxAccuracyAllowed;
	}

	public void setMaxAccuracyAllowed(int maxAccuracyAllowed) 
	{
		this.maxAccuracyAllowed = maxAccuracyAllowed;
	}

	private void sendLocalBroadcast(String action, Location data) 
	{
		try
		{
			if( context != null )
			{
				Intent intent = new Intent(Constants.SERVICE_STATUS_MESSAGE).putExtra(action, data);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			}
		}
		catch(Exception e)
		{
			Log.w(TAG, "sendLocalBroadcast Location " + e);
		}
	}		

	private void sendLocalBroadcast(String action, boolean data) 
	{
		try
		{
			if( context != null )
			{
				Intent intent = new Intent(Constants.SERVICE_STATUS_MESSAGE).putExtra(action, data);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			}
		}
		catch(Exception e)
		{
			Log.w(TAG, "sendLocalBroadcast bool " + e);
		}
	}

	private void sendLocalBroadcast(String action, float data) 
	{
		try
		{
			if( context != null )
			{
				Intent intent = new Intent(Constants.SERVICE_STATUS_MESSAGE).putExtra(action, data);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			}
		}
		catch(Exception e)
		{
			Log.w(TAG, "sendLocalBroadcast float " + e);
		}
	}

	public void handleLocationData(Location location) 
	{
		try 
		{

			if (!LocationUtils.isValidLocation(location)) 
			{
				sendLocalBroadcast(Constants.LOCATION_INVALID_MESSAGE, true);
				Log.w(TAG, "Ignore onLocationChangedAsync. location is invalid.");
				return;
			}

			if (location.getAccuracy() > maxAccuracyAllowed)
			{
				sendLocalBroadcast(Constants.LOCATION_INACCURATE_MESSAGE, true);
				Log.d(TAG, "Ignore onLocationChangedAsync. Poor accuracy.");
				return;
			}

			//valid location, test line crossing
			sendLocalBroadcast(Constants.NEW_LOCATION_MESSAGE, location);

			HandleLineCrossing(location);

		}
		catch (Exception e) 
		{
			Log.e(TAG, "Error in handleLocationData ", e);
		}
	}

	//are we near of crossing the finish line  
	private void HandleLineCrossing(Location location) 
	{
		//does this location and bearing intersect with finish line
		DistanceIntersection diToFinish = LocationUtils.distanceToFinish(location, buoy1, buoy2, finishLineExtension);
		sendLocalBroadcast(Constants.FINISHLINE_DISTANCE_MESSAGE, diToFinish.distance);


		if( isRacing )
		{
			//sound appropriate to distance
			startBeepTimer(PlaySounds.getPeriodFromDistance((int)diToFinish.distance));

			//update latest time (used for deciding if service should restart after a crash/reboot
			PreferencesUtils.setLastRaceStopTime(context, location.getTime());


			if( lastLocation != null && lastLocation.distanceTo(location) > LocationUtils.EPSILON)
			{
				Log.d(TAG, "last location time " + lastLocation.getTime() );
				
				
				Location locationLookingBack = new Location(location);
				locationLookingBack.setBearing(locationLookingBack.bearingTo(lastLocation));

				DistanceIntersection diBackToFinish = LocationUtils.distanceToFinish(locationLookingBack, buoy1, buoy2, finishLineExtension);
				if( !Double.isInfinite(diBackToFinish.distance) && diBackToFinish.distance >= 0 && diBackToFinish.intersection != null 
						&& (location.distanceTo(diBackToFinish.intersection) <= location.distanceTo(lastLocation) ))
				{
					long timeOfCrossing = lastLocation.getTime() + (long)((location.getTime() - lastLocation.getTime()) * 
							(lastLocation.distanceTo(diBackToFinish.intersection) / location.distanceTo(lastLocation)) ) ;

					diBackToFinish.intersection.setTime(timeOfCrossing);
					diBackToFinish.intersection.setBearing(lastLocation.bearingTo(location));
					finishLineDataStorage.addCrossing(diBackToFinish.intersection);

					sendLocalBroadcast(Constants.FINISHLINE_CROSSED_MESSAGE, true);

					PlaySounds.playLineCross(context);
				}

			}

			lastLocation = location;
		}
		//lastDistanceToFinish = diToFinish.distance;

		/*	
		if (lastLocation != null && isRacing) 
		{

			//test to see if we crossed the line
			if( !Double.isInfinite(lastDistanceToFinish) && lastDistanceToFinish > 0 && diToFinish.distance <= 0 )
			{
				long timeOfCrossing = lastLocation.getTime() + (long)((location.getTime() - lastLocation.getTime()) * 
						(lastDistanceToFinish / (Math.abs(diToFinish.distance) + lastDistanceToFinish)) ) ;

				Location buoyStart = new Location("na");
				buoyStart.setLatitude(buoy1.Position.latitude);
				buoyStart.setLongitude(buoy1.Position.longitude);

				Location buoyEnd = new Location("na");
				buoyEnd.setLatitude(buoy2.Position.latitude);
				buoyEnd.setLongitude(buoy2.Position.longitude);
				buoyEnd.setBearing(buoyEnd.bearingTo(buoyStart));

				buoyStart.setBearing(buoyStart.bearingTo(buoyEnd));

				//Location locationCrossing = LocationUtils.intersectionOfTwoPaths(lastLocation, buoyStart);
				if( diToFinish.intersection != null )
				{
					diToFinish.intersection.setTime(timeOfCrossing);
					diToFinish.intersection.setBearing(lastLocation.getBearing());
					finishLineDataStorage.addCrossing(diToFinish.intersection);

					sendLocalBroadcast(Constants.FINISHLINE_CROSSED_MESSAGE, true);

					PlaySounds.playLineCross(context);
				}
			}

		}
		 */
	}

	public void initialise() 
	{
		lastLocation = null;
	}

	public void setRacing(boolean isRacing) 
	{
		this.isRacing = isRacing;		
		if( !isRacing )
		{
			stopBeepTimer();
		}
	}


	private ScheduledExecutorService scheduleBeeps;
	private int lastBeepInterval = 0;

	private void stopBeepTimer() 
	{
		if( scheduleBeeps != null )
		{
			scheduleBeeps.shutdown();
		}
		lastBeepInterval = 0;
	}

	private void startBeepTimer(int beepInterval)
	{
		//different interval - reset the timer
		if( lastBeepInterval != beepInterval)
		{
			stopBeepTimer();

			if( beepInterval > 0)
			{
				scheduleBeeps = Executors.newScheduledThreadPool(1);

				scheduleBeeps.scheduleAtFixedRate(new Runnable() 
				{
					public void run() 
					{
						//beep
						PlaySounds.playProximityTone(context);

					}
				}, 0, beepInterval, TimeUnit.MILLISECONDS);

			}		
		}
		
		lastBeepInterval = beepInterval;
	}

}
