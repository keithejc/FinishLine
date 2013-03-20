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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;

public class AboutDialog extends DialogFragment 
{


	private static final String TAG = "AboutDialog";


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL + DialogFragment.STYLE_NO_TITLE, R.style.Theme_Sherlock_DialogWithCorners);
    }
	
    static AboutDialog newInstance() 
    {
    	AboutDialog f = new AboutDialog();
        return f;
    }
	
	
	

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
    {
        View v = inflater.inflate(R.layout.about, container, false);
        
		TextView tvLegal = (TextView)v.findViewById(R.id.legal_text);
		htmlIfy(tvLegal, readRawTextFile(R.raw.legal));
		
		TextView tvInfo = (TextView)v.findViewById(R.id.info_text);
		String version = "";
		try 
		{
			version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e) 
		{
			Log.e(TAG, "Error in onCreateView: " + e);
		}
		String info = readRawTextFile(R.raw.info);
		String infoWithVersion = info.replace("VVV", version);
		htmlIfy(tvInfo, infoWithVersion);
		
    	getDialog().setCanceledOnTouchOutside(true);
		
        return v;
    }	
    
    void htmlIfy(TextView tv, String text)
    {
		tv.setText(Html.fromHtml(text));
		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);
    }
    
	
	public String readRawTextFile(int id) 
	{
		InputStream inputStream = getActivity().getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		StringBuilder text = new StringBuilder();
		try 
		{
			while (( line = buf.readLine()) != null) text.append(line);
		}
		catch (IOException e) 
		{
			return null;
		}
		return text.toString();
	}
}

