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
