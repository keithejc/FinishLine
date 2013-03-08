package com.keithcassidy.finishline.test;

import java.util.ArrayList;

import android.location.Location;

import com.keithcassidy.finishline.FinishLineDataInterface;

public class MockFinishLineDataStorage implements FinishLineDataInterface 
{
	private ArrayList<Location> crossings;
	public ArrayList<Location> getCrossings() {
		return crossings;
	}


	public MockFinishLineDataStorage()
	{
		crossings = new ArrayList<Location>(); 
	}

	@Override
	public long addCrossing(Location location) 
	{
		crossings.add(location);
		return crossings.size();
	}
}
