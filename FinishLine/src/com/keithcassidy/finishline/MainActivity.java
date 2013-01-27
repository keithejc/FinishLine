package com.keithcassidy.finishline;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.keithcassidy.finishline.FinishLineService.LocalBinder;

public class MainActivity extends android.support.v4.app.FragmentActivity 
{
	private static final String TAG = MainActivity.class.getSimpleName(); 
    private GoogleMap raceMap;

    FinishLineService mService;
    boolean mBound = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		ApiAdapterFactory.getApiAdapter().hideTitle(this);
		setContentView(R.layout.activity_map);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(onBroadcastServiceStatusReceived,
			      new IntentFilter(Constants.SERVICE_STATUS_MESSAGE));

		setUpMapIfNeeded();
		
		//make sure service is started so we can bind to it and start races
        startService();
	}

	@Override
    protected void onStart() 
	{
        super.onStart();
        // Bind to FinishLineService so we can call it directly
        Intent intent = new Intent(this, FinishLineService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
    }
	
    @Override
    protected void onStop() 
    {
        super.onStop();
        
        if( mService != null )
        {
        	if( !mService.isRacing())
        	{
        		stopService();
        	}
        }
        
        // Unbind from the service
        if (mBound) 
        {
            unbindService(mConnection);
            mBound = false;
        }
    }
	
	@Override
	protected void onDestroy() 
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(onBroadcastServiceStatusReceived);

		super.onDestroy();
	}
	
	private BroadcastReceiver onBroadcastServiceStatusReceived = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{			
			// Get extra data included in the Intent
			String message = intent.getStringExtra("message");
			Log.d(TAG, "Got message: " + message);
		}
	};	

	public  void playAlertTone(final Context context)
	{
		Thread t = new Thread()
		{
			public void run()
			{
				MediaPlayer player = null;
				int countBeep = 0;
				while(countBeep<2){
					player = MediaPlayer.create(context,R.raw.beephigh);
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

	
    private void setupButtons() 
    {
    	final Button startStop = (Button)findViewById(R.id.buttonStart);
		
		if( startStop != null)
		{
			if( isServiceRunning() )
			{
				if( mService.isRacing())
				{
					startStop.setText(R.string.stop);
					startStop.setOnClickListener(new Button.OnClickListener() 
					{  
						public void onClick(View v)
						{
							stopRace();
							setupButtons();
						}
					});				
				}
				else
				{
					startStop.setText(R.string.start);

					startStop.setOnClickListener(new Button.OnClickListener() 
					{  
						public void onClick(View v)
						{
							startRace();
							setupButtons();
						}
					});				

				}
				
			}
			else
			{
				startStop.setText(R.string.start);

				startStop.setOnClickListener(new Button.OnClickListener() 
				{  
					public void onClick(View v)
					{
						startRace();
						setupButtons();
					}
				});				

			}
		}		
	}

	private void startRace() 
	{
		mService.startNewRace();		
	}
    
	@Override
    protected void onResume() 
	{
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() 
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (raceMap == null) 
        {
            // Try to obtain the map from the SupportMapFragment.
        	raceMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (raceMap != null) 
            {
                setUpMap();
            }
        }
    }
    
    private void setUpMap() 
    {
        raceMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        raceMap.setMyLocationEnabled(true);
    }    
    
    private void startService()
    {
    	Intent startIntent = new Intent(this, FinishLineService.class);
    	this.startService(startIntent);
    }
    
    private void stopRace()
    {
    	mService.endCurrentRace();        
    }
    
    private ServiceConnection mConnection = new ServiceConnection() 
    {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            
            setupButtons();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };    
    
    private void stopService()
    {
    	Intent stopIntent = new Intent(this, FinishLineService.class);
    	this.stopService(stopIntent);
    }
    
    private boolean isServiceRunning() 
    {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
        {
        	
        	String name = service.service.getClassName();
			Log.d(TAG, "Running service: " + name );
            if ("com.keithcassidy.finishline.FinishLineService".equals(name)) 
            {
                return true;
            }
        }
        return false;
    }   
    
   

}
