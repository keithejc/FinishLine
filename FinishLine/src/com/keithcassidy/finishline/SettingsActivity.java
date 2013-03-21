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

import com.google.analytics.tracking.android.EasyTracker;

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
	
	@Override
	protected void onStart() 
	{
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	
}
