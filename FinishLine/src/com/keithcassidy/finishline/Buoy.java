package com.keithcassidy.finishline;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Buoy implements Parcelable
{
	public Buoy() 
	{
	}
	public Buoy(long id, String name, LatLng position)
	{
		Id = id;
		Name = name;
		Position = position;
	}
	public Buoy(long id, String name, Double lat, Double lng) {
		Id = id;
		Name = name;
		Position = new LatLng(lat, lng);
	}
	public long Id;
	public String Name;
	public LatLng Position;
	
	@Override
	public String toString() 
	{
		return Name;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int flags) 
	{
		out.writeLong(Id);
		out.writeString(Name);
		Position.writeToParcel(out, flags);
		
	}	

	public static final Parcelable.Creator<Buoy> CREATOR = new Parcelable.Creator<Buoy>() 
	{
		public Buoy createFromParcel(Parcel in) 
		{
			return new Buoy(in);
		}

		public Buoy[] newArray(int size) 
		{
			return new Buoy[size];
		}
	};

	private Buoy(Parcel in) 
	{
		Id = in.readLong();
		Name = in.readString();
		Position = LatLng.CREATOR.createFromParcel(in);
	}	
}
