package com.keithcassidy.finishline;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;



public class SettingsActivity extends PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(Constants.SETTINGS_NAME);
		prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);

		addPreferencesFromResource(R.xml.preferences);
	}
}
