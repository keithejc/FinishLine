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
package com.keithcassidy.finishline.test;

import com.google.android.gms.maps.model.LatLng;
import com.keithcassidy.finishline.Buoy;
import com.keithcassidy.finishline.DistanceIntersection;
import com.keithcassidy.finishline.LocationUtils;

import android.location.Location;
import android.test.AndroidTestCase;

public class LocationUtilsTests extends AndroidTestCase 
{

	public LocationUtilsTests()
	{
		super();
		
	}
	
	public void testInterpolationSimple()
	{
		Location l1 = new Location("");
		l1.setTime(0);
		l1.setLatitude(0);
		l1.setLongitude(0);
		
		Location l2 = new Location("");
		l2.setTime(10);
		l2.setLatitude(10);
		l2.setLongitude(10);
		
		LatLng l3 = LocationUtils.getIntermediatePosition(1, l1, l2);
		
		assertEquals(1.0, l3.latitude);
		assertEquals(1.0, l3.longitude);
		
	}

	public void testInterpolation()
	{
		Location l1 = new Location("");
		l1.setTime(0);
		l1.setLatitude(1);
		l1.setLongitude(1);
		
		Location l2 = new Location("");
		l2.setTime(10);
		l2.setLatitude(-9);
		l2.setLongitude(-9);
		
		LatLng l3 = LocationUtils.getIntermediatePosition(1, l1, l2);
		
		assertEquals(0.0, l3.latitude);
		assertEquals(0.0, l3.longitude);
		
	}
	
	public void testOffsets()
	{
		LatLng l1 = new LatLng(0, 0);
		LatLng l2 = LocationUtils.adjustPositionByOffsets(l1, 0, 0, 0);
		
		assertEquals(0.0, l2.latitude);
		assertEquals(0.0, l2.longitude);
		
				
		l1 = new LatLng(0, 0);
		l2 = LocationUtils.adjustPositionByOffsets(l1, 0, 1000, 90);
		assertEquals(-0.008991D, l2.latitude, 0.000001);
		assertEquals(0.0, l2.longitude, 0.000001);		

		l1 = new LatLng(0, 0);
		l2 = LocationUtils.adjustPositionByOffsets(l1, 0, 1000, 270);
		assertEquals(0.008991D, l2.latitude, 0.000001);
		assertEquals(0.0, l2.longitude);		
		
		l1 = new LatLng(0, 0);
		l2 = LocationUtils.adjustPositionByOffsets(l1, 0, 1000, 0);
		assertEquals(0.0, l2.latitude, 0.000001);
		assertEquals(0.008991D, l2.longitude, 0.000001);		

		l1 = new LatLng(0, 0);
		l2 = LocationUtils.adjustPositionByOffsets(l1, 1000, 1000, 0);
		assertEquals(0.008991D, l2.latitude, 0.000001);
		assertEquals(0.008991D, l2.longitude, 0.000001);		
		
		l1 = new LatLng(0, 0);
		l2 = LocationUtils.adjustPositionByOffsets(l1, -1000, -1000, 0);
		assertEquals(-0.008991D, l2.latitude, 0.000001);
		assertEquals(-0.008991D, l2.longitude, 0.000001);		
	}
	
	public void testDistanceFromPointSimple()
	{
		Location start = new Location("na");
		start.setLatitude(0);
		start.setLongitude(0);
		start.setBearing(90);
		Location end = LocationUtils.getPointFromDistanceAndBearing(start, 1000);
		assertEquals(0.008991D, end.getLongitude(), 0.000001);
		assertEquals(0D, end.getLatitude(), 0.000001);
		
		start.setLatitude(0);
		start.setLongitude(0);
		start.setBearing(45);
		end = LocationUtils.getPointFromDistanceAndBearing(start, 1000);
		assertEquals(0.006357D, end.getLongitude(), 0.000001);
		assertEquals(0.006357D, end.getLatitude(), 0.000001);
		
	}

