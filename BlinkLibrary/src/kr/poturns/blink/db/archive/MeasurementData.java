package kr.poturns.blink.db.archive;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;


public class MeasurementData implements IDatabaseObject,Parcelable{
	public int MeasurementId;
	public int GroupId;
	public String Data;
	public String DateTime;
	
	public MeasurementData(){
		
	}

	public String toString(){
		String ret = "";
		ret += "MeasurementId : "+MeasurementId+"\r\n";
		ret += "MeasurementDataId : "+GroupId+"\r\n";
		ret += "Data : "+Data+"\r\n";
		ret += "DateTime : "+DateTime+"\r\n";
		return ret;
	}
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<MeasurementData> CREATOR = new Parcelable.Creator<MeasurementData>() {
		 public MeasurementData createFromParcel(Parcel in) {
		 	return new MeasurementData(in);
		 }
	        
		 public MeasurementData[] newArray( int size ) {
			 return new MeasurementData[size];
		 }
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}
	public MeasurementData(Parcel in){
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in){
		MeasurementData mMeasurementData = JsonManager.gson.fromJson(in.readString(),MeasurementData.class);
		CopyFromOtherObject(mMeasurementData);
	}
	public void CopyFromOtherObject(MeasurementData mMeasurementData){
		this.MeasurementId = mMeasurementData.MeasurementId;
		this.GroupId = mMeasurementData.GroupId;
		this.Data = mMeasurementData.Data;
		this.DateTime = mMeasurementData.DateTime;
	}
}
