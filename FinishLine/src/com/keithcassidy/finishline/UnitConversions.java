package com.keithcassidy.finishline;

public class UnitConversions 
{
	  private UnitConversions() {}
	  
	  // multiplication factor to convert kilometers to miles
	  public static final double KM_TO_MI = 0.621371192;

	  // multiplication factor to convert miles to kilometers
	  public static final double MI_TO_KM = 1 / KM_TO_MI;

	  // multiplication factor to convert miles to feet
	  public static final double MI_TO_FT = 5280.0;

	  // multiplication factor to convert feet to miles
	  public static final double FT_TO_MI = 1 / MI_TO_FT;

	  // multiplication factor to convert meters to kilometers
	  public static final double M_TO_KM = 1 / 1000.0;

	  // multiplication factor to convert meters per second to kilometers per hour
	  public static final double MS_TO_KMH = 3.6;

	  // multiplication factor to convert meters to miles
	  public static final double M_TO_MI = M_TO_KM * KM_TO_MI;

	  // multiplication factor to convert meters to feet
	  public static final double M_TO_FT = M_TO_MI * MI_TO_FT;

	  // multiplication factor to convert degrees to radians
	  public static final double DEG_TO_RAD = Math.PI / 180.0;
}
