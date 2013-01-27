package com.keithcassidy.finishline;

import java.util.List;
import android.location.Location;

public class Race 
{
	private List<Location> locationHistory;
	private List<Location> lineCrossings;
	private Buoy buoy1;
	private Buoy buoy2;
	private long StopTime;
	private long Id;
	
	public List<Location> getLocationHistory() {
		return locationHistory;
	}
	public void setLocationHistory(List<Location> locationHistory) {
		this.locationHistory = locationHistory;
	}
	public List<Location> getLineCrossings() {
		return lineCrossings;
	}
	public void setLineCrossings(List<Location> lineCrossings) {
		this.lineCrossings = lineCrossings;
	}
	public Buoy getBuoy1() {
		return buoy1;
	}
	public void setBuoy1(Buoy buoy1) {
		this.buoy1 = buoy1;
	}
	public Buoy getBuoy2() {
		return buoy2;
	}
	public void setBuoy2(Buoy buoy2) {
		this.buoy2 = buoy2;
	}
	public long getStopTime() {
		return StopTime;
	}
	public void setStopTime(long stopTime) {
		StopTime = stopTime;
	}
	public long getId() {
		return Id;
	}
	public void setId(long id) {
		Id = id;
	}

}
