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
		playTone(context, 1, Sound.MidTone);
	}

	public static void playEndRace(final Context context)
	{
		playTone(context, 2, Sound.MidTone);
	}
	
	public static void playProximity(final Context context, int distance)
	{
		if( distance < 20)
		{
			playTone(context, 1, Sound.ToneHalfSec);
		}			
		else if( distance < 40 )
		{
			playTone(context, 1, Sound.ToneQuarterSec);
		}
	}
	
	public static void playLineCross(final Context context)
	{
		playTone(context, 2, Sound.Tone1Sec);
	}
	
	public static void playTone(final Context context, final int count, final Sound soundFile)
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
