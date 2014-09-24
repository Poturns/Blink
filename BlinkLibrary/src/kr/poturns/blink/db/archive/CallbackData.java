package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 외부 디바이스로 데이터를 요청했을 때 반환되는 형식이다. <br>
 * 내부 데이터, 외부 데이터, 호출 결과, 호출 상세 결과의 정보를 갖는다.<br>
 * 데이터 반환 형식은 Json이다.<br>
 * 
 * @author Jiwon
 * 
 */
public class CallbackData implements Parcelable {
	/**
	 * 에러 없이 성공적으로 호출되었을 경우
	 */
	public static final int ERROR_NO = 0x00;
	/**
	 * 자기 자신이 Cetner 디바이스인 경우
	 */
	public static final int ERROR_CENTER_DEVICE = 0x01;
	/**
	 * 외부 디바이스에 데이터가 없는데 호출한 경우
	 */
	public static final int ERROR_NO_OUT_DEVICE = 0x02;
	/**
	 * 디바이스에 데이터가 없는데 호출한 경우
	 */
	public static final int ERROR_CONNECT_FAIL = 0x03;

	public String InDeviceData;
	public String OutDeviceData;
	public boolean Result;
	public int ResultDetail;

	public CallbackData() {
		InDeviceData = null;
		OutDeviceData = null;
		Result = true;
		ResultDetail = ERROR_NO;
	}

	/*
	 * Parcelable 구현 매소드들
	 */

	@Override
	public int describeContents() {
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
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public CallbackData(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		CallbackData mCallbackData = JsonManager.gson.fromJson(in.readString(),
				CallbackData.class);
		CopyFromOtherObject(mCallbackData);
	}

	public void CopyFromOtherObject(CallbackData mCallbackData) {
		this.InDeviceData = mCallbackData.InDeviceData;
		this.OutDeviceData = mCallbackData.OutDeviceData;
		this.Result = mCallbackData.Result;
		this.ResultDetail = mCallbackData.ResultDetail;
	}
}
