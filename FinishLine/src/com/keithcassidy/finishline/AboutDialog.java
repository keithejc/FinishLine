package com.keithcassidy.finishline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;

public class AboutDialog extends DialogFragment 
{


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
        
		TextView tv = (TextView)v.findViewById(R.id.legal_text);
		tv.setText(readRawTextFile(R.raw.legal));
		
		tv = (TextView)v.findViewById(R.id.info_text);
		
		String version = "";
		try 
		{
			version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String info = readRawTextFile(R.raw.info);
		String infoReady = info.replace("VVV", version);
		tv.setText(Html.fromHtml(infoReady));
		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);

    	getDialog().setCanceledOnTouchOutside(true);
		
        return v;
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

