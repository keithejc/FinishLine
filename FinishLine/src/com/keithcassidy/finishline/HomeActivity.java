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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.viewpagerindicator.TitlePageIndicator;

public class HomeActivity extends Activity
{

	protected static final String TAG = null;
	private int currentFragment =0;
	private boolean gpsDialogShownAlready = false;
	private long sysTimeDelta = 0;	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId())
		{
		case R.id.menu_settings:
			showSettings();
			return true;
		case R.id.menu_help:
			showHelp();
			return true;
		case R.id.menu_about:
			showAbout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
	
	private void showAbout() 
	{
		// Create and show the dialog.
		DialogFragment newFragment = AboutDialog.newInstance();
		newFragment.show(getFragmentManager(), "aboutdialog");
	}

	private void showHelp() 
	{
		// TODO Auto-generated method stub

	}

	private void showSettings() 
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	Fragment homeFragment = null;
	Fragment setupFragment1 = null;
	Fragment setupFragment2 = null;
	Fragment lineCrossFragment = null;
	Fragment mapFragment = null;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public void onBackPressed() 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.confirm_quit)).setPositiveButton(getString(R.string.yes), dialogClickListener)
		.setNegativeButton(getString(R.string.no), dialogClickListener).show();
	}    

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case DialogInterface.BUTTON_POSITIVE:
				finish();
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				//No button clicked
				break;
			}
		}
	};	

	public boolean isRacing() 
	{
		return PreferencesUtils.getBoolean(this, R.string.is_racing_key, false);
	}

	@Override
	protected void onDestroy() 
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(onBroadcastServiceStatusReceived);

		PreferencesUtils.setCurrentFragment(this, currentFragment);

		if( !isRacing())
		{
			Log.v(TAG, "xxx Stopping service");
			stopService();
		}

		super.onDestroy();
	}

	
	@Override
	protected void onPause() 
	{
		PreferencesUtils.setCurrentFragment(this, currentFragment);
		if( !isRacing())
		{
			Log.v(TAG, "xxx Stopping service");
			stopService();
		}
		super.onPause();
	}
	
	@Override
	protected void onStart() 
	{
		super.onStart();
		startService();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	
	private void startService()
	{
		Log.v(TAG, "xxx Starting service");
		Intent startIntent = new Intent(this, FinishLineService.class);
		this.startService(startIntent);
	}


	private void stopService()
	{
		Intent stopIntent = new Intent(this, FinishLineService.class);
		this.stopService(stopIntent);
	}


	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private TitlePageIndicator mIndicator;
	private int lastPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		//volume controls will adjust media playback when this app has focus
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();// getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.pinstripe_actionbar_tile));

		currentFragment = PreferencesUtils.getCurrentFragment(this);

		homeFragment = new HomeFragment();
		setupFragment1 = new SetupFragment();
		((SetupFragment)setupFragment1).setBuoyNumber(1);
		setupFragment2 = new SetupFragment();
		((SetupFragment)setupFragment2).setBuoyNumber(2);
		lineCrossFragment = new LineCrossingsFragment();
		mapFragment = new FinishLineMapFragment();

		// Create the adapter that will return a fragment for each of the 
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(currentFragment);

		mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);

		mIndicator.setClipPadding(-10);
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() 
		{
			@Override
			public void onPageSelected(int position) 
			{
				try
				{
					currentFragment = position;
					//					PreferencesUtils.setCurrentFragment(context, currentFragment);


					TabFocusInterface frag =  (TabFocusInterface)mSectionsPagerAdapter.getItem(position);
					frag.tabSetFocus();

					if( lastPosition != -1 )
					{
						frag =  (TabFocusInterface)mSectionsPagerAdapter.getItem(lastPosition);
						frag.tabLoseFocus();
					}

					lastPosition = position;
				}
				catch(Exception e)
				{
					Log.e(TAG, "onPageSelected " + e);
				}

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}
		});

		LocalBroadcastManager.getInstance(this).registerReceiver(onBroadcastServiceStatusReceived,
			      new IntentFilter(Constants.SERVICE_STATUS_MESSAGE));
		
		new SimpleEula(this).show(); 		
	}

	private HomeActivity getHomeActivity()
	{
		return this;
	}
	
	
	private BroadcastReceiver onBroadcastServiceStatusReceived = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{	
			
			ServiceStatusInterface frag =  (ServiceStatusInterface)mSectionsPagerAdapter.getItem(currentFragment);
			if( frag != null)
			{
				((ServiceStatusInterface)homeFragment).onReceiveServiceStatus(context, intent);
			}
			
			if( intent.getBooleanExtra(Constants.GPS_NOT_ENABLED_MESSAGE, false) && gpsDialogShownAlready == false)
			{
				gpsDialogShownAlready = true;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getHomeActivity());
				builder.setMessage(getString(R.string.enable_gps)).setPositiveButton(getString(R.string.show_gps_settings), dialogGpsClickListener)
				.setNegativeButton(getString(R.string.cancel), dialogGpsClickListener).show();
			}
			
			Location newLocation = intent.getParcelableExtra(Constants.NEW_LOCATION_MESSAGE);
			if( newLocation != null )
			{
				calcSysTimeDelta(newLocation.getTime());
			}
			
		}
	};


	DialogInterface.OnClickListener dialogGpsClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which)
			{
			case DialogInterface.BUTTON_POSITIVE:
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				//No button clicked
				break;
			}
		}
	};
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) 
		{
			switch(position)
			{
			case 0:
				return homeFragment;
			case 1:
				return setupFragment1;
			case 2:
				return setupFragment2;
			case 3:
				return lineCrossFragment;
			default:
				return mapFragment;
			}

		}

		@Override
		public int getCount() 
		{
			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) 
		{
			switch (position) 
			{
			case 0:
				return getString(R.string.title_home);
			case 1:
				return getString(R.string.title_setup_buoy1);
			case 2:
				return getString(R.string.title_setup_buoy2);
			case 3:
				return getString(R.string.title_linecrossings);
			case 4:
				return getString(R.string.title_map);
			}
			return null;
		}
	}

	//get diff between sys time and gps time
	protected void calcSysTimeDelta(long gpsTime) 
	{
		sysTimeDelta = System.currentTimeMillis() - gpsTime;
	}

	public void addManualTime()
	{
		FinishLineDataStorage finishLineDataStorage = new FinishLineDataStorage(this);
		finishLineDataStorage.open();
		finishLineDataStorage.addManualTime(System.currentTimeMillis() - sysTimeDelta);
		finishLineDataStorage.close();
		
		PlaySounds.playAddManualTime(this);
	}



}
