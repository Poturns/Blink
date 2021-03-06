package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * BlinkLog 테이블과 맵핑되는 클래스 <br>
 * <br>
 * <b>Blink Service</b>에서 작업을 수행할 때 남기는 Log를 의미한다.
 * 
 * @author Jiwon
 * 
 */
public class BlinkLog implements IDatabaseObject, Parcelable {
	/**Database에서의 BlinkLog의 ID*/
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

	/**
	 * BlinkLog 테이블의 등록 조건을 만족하는지 확인한다.
	 */
	@Override
	public boolean checkIntegrity() {
		return true;
	}

	/*
	 * Parcelable 구현 매소드들
	 */

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<BlinkLog> CREATOR = new Parcelable.Creator<BlinkLog>() {
		public BlinkLog createFromParcel(Parcel in) {
			return new BlinkLog(in);
		}

		public BlinkLog[] newArray(int size) {
			return new BlinkLog[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public BlinkLog(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		BlinkLog mBlinkLog = JsonManager.gson.fromJson(in.readString(),
				BlinkLog.class);
		CopyFromOtherObject(mBlinkLog);
	}

	public void CopyFromOtherObject(BlinkLog mBlinkLog) {
		this.LogId = mBlinkLog.LogId;
		this.Device = mBlinkLog.Device;
		this.App = mBlinkLog.App;
		this.Type = mBlinkLog.Type;
		this.Content = mBlinkLog.Content;
		this.DateTime = mBlinkLog.DateTime;
	}
}
