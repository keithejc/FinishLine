package com.keithcassidy.finishline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;

public class SimpleEula {

	private Activity mActivity; 

	public SimpleEula(Activity context) {
		mActivity = context; 
	}

	public String readRawTextFile(int id) 
	{
		InputStream inputStream = mActivity.getResources().openRawResource(id);
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
	
     public void show() 
     {

        boolean hasBeenShown = PreferencesUtils.hasEulaBeenShown(mActivity);
        if(hasBeenShown == false)
        {

        	// Show the Eula
            String title = mActivity.getString(R.string.app_name);
            
            //Includes the updates as well so users know what changed. 
            String message = mActivity.getString(R.string.eula);

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.agree, new Dialog.OnClickListener() 
                    {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) 
                        {
                            // Mark this eula as read.
                        	PreferencesUtils.setEulaShown(mActivity);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							// Close the activity as they have declined the EULA
							mActivity.finish(); 
						}
                    	
                    });
            builder.setOnKeyListener(new OnKeyListener() 
            { 
            	@Override 
            	public boolean onKey(DialogInterface dialoginterface, int keyCode, KeyEvent event) 
            	{
            		if ((keyCode == KeyEvent.KEYCODE_HOME)) 
            		{   
            			return false;  
            		} 
            		else 
            		{   
            			return true;  
            		} 
            	}

            });
            
            builder.setCancelable(false);
            builder.create().show();
        }
    }

}