	//test a location created at a distance and bearing from a start point
	public void testDistanceFromPointCrossMeridian()
	{
		Location start = new Location("na");
		start.setLatitude(0);
		start.setLongitude(-0.0001);
		start.setBearing(90);
		Location end = LocationUtils.getPointFromDistanceAndBearing(start, 1000);
		assertEquals(0.0, end.getLatitude(), 0.000001);
		assertEquals(0.008891, end.getLongitude(), 0.000001);
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
		
		assertEquals(0D, intersection.getLatitude(), 0.000001);
		assertEquals(0D, intersection.getLongitude(), 0.000001);

		
		
		path1.setLatitude(50);
		path1.setLongitude(-1);
		path1.setBearing(90);
		
		path2.setLatitude(-1);
		path2.setLongitude(0);
		path2.setBearing(0);
		
		intersection = LocationUtils.intersectionOfTwoPaths(path1, path2);
		assertTrue(intersection != null);
		
		assertEquals(49.995703D, intersection.getLatitude(), 0.000001);
		assertEquals(0D, intersection.getLongitude(), 0.000001);
		
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
		
		DistanceIntersection di = LocationUtils.distanceToLine(boat, buoy1, buoy2, 0);
		
		assertEquals(110591.234F, di.distance);
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
		
		DistanceIntersection di = LocationUtils.distanceToLine(boat, buoy1, buoy2, 0);
		
		assertEquals(Float.POSITIVE_INFINITY, di.distance);
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
		
		DistanceIntersection di = LocationUtils.distanceToLine(boat, buoy1, buoy2, 0);
		
		assertEquals(Float.POSITIVE_INFINITY, di.distance);
	}

	
	public void testDistanceToFinishExtendPlus()
	{
		Buoy buoy1 = new Buoy();
		buoy1.Position = new LatLng(1, -1);
		
		Buoy buoy2 = new Buoy();
		buoy2.Position = new LatLng(1, 1);
		
		Location boat = new Location("na");
		boat.setLatitude(0);
		boat.setLongitude(1.1);
		boat.setBearing(0);
		
		DistanceIntersection di = LocationUtils.distanceToLine(boat, buoy1, buoy2, 100000);
		
		assertEquals(110570.836F, di.distance);
	}

	public void testDistanceToFinishExtendMinus()
	{
		Buoy buoy1 = new Buoy();
		buoy1.Position = new LatLng(1, -1);
		
		Buoy buoy2 = new Buoy();
		buoy2.Position = new LatLng(1, 1);
		
		Location boat = new Location("na");
		boat.setLatitude(0);
		boat.setLongitude(-1.1);
		boat.setBearing(0);
		
		DistanceIntersection di = LocationUtils.distanceToLine(boat, buoy1, buoy2, 100000);
		
		assertEquals(110570.84F, di.distance);
	}
	
	public void testParsing()
	{
		assertEquals(51.645011, LocationUtils.parseDMS("51:38:42.0396"));
		
		assertEquals(-1.0, LocationUtils.parseDMS("1 0 0W"));
		assertEquals(-1.0, LocationUtils.parseDMS("1  0 0W"));
		assertEquals(-1.0, LocationUtils.parseDMS("1  0  0W"));
		assertEquals(-1.0, LocationUtils.parseDMS("1 0  0W"));
		assertEquals(1.0, LocationUtils.parseDMS("1 0 0E"));
		assertEquals(-1.0, LocationUtils.parseDMS("-1 0 0"));
		
		assertEquals(-1.508333, LocationUtils.parseDMS("1 30.5W"), 0.000001);
		
		assertEquals(49.995703, LocationUtils.parseDMS("49° 59' 44.5302\""), 0.000001);
		
		assertEquals(50.8010, LocationUtils.parseDMS("	N50° 48.06'"), 0.000001);
		
		assertEquals(-0.106667, LocationUtils.parseDMS("W00° 06.40'"), 0.000001);
		
		assertEquals(-1.5, LocationUtils.parseDMS("1 30 0w"));
		assertEquals(-1.5, LocationUtils.parseDMS("1° 30' 0\"w"));
		assertEquals(-1.0, LocationUtils.parseDMS("1 0 0S"));
		assertEquals(1.008333, LocationUtils.parseDMS("1 0 30"), 0.000001);
		assertEquals(1.008333, LocationUtils.parseDMS("1.008333"), 0.000001);
		assertEquals(-1.008333, LocationUtils.parseDMS("1.008333S"), 0.000001);
		assertEquals(1.008333, LocationUtils.parseDMS("1.008333E"), 0.000001);
		assertEquals(-1.008333, LocationUtils.parseDMS("1.008333W"), 0.000001);
		assertEquals(-1.008333, LocationUtils.parseDMS("1.008333w"), 0.000001);
		assertEquals(1.008333, LocationUtils.parseDMS("1.008333N"), 0.000001);
		assertEquals(-1.0, LocationUtils.parseDMS("w1 0 0"));
		assertEquals(1.0, LocationUtils.parseDMS("e1 0 0"));
		assertEquals(-1.0, LocationUtils.parseDMS("-1 0 0"));
		assertEquals(-1.5, LocationUtils.parseDMS("w1 30 0"));
		assertEquals(-1.0, LocationUtils.parseDMS("s1 0 0"));
		assertEquals(1.008333, LocationUtils.parseDMS("1 0 30"), 0.000001);
		assertEquals(1.008333, LocationUtils.parseDMS("1.008333"), 0.000001);
		assertEquals(-1.008333, LocationUtils.parseDMS("S1.008333"), 0.000001);
		assertEquals(1.008333, LocationUtils.parseDMS("E1.008333"), 0.000001);
		assertEquals(-1.008333, LocationUtils.parseDMS("W1.008333"), 0.000001);
		assertEquals(1.008333, LocationUtils.parseDMS("N1.008333"), 0.000001);
		
		assertEquals(50.790183, LocationUtils.parseDMS("N50:47.411"), 0.000001);
		assertEquals(-0.112183, LocationUtils.parseDMS("W00:06.731"), 0.000001);
	}
	
}
