package com.keithcassidy.finishline.test;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;
import com.keithcassidy.finishline.Buoy;
import com.keithcassidy.finishline.FinishLineDataInterface;
import com.keithcassidy.finishline.LineCrossHandler;

import android.location.Location;
import android.test.AndroidTestCase;

public class LineCrossTest extends AndroidTestCase 
{

	public void testLineCross()
	{
		FinishLineDataInterface finishLineDataStorage = new MockFinishLineDataStorage();
		Buoy buoy1 = new Buoy();
		buoy1.Name = "buoy1";
		buoy1.Position = new LatLng(1, -10);

		Buoy buoy2 = new Buoy();
		buoy2.Name = "buoy2";
		buoy2.Position = new LatLng(1, 10);
		
		LineCrossHandler ca = new LineCrossHandler();
		ca.setBouys(buoy1, buoy2);
		
		ca.setContext(null);
		ca.setFinishLineDataStorage(finishLineDataStorage);
		ca.setMaxAccuracyAllowed(10);
		ca.setFinishLineExtension(0);
		ca.setRacing(true);
		
		
		Location loc1 = new Location("");
		loc1.setLatitude(0);
		loc1.setLongitude(0);
		loc1.setBearing(0);
		ca.handleLocationData(loc1);
		
		Location loc2 = new Location("");
		loc2.setLatitude(2);
		loc2.setLongitude(0);
		loc2.setBearing(0);
		ca.handleLocationData(loc2);

		ArrayList<Location> crossings = ((MockFinishLineDataStorage)finishLineDataStorage).getCrossings();
		assertEquals(1, crossings.size());
		assertEquals(1.0154256, crossings.get(0).getLatitude(), 0.000001 );
		assertEquals(0.0, crossings.get(0).getLongitude() );
		
	}
	
	public void testLineNotCross()
	{
		FinishLineDataInterface finishLineDataStorage = new MockFinishLineDataStorage();
		Buoy buoy1 = new Buoy();
		buoy1.Name = "buoy1";
		buoy1.Position = new LatLng(1, -10);

		Buoy buoy2 = new Buoy();
		buoy2.Name = "buoy2";
		buoy2.Position = new LatLng(1, 10);
		
		LineCrossHandler ca = new LineCrossHandler();
		ca.setBouys(buoy1, buoy2);
		
		ca.setContext(null);
		ca.setFinishLineDataStorage(finishLineDataStorage);
		ca.setMaxAccuracyAllowed(10);
		ca.setFinishLineExtension(0);
		ca.setRacing(true);
		
		
		Location loc1 = new Location("");
		loc1.setLatitude(0);
		loc1.setLongitude(0);
		loc1.setBearing(0);
		ca.handleLocationData(loc1);
		
		Location loc2 = new Location("");
		loc2.setLatitude(0.1);
		loc2.setLongitude(0);
		loc2.setBearing(0);
		ca.handleLocationData(loc2);

		
		loc2.setLatitude(0.5);
		loc2.setLongitude(1);
		loc2.setBearing(0);
		ca.handleLocationData(loc2);
		
		loc2.setLatitude(0);
		loc2.setLongitude(0);
		loc2.setBearing(0);
		ca.handleLocationData(loc2);

		loc2.setLatitude(0.5);
		loc2.setLongitude(1);
		loc2.setBearing(0);
		ca.handleLocationData(loc2);
		
		ArrayList<Location> crossings = ((MockFinishLineDataStorage)finishLineDataStorage).getCrossings();
		assertEquals(0, crossings.size());
		//assertEquals(1.0154256, crossings.get(0).getLatitude(), 0.000001 );
		//assertEquals(0.0, crossings.get(0).getLongitude() );
		
	}
		
}
