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

public class PlaySounds 
{
	enum Sound
	{
		MidTone,
		HighTone,
		ToneQuarterSec,
		ToneHalfSec,
		Tone1Sec
	}

	public static void playStartRace(final Context context)
	{
		if( context != null )
		{
			playTone(context, 1, Sound.MidTone);
		}
	}

	public static void playEndRace(final Context context)
	{
		if( context != null )
		{
			playTone(context, 2, Sound.MidTone);
		}
	}

	public static void playProximity(final Context context, int distance)
	{
		if( context != null )
		{
			if( distance > 0 && distance < 30)
			{
				playTone(context, 1, Sound.ToneHalfSec);
			}			
			else if( distance > 0 && distance < 50 )
			{
				playTone(context, 1, Sound.ToneQuarterSec);
			}
		}
	}

	public static void playLineCross(final Context context)
	{
		if( context != null )
		{
			playTone(context, 2, Sound.Tone1Sec);
		}
	}

	public static void playTone(final Context context, final int count, final Sound soundFile)
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
						}
						player.start();
						countBeep+=1;
						try 
						{
							// 100 millisecond is duration gap between two beep
							Thread.sleep(player.getDuration()+100);
							player.release();
						}
						catch (InterruptedException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}


					}
				}
			};

			t.start();   

		}
	}
}
