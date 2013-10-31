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

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class PlaySounds 
{
	enum Sound
	{
		MidTone,
		HighTone,
		ToneQuarterSec,
		ToneHalfSec,
		Tone1Sec,
		Tick,
		VHighTone
	}

	protected static final String TAG = "PlaySounds";
	
	public static int getPeriodFromDistance(double distance)
	{
		if( distance > 0 && distance < 10)
		{
			return 250;
		}
		if( distance > 0 && distance < 20)
		{
			return 500;
		}
		else if( distance > 0 && distance < 30)
		{
			return 1000;
		}			
		else if( distance > 0 && distance < 50 )
		{
			return 2000;
		}
		else
		{
			return 0;
		}
	}

	public static void playStartRace(final Context context)
	{
		if( context != null )
		{
			playTone(context, Sound.ToneQuarterSec);
		}
	}

	public static void playEndRace(final Context context)
	{
		if( context != null )
		{
			playTone(context, Sound.ToneQuarterSec, 2, 5);
		}
	}

	public static void playAddManualTime(final Context context)
	{
		if( context != null )
		{
			playTone(context, Sound.Tick, 3, 1);
		}
	}
	
	public static void playProximityTone(final Context context)
	{
		if( context != null )
		{
			playTone(context, Sound.Tick);
		}
	}

	public static void playLineCross(final Context context)
	{
		if( context != null )
		{
			playTone(context, Sound.VHighTone, 3, 1);
		}
	}

	public static void playTone(final Context context, final Sound soundFile)
	{
		playTone(context, soundFile, 1, 0);
	}
	
	public static void playTone(final Context context, final Sound soundFile, final int count, final int msGap)
	{
		if( context != null )
		{
			Thread t = new Thread()
			{
				public void run()
				{
					MediaPlayer player = null;
					int countBeep = 0;
					while(countBeep < count)
					{
						switch(soundFile)
						{
						case MidTone:
							player = MediaPlayer.create(context,R.raw.beepmid);
							break;
						case HighTone:
							player = MediaPlayer.create(context,R.raw.beephigh);
							break;
						case ToneQuarterSec:
							player = MediaPlayer.create(context,R.raw.beepquartersec);
							break;
						case ToneHalfSec:
							player = MediaPlayer.create(context,R.raw.beephalfsec);
							break;
						case Tone1Sec:
							player = MediaPlayer.create(context,R.raw.beep1sec);
							break;
						case Tick:
							player = MediaPlayer.create(context,R.raw.beeptick);
							break;
						case VHighTone:
							player = MediaPlayer.create(context,R.raw.beepvhigh);
							break;
						}
						player.start();
						countBeep+=1;
						try 
						{
							// millisecond gap is duration gap between two beep
							Thread.sleep(player.getDuration() + msGap);
							player.release();
						}
						catch (Exception e) 
						{
							// TODO Auto-generated catch block
						   	Log.e(TAG, "playTone ", e );
						}


					}
				}
			};

			t.start();   

		}
	}
}
