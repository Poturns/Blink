package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;


public class Measurement implements IDatabaseObject, Parcelable{
	
	public int AppId;
	public int MeasurementId;
	public String Measurement;
	public String Type;
	public String Description;
	
	public Measurement(){
		this.AppId = -1;
		this.Description = "";
	}
	public Measurement(String Measurement,String Type,String Description){
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
		ret += "DeviceAppId : "+AppId+"\r\n";
		ret += "MeasurementId : "+MeasurementId+"\r\n";
		ret += "Measurement : "+Measurement+"\r\n";
		ret += "Type : "+Type+"\r\n";
		ret += "Description : "+Description+"\r\n";
		return ret;
	}
	/**
	 * Measurement 테이블에 등록하기 위한 최소한의 조건을 만족하는지 확인
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

	public static final Parcelable.Creator<Measurement> CREATOR = new Parcelable.Creator<Measurement>() {
		 public Measurement createFromParcel(Parcel in) {
		 	return new Measurement(in);
		 }
	        
		 public Measurement[] newArray( int size ) {
			 return new Measurement[size];
		 }
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}
	public Measurement(Parcel in){
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in){
		Measurement mMeasurement = JsonManager.gson.fromJson(in.readString(), Measurement.class);
		CopyFromOtherObject(mMeasurement);
	}
	public void CopyFromOtherObject(Measurement mMeasurement){
		this.AppId = mMeasurement.AppId;
		this.MeasurementId = mMeasurement.MeasurementId;
		this.Measurement = mMeasurement.Measurement;
		this.Type = mMeasurement.Type;
		this.Description = mMeasurement.Description;
	}
}
