package com.keithcassidy.finishline.test;

import com.google.android.gms.maps.model.LatLng;
import com.keithcassidy.finishline.Buoy;
import com.keithcassidy.finishline.LocationUtils;

import android.location.Location;
import android.test.AndroidTestCase;

public class LocationUtilsTests extends AndroidTestCase 
{

	public LocationUtilsTests()
	{
		super();
		
	}
	
	public void testDistanceFromPointSimple()
	{
		Location start = new Location("na");
		start.setLatitude(0);
		start.setLongitude(0);
		start.setBearing(90);
		Location end = LocationUtils.getPointFromDistanceAndBearing(start, 1000);
		assertEquals(0.008991D, end.getLongitude());
		assertEquals(0D, end.getLatitude());
		
		start.setLatitude(0);
		start.setLongitude(0);
		start.setBearing(45);
		end = LocationUtils.getPointFromDistanceAndBearing(start, 1000);
		assertEquals(0.006357D, end.getLongitude());
		assertEquals(0.006357D, end.getLatitude());
		
	}

	//test a location created at a distance and bearing from a start point
	public void testDistanceFromPointCrossMeridian()
	{
		Location start = new Location("na");
		start.setLatitude(0);
		start.setLongitude(-0.0001);
		start.setBearing(90);
		Location end = LocationUtils.getPointFromDistanceAndBearing(start, 1000);
		assertEquals(0.0, end.getLatitude());
		assertEquals(0.008891, end.getLongitude());
	}
	
	//test a location created at a distance and bearing from a start point
	public void TestDistanceFromPointCrossDateLineWE()
	{
		Location start = new Location("na");
		start.setLatitude(1D);
		start.setLongitude(-179);
		start.setBearing(230F);
		Location end = LocationUtils.getPointFromDistanceAndBearing(start, 500000);
		assertEquals(-1.889615, end.getLatitude());
		assertEquals(177.555957, end.getLongitude());
	}
	public void TestDistanceFromPointCrossDateLineEW()
	{
		Location start = new Location("na");
		start.setLatitude(-1);
		start.setLongitude(179);
		start.setBearing(45F);
		Location end = LocationUtils.getPointFromDistanceAndBearing(start, 500000);
		assertEquals(2.178593, end.getLatitude());
		assertEquals(-177.820647, end.getLongitude());
	}
	
	public void testIntersectionOfPathsSimple()
	{
		Location path1 = new Location("na");
		path1.setLatitude(0);
		path1.setLongitude(0);
		path1.setBearing(0);
		
		Location path2 = new Location("na");
		path2.setLatitude(0);
		path2.setLongitude(-1);
		path2.setBearing(90);
		
		Location intersection = LocationUtils.intersectionOfTwoPaths(path1, path2);
		assertTrue(intersection != null);
		
		assertEquals(0D, intersection.getLatitude());
		assertEquals(0D, intersection.getLongitude());

		
		path1.setLatitude(-1);
		path1.setLongitude(0);
		path1.setBearing(0);
		
		path2.setLatitude(0);
		path2.setLongitude(-1);
		path2.setBearing(90);
		
		intersection = LocationUtils.intersectionOfTwoPaths(path1, path2);
		assertTrue(intersection != null);
		
		assertEquals(0D, intersection.getLatitude());
		assertEquals(0D, intersection.getLongitude());

		
		
		path1.setLatitude(50);
		path1.setLongitude(-1);
		path1.setBearing(90);
		
		path2.setLatitude(-1);
		path2.setLongitude(0);
		path2.setBearing(0);
		
		intersection = LocationUtils.intersectionOfTwoPaths(path1, path2);
		assertTrue(intersection != null);
		
		assertEquals(49.995703D, intersection.getLatitude());
		assertEquals(0D, intersection.getLongitude());
		
	}
	
	public void testIntersectionOfPathsFail()
	{
		Location path1 = new Location("na");
		path1.setLatitude(1);
		path1.setLongitude(0);
		path1.setBearing(90);
		
		Location path2 = new Location("na");
		path2.setLatitude(-1);
		path2.setLongitude(0);
		path2.setBearing(270);
		
		Location intersection = LocationUtils.intersectionOfTwoPaths(path1, path2);
		assertTrue(intersection == null);
	}	
	
