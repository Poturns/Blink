package kr.poturns.blink.db.archive;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

public class DeviceAppLog implements IDatabaseObject, Parcelable{
	public int LogId;
	public String Device;
	public String App;
	public int Type;
	public String Content;
	public String DateTime;
	
	public DeviceAppLog() {
		this.LogId = -1;
		this.Device = "";
		this.App = "";
		this.Type = -1;
		this.Content = "";
		this.DateTime = "";
	}

	public String toString() {
		String ret = "";
		ret += "LogId : " + LogId + "\r\n";
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

	public static final Parcelable.Creator<DeviceAppLog> CREATOR = new Parcelable.Creator<DeviceAppLog>() {
		 public DeviceAppLog createFromParcel(Parcel in) {
		 	return new DeviceAppLog(in);
		 }
	        
		 public DeviceAppLog[] newArray( int size ) {
			 return new DeviceAppLog[size];
		 }
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}
	public DeviceAppLog(Parcel in){
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in){
		DeviceAppLog mDeviceAppLog = JsonManager.gson.fromJson(in.readString(), DeviceAppLog.class);
		CopyFromOtherObject(mDeviceAppLog);
	}
	public void CopyFromOtherObject(DeviceAppLog mDeviceAppLog){
		this.LogId = mDeviceAppLog.LogId;
		this.Device = mDeviceAppLog.Device;
		this.App = mDeviceAppLog.App;
		this.Type = mDeviceAppLog.Type;
		this.Content = mDeviceAppLog.Content;
		this.DateTime = mDeviceAppLog.DateTime;
	}
}
