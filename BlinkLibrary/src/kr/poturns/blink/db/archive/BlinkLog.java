package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

public class BlinkLog implements IDatabaseObject, Parcelable{
	public int LogId;
	public String Device;
	public String App;
	public int Type;
	public String Content;
	public String DateTime;
	
	public BlinkLog() {
		this.LogId = -1;
		this.Device = "";
		this.App = "";
		this.Type = -1;
		this.Content = "";
		this.DateTime = "";
	}

	public String toString() {
		String ret = "";
		ret += "BlinkLogId : " + LogId + "\r\n";
		ret += "Device : " + Device + "\r\n";
		ret += "App : " + App + "\r\n";
		ret += "Type : " + Type + "\r\n";
		ret += "Content : " + Content + "\r\n";
		ret += "DateTime : " + DateTime + "\r\n";
		return ret;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * Parcelable implement
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<BlinkLog> CREATOR = new Parcelable.Creator<BlinkLog>() {
		 public BlinkLog createFromParcel(Parcel in) {
		 	return new BlinkLog(in);
		 }
	        
		 public BlinkLog[] newArray( int size ) {
			 return new BlinkLog[size];
		 }
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}
	public BlinkLog(Parcel in){
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in){
		BlinkLog mBlinkLog = JsonManager.gson.fromJson(in.readString(), BlinkLog.class);
		CopyFromOtherObject(mBlinkLog);
	}
	public void CopyFromOtherObject(BlinkLog mBlinkLog){
		this.LogId = mBlinkLog.LogId;
		this.Device = mBlinkLog.Device;
		this.App = mBlinkLog.App;
		this.Type = mBlinkLog.Type;
		this.Content = mBlinkLog.Content;
		this.DateTime = mBlinkLog.DateTime;
	}
}