	public void testDistanceToFinishSimple()
	{
		Buoy buoy1 = new Buoy();
		buoy1.Position = new LatLng(1, -1);
		
		Buoy buoy2 = new Buoy();
		buoy2.Position = new LatLng(1, 1);
		
		Location boat = new Location("na");
		boat.setLatitude(0);
		boat.setLongitude(0);
		boat.setBearing(0);
		
		float distance = LocationUtils.distanceToFinish(boat, buoy1, buoy2);
		
		assertEquals(110591.195F, distance);
	}

	public void testDistanceToFinishWrongDirection()
	{
		Buoy buoy1 = new Buoy();
		buoy1.Position = new LatLng(1, -1);
		
		Buoy buoy2 = new Buoy();
		buoy2.Position = new LatLng(1, 1);
		
		Location boat = new Location("na");
		boat.setLatitude(0);
		boat.setLongitude(0);
		boat.setBearing(180);
		
		float distance = LocationUtils.distanceToFinish(boat, buoy1, buoy2);
		
		assertEquals(LocationUtils.EARTH_CIRCUMFERENCE, distance);
	}

	public void testDistanceToFinishMiss()
	{
		Buoy buoy1 = new Buoy();
		buoy1.Position = new LatLng(1, -1);
		
		Buoy buoy2 = new Buoy();
		buoy2.Position = new LatLng(1, 1);
		
		Location boat = new Location("na");
		boat.setLatitude(0);
		boat.setLongitude(2);
		boat.setBearing(0);
		
		float distance = LocationUtils.distanceToFinish(boat, buoy1, buoy2);
		
		assertEquals(LocationUtils.EARTH_CIRCUMFERENCE, distance);
	}
	
	public void testParsing()
	{
		assertEquals(-1.0, LocationUtils.parseDMS("1 0 0W"));
		assertEquals(1.0, LocationUtils.parseDMS("1 0 0E"));
		assertEquals(-1.0, LocationUtils.parseDMS("-1 0 0"));
		
		assertEquals(-1.508333, LocationUtils.parseDMS("1 30.5W"));
		
		assertEquals(49.995703, LocationUtils.parseDMS("49° 59' 44.5302\""));
		
		assertEquals(50.8010, LocationUtils.parseDMS("	N50° 48.06'"));
		
		assertEquals(-0.106667, LocationUtils.parseDMS("W00° 06.40'"));
		
		assertEquals(-1.5, LocationUtils.parseDMS("1 30 0w"));
		assertEquals(-1.5, LocationUtils.parseDMS("1° 30' 0\"w"));
		assertEquals(-1.0, LocationUtils.parseDMS("1 0 0S"));
		assertEquals(1.008333, LocationUtils.parseDMS("1 0 30"));
		assertEquals(1.008333, LocationUtils.parseDMS("1.008333"));
		assertEquals(-1.008333, LocationUtils.parseDMS("1.008333S"));
		assertEquals(1.008333, LocationUtils.parseDMS("1.008333E"));
		assertEquals(-1.008333, LocationUtils.parseDMS("1.008333W"));
		assertEquals(-1.008333, LocationUtils.parseDMS("1.008333w"));
		assertEquals(1.008333, LocationUtils.parseDMS("1.008333N"));
		assertEquals(-1.0, LocationUtils.parseDMS("w1 0 0"));
		assertEquals(1.0, LocationUtils.parseDMS("e1 0 0"));
		assertEquals(-1.0, LocationUtils.parseDMS("-1 0 0"));
		assertEquals(-1.5, LocationUtils.parseDMS("w1 30 0"));
		assertEquals(-1.0, LocationUtils.parseDMS("s1 0 0"));
		assertEquals(1.008333, LocationUtils.parseDMS("1 0 30"));
		assertEquals(1.008333, LocationUtils.parseDMS("1.008333"));
		assertEquals(-1.008333, LocationUtils.parseDMS("S1.008333"));
		assertEquals(1.008333, LocationUtils.parseDMS("E1.008333"));
		assertEquals(-1.008333, LocationUtils.parseDMS("W1.008333"));
		assertEquals(1.008333, LocationUtils.parseDMS("N1.008333"));
		
	}
	
}
