package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;


public class DeviceAppMeasurement implements IDatabaseObject, Parcelable{
	
	public int DeviceAppId;
	public int MeasurementId;
	public String Measurement;
	public String Type;
	public String Description;
	
	public DeviceAppMeasurement(){
		this.Description = "";
	}
	public DeviceAppMeasurement(String Measurement,String Type,String Description){
		this.Measurement = Measurement;
		this.Type = Type;
		this.Description = Description;
	}
	public MeasurementData obtainMeasurement(){
		MeasurementData mMeasurementData = new MeasurementData();
		mMeasurementData.MeasurementId = this.MeasurementId;
		return mMeasurementData;
	}
	
	
	public String toString(){
		String ret = "";
		ret += "DeviceAppId : "+DeviceAppId+"\r\n";
		ret += "MeasurementId : "+MeasurementId+"\r\n";
		ret += "Measurement : "+Measurement+"\r\n";
		ret += "Type : "+Type+"\r\n";
		ret += "Description : "+Description+"\r\n";
		return ret;
	}
	/**
	 * DeviceAppMeasurement 테이블에 등록하기 위한 최소한의 조건을 만족하는지 확인
	 * 테이블 구조대로 Measurement와 Type 필드가 null이 아니여야한다.
	 * param	:	void
	 * return	:	boolean (Measurement와 Type 변수가 null이 아니고 길이가 0보다 클 경우) 
	 * 				false (Measurement와 Type 변수가 null이거나 길이가 0인 경우)
	 */
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		if(Measurement!=null&&Type!=null&&Measurement.length()>0&&Type.length()>0)return true;
		return false;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<DeviceAppMeasurement> CREATOR = new Parcelable.Creator<DeviceAppMeasurement>() {
		 public DeviceAppMeasurement createFromParcel(Parcel in) {
		 	return new DeviceAppMeasurement(in);
		 }
	        
		 public DeviceAppMeasurement[] newArray( int size ) {
			 return new DeviceAppMeasurement[size];
		 }
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}
	public DeviceAppMeasurement(Parcel in){
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in){
		DeviceAppMeasurement mDeviceAppMeasurement = JsonManager.gson.fromJson(in.readString(), DeviceAppMeasurement.class);
		CopyFromOtherObject(mDeviceAppMeasurement);
	}
	public void CopyFromOtherObject(DeviceAppMeasurement mDeviceAppMeasurement){
		this.DeviceAppId = mDeviceAppMeasurement.DeviceAppId;
		this.MeasurementId = mDeviceAppMeasurement.MeasurementId;
		this.Measurement = mDeviceAppMeasurement.Measurement;
		this.Type = mDeviceAppMeasurement.Type;
		this.Description = mDeviceAppMeasurement.Description;
	}
}
