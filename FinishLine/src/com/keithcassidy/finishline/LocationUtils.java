package com.keithcassidy.finishline;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import android.location.Location;
import android.util.Log;

public class LocationUtils 
{

	private final static double EPSILON = 0.000001;
	public final static double MAX_LATLNG_PRECISION_DPLACES = 6;
	public final static double EARTH_RADIUS =   6372797.6D;
	public final static float EARTH_CIRCUMFERENCE = 10010366.12F * 2F;
	private LocationUtils() {}

	/**
	 * Computes the distance on the two sphere between the point c0 and the line
	 * segment c1 to c2.
	 * 
	 * @param point the first coordinate
	 * @param lineStart the beginning of the line segment
	 * @param lineEnd the end of the lone segment
	 * @return the distance in m (assuming spherical earth)
	 */
	private static double distanceBetweenPointAndLine(final Location point, final Location lineStart, final Location lineEnd) 
	{
		if (lineStart.equals(lineEnd)) 
		{
			return lineEnd.distanceTo(point);
		}

		final double s0lat = point.getLatitude() * UnitConversions.DEG_TO_RAD;
		final double s0lng = point.getLongitude() * UnitConversions.DEG_TO_RAD;
		final double s1lat = lineStart.getLatitude() * UnitConversions.DEG_TO_RAD;
		final double s1lng = lineStart.getLongitude() * UnitConversions.DEG_TO_RAD;
		final double s2lat = lineEnd.getLatitude() * UnitConversions.DEG_TO_RAD;
		final double s2lng = lineEnd.getLongitude() * UnitConversions.DEG_TO_RAD;

		double s2s1lat = s2lat - s1lat;
		double s2s1lng = s2lng - s1lng;
		final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
				/ (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
		if (u <= 0) {
			return point.distanceTo(lineStart);
		}
		if (u >= 1) {
			return point.distanceTo(lineEnd);
		}
		Location sa = new Location("");
		sa.setLatitude(point.getLatitude() - lineStart.getLatitude());
		sa.setLongitude(point.getLongitude() - lineStart.getLongitude());
		Location sb = new Location("");
		sb.setLatitude(u * (lineEnd.getLatitude() - lineStart.getLatitude()));
		sb.setLongitude(u * (lineEnd.getLongitude() - lineStart.getLongitude()));
		return sa.distanceTo(sb);
	}

	static double roundLatOrLong(double latLng)
	{
		BigDecimal bd = new BigDecimal(latLng);
		BigDecimal rounded = bd.setScale((int) MAX_LATLNG_PRECISION_DPLACES, BigDecimal.ROUND_HALF_UP);
		return rounded.doubleValue();	  
	}

	/**
	 * Decimates the given locations for a given zoom level. This uses a
	 * Douglas-Peucker decimation algorithm.
	 * 
	 * @param tolerance in meters
	 * @param locations input
	 * @param decimated output
	 */
	public static void decimateTrack( double tolerance, ArrayList<Location> locations, ArrayList<Location> decimated)
	{
		final int n = locations.size();
		if (n < 1) {
			return;
		}
		int idx;
		int maxIdx = 0;
		Stack<int[]> stack = new Stack<int[]>();
		double[] dists = new double[n];
		dists[0] = 1;
		dists[n - 1] = 1;
		double maxDist;
		double dist = 0.0;
		int[] current;

		if (n > 2) {
			int[] stackVal = new int[] { 0, (n - 1) };
			stack.push(stackVal);
			while (stack.size() > 0) {
				current = stack.pop();
				maxDist = 0;
				for (idx = current[0] + 1; idx < current[1]; ++idx) {
					dist = LocationUtils.distanceBetweenPointAndLine(
							locations.get(idx), locations.get(current[0]), locations.get(current[1]));
					if (dist > maxDist) {
						maxDist = dist;
						maxIdx = idx;
					}
				}
				if (maxDist > tolerance) {
					dists[maxIdx] = maxDist;
					int[] stackValCurMax = { current[0], maxIdx };
					stack.push(stackValCurMax);
					int[] stackValMaxCur = { maxIdx, current[1] };
					stack.push(stackValMaxCur);
				}
			}
		}

		int i = 0;
		idx = 0;
		decimated.clear();
		for (Location l : locations) {
			if (dists[idx] != 0) {
				decimated.add(l);
				i++;
			}
			idx++;
		}
		Log.d(Constants.TAG, "Decimating " + n + " points to " + i + " w/ tolerance = " + tolerance);
	}


	/**
	 * Checks if a given location is a valid (i.e. physically possible) location
	 * on Earth. Note: The special separator locations (which have latitude = 100)
	 * will not qualify as valid. Neither will locations with lat=0 and lng=0 as
	 * these are most likely "bad" measurements which often cause trouble.
	 * 
	 * @param location the location to test
	 * @return true if the location is a valid location.
	 */
	public static boolean isValidLocation(Location location) 
	{
		return location != null && Math.abs(location.getLatitude()) <= 90
				&& Math.abs(location.getLongitude()) <= 180;
	}

	//http://www.geomidpoint.com/destination/calculation.html
	//assumes spherical earth
	public static Location getPointFromDistanceAndBearing(Location startLoc, double distance) 
	{ 
		Location newLocation = new Location("newLocation");

		double bearing = Math.toRadians(startLoc.getBearing());
		double long1 = Math.toRadians( startLoc.getLongitude() );
		double lat1 = Math.toRadians( startLoc.getLatitude() );

		double dR = distance/EARTH_RADIUS;
		double cosDr = Math.cos(dR);
		double sinDr = Math.sin(dR);
		double cosLat1 = Math.cos(lat1);
		double sinLat1 = Math.sin(lat1);

		double lat2 = Math.asin( sinLat1 * cosDr + cosLat1 * sinDr * Math.cos(bearing));

		double long2 = Math.toDegrees(long1 + Math.atan2(Math.sin(bearing)* sinDr * cosLat1, 
				cosDr - sinLat1 * Math.sin(lat2)));	      

		if( long2 < -180 )
		{
			long2 += 360;
		}	      
		else if( long2 > 180 )
		{
			long2 -= 360;
		}

		newLocation.setLongitude(roundLatOrLong(long2));
		newLocation.setLatitude(roundLatOrLong(Math.toDegrees(lat2)));


		return newLocation;
	};

	/**
	 * Returns the point of intersection of two paths defined by point and bearing
	 *
	 *   see http://williams.best.vwh.net/avform.htm#Intersection
	 *
	 * @param   {LatLon} p1: First point
	 * @param   {Number} brng1: Initial bearing from first point
	 * @param   {LatLon} p2: Second point
	 * @param   {Number} brng2: Initial bearing from second point
	 * @returns {Location} Destination point (null if no unique intersection defined)
	 */
	public static Location intersectionOfTwoPaths(Location p1, Location p2) 
	{

		Float brng1 = p1.getBearing();
		Float brng2 = p2.getBearing();

		Double lat1 = Math.toRadians( p1.getLatitude() );
		Double lon1 = Math.toRadians( p1.getLongitude() );

		Double lat2 = Math.toRadians( p2.getLatitude() );
		Double lon2 = Math.toRadians( p2.getLongitude() );

		Double brng13 = Math.toRadians( brng1);
		Double brng23 = Math.toRadians( brng2);
		Double dLat = lat2-lat1;
		Double dLon = lon2-lon1;

		Double dist12 = 2*Math.asin( Math.sqrt( Math.sin(dLat/2)*Math.sin(dLat/2) + 
				Math.cos(lat1)*Math.cos(lat2)*Math.sin(dLon/2)*Math.sin(dLon/2) ) );
		if (dist12 == 0) 
		{
			return null;
		}

		// initial/final bearings between points
		Double brngA = Math.acos( ( Math.sin(lat2) - Math.sin(lat1)*Math.cos(dist12) ) / 
				( Math.sin(dist12)*Math.cos(lat1) ) );

		if (Double.isNaN(brngA)) 
		{
			brngA = (double) 0;  // protect against rounding
		}

		Double brngB = Math.acos( ( Math.sin(lat1) - Math.sin(lat2)*Math.cos(dist12) ) / 
				( Math.sin(dist12)*Math.cos(lat2) ) );

		Double brng12;
		Double brng21;	  
		if (Math.sin(lon2-lon1) > 0) 
		{
			brng12 = brngA;
			brng21 = 2*Math.PI - brngB;
		}
		else
		{
			brng12 = 2*Math.PI - brngA;
			brng21 = brngB;
		}

		Double alpha1 = (brng13 - brng12 + Math.PI) % (2*Math.PI) - Math.PI;  // angle 2-1-3
		Double alpha2 = (brng21 - brng23 + Math.PI) % (2*Math.PI) - Math.PI;  // angle 1-2-3

		if (Math.sin(alpha1)==0 && Math.sin(alpha2)==0) 
		{
			return null;  // infinite intersections
		}
		if (Math.sin(alpha1)*Math.sin(alpha2) < 0) 
		{
			return null;       // ambiguous intersection
		}

		//alpha1 = Math.abs(alpha1);
		//alpha2 = Math.abs(alpha2);
		// ... Ed Williams takes abs of alpha1/alpha2, but seems to break calculation?

		Double alpha3 = Math.acos( -Math.cos(alpha1)*Math.cos(alpha2) + 
				Math.sin(alpha1)*Math.sin(alpha2)*Math.cos(dist12) );
		Double dist13 = Math.atan2( Math.sin(dist12)*Math.sin(alpha1)*Math.sin(alpha2), 
				Math.cos(alpha2)+Math.cos(alpha1)*Math.cos(alpha3) );
		Double lat3 = Math.asin( Math.sin(lat1)*Math.cos(dist13) + 
				Math.cos(lat1)*Math.sin(dist13)*Math.cos(brng13) );
		Double dLon13 = Math.atan2( Math.sin(brng13)*Math.sin(dist13)*Math.cos(lat1), 
				Math.cos(dist13)-Math.sin(lat1)*Math.sin(lat3) );
		Double lon3 = lon1+dLon13;
		lon3 = (lon3+3*Math.PI) % (2*Math.PI) - Math.PI;  // normalise to -180..+180º

		Location ret = new Location("na");
		ret.setLatitude(roundLatOrLong(Math.toDegrees(lat3)));
		ret.setLongitude(roundLatOrLong(Math.toDegrees(lon3)));
		return ret;

	}

	public static float distanceToFinish(Location boat, Buoy buoy1, Buoy buoy2)
	{
		Location buoyStart = new Location("na");
		buoyStart.setLatitude(buoy1.Position.latitude);
		buoyStart.setLongitude(buoy1.Position.longitude);

		Location buoyEnd = new Location("na");
		buoyEnd.setLatitude(buoy2.Position.latitude);
		buoyEnd.setLongitude(buoy2.Position.longitude);

		buoyStart.setBearing(buoyStart.bearingTo(buoyEnd));

		Location intersection = intersectionOfTwoPaths(boat, buoyStart);

		if( intersection == null )
		{
			return (float) EARTH_CIRCUMFERENCE;
		}

		float dIntersection = buoyStart.distanceTo(intersection);
		float dEnd = buoyStart.distanceTo(buoyEnd);
		double bInt = buoyStart.bearingTo(intersection);
		double bEnd = buoyStart.bearingTo(buoyEnd);

		//make sure intersection is within the finish line not halfway round the earth 
		if(  dIntersection > dEnd ||
				Math.abs( (bInt - bEnd) ) > 90 )
		{
			return EARTH_CIRCUMFERENCE;
		}
		return  boat.distanceTo(intersection);

	}

	public static double parseDoubleLocale(String d)
	{
		try
		{
			Number num = NumberFormat.getInstance().parse(d);
			double decimalDeg = num.doubleValue(); 
			if( !Double.isInfinite(decimalDeg) && !Double.isNaN(decimalDeg) )
			{
				return decimalDeg;
			}
		}
		catch(ParseException e)
		{

		}

		return Double.NaN;
	}
	public static double parseDMS (String dmsStr) 
	{
		double decimalDeg = Double.NaN;
		String dmsStrTrim = dmsStr.trim(); 

		if( !dmsStrTrim.contains(" "))
		{
			decimalDeg = parseDoubleLocale(dmsStr);
		}

		// check for signed decimal degrees without NSEW, if so return it directly
		if( Double.isNaN(decimalDeg) )
		{
			// strip off any sign or compass dir'n & split out separate d/m/s
			String cleaned = dmsStrTrim.replaceAll("^-","").replaceAll("[NSEWnsew°\"']","");
			List<String> dms = Arrays.asList(cleaned.split("[^0-9.,]"));

			if (dms.get(dms.size() - 1) == "")
			{
				dms.remove(dms.size() - 1);  // from trailing symbol
			}

			if (dms.size() == 0)
			{
				return Double.NaN;
			}

			// and convert to decimal degrees...
			switch (dms.size())
			{
			case 3:  // interpret 3-part result as d/m/s
				decimalDeg = parseDoubleLocale(dms.get(0)) + parseDoubleLocale(dms.get(1))/60 + parseDoubleLocale(dms.get(2))/3600; 
				break;
			case 2:  // interpret 2-part result as d/m
				decimalDeg = parseDoubleLocale(dms.get(0)) + parseDoubleLocale(dms.get(1))/60; 
				break;
			case 1:  // just d (possibly decimal) or non-separated dddmmss
				decimalDeg = parseDoubleLocale(dms.get(0));
				// check for fixed-width unseparated format eg 0033709W
				//if (/[NS]/i.test(dmsStr)) deg = '0' + deg;  // - normalise N/S to 3-digit degrees
				//if (/[0-9]{7}/.test(deg)) deg = deg.slice(0,3)/1 + deg.slice(3,5)/60 + deg.slice(5)/3600; 
				break;
			default:
				return Double.NaN;
			}

		}

		if( !Double.isNaN(decimalDeg) )
		{
			if( dmsStrTrim.startsWith("-") || dmsStrTrim.contains("W") || dmsStrTrim.contains("S") || dmsStrTrim.contains("w") || dmsStrTrim.contains("s")) 
			{
				decimalDeg = -decimalDeg;
			}
			decimalDeg = roundLatOrLong(decimalDeg);
		}
		
		return decimalDeg;

	}
}
