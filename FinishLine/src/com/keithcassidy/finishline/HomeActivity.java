package com.keithcassidy.finishline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

public class HomeActivity extends SherlockFragmentActivity
{

	protected static final String TAG = null;
	private int currentFragment =0;

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
		newFragment.show(getSupportFragmentManager(), "aboutdialog");
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
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) 
	{
		getSupportMenuInflater().inflate(R.menu.options_menu, menu);
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

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		Bitmap tile = BitmapFactory.decodeResource(getResources(), R.drawable.action_bar_background);		
		BitmapDrawable background = new BitmapDrawable(getResources(), tile);
		background.setTileModeX(android.graphics.Shader.TileMode.REPEAT);
		actionBar.setBackgroundDrawable(background);		

		currentFragment = PreferencesUtils.getCurrentFragment(this);

		homeFragment = new HomeFragment();
		setupFragment1 = new SetupFragment();
		((SetupFragment)setupFragment1).setBuoyNumber(1);
		setupFragment2 = new SetupFragment();
		((SetupFragment)setupFragment2).setBuoyNumber(2);
		lineCrossFragment = new LineCrossingsFragment();
		mapFragment = new MapFragment();

		// Create the adapter that will return a fragment for each of the 
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

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

	}



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




}