package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

public class CallbackData implements Parcelable{	
	public String InDeviceData;
	public String OutDeviceData;
	public int Error;
	
	/**
	 * Parcelable implements
	 */
	public CallbackData(){
		InDeviceData = null;
		OutDeviceData = null;
		Error = 0;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<CallbackData> CREATOR = new Parcelable.Creator<CallbackData>() {
		public CallbackData createFromParcel(Parcel in) {
			return new CallbackData(in);
		}

		public CallbackData[] newArray(int size) {
			return new CallbackData[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public CallbackData(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		CallbackData mCallbackData = JsonManager.gson.fromJson(
				in.readString(), CallbackData.class);
		CopyFromOtherObject(mCallbackData);
	}

	public void CopyFromOtherObject(CallbackData mCallbackData) {
		this.InDeviceData = mCallbackData.InDeviceData;
		this.OutDeviceData = mCallbackData.OutDeviceData;
		this.Error = mCallbackData.Error;
	}
}
