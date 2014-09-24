package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Measurement 테이블과 맵핑되는 클래스
 * 
 * <br>
 * <br>
 * <b>Blink Database</b>에서 <b>Application</b>이 <b>측정할 수 있는 데이터</b>를 의미한다.
 * 
 * @author Jiwon
 * 
 */
public class Measurement implements IDatabaseObject, Parcelable {
	/* 측정한 데이터의 타입을 나타낸다. */
	public static final String TYPE_STRING = "string";
	public static final String TYPE_INT = "int";
	public static final String TYPE_DOUBLE = "double";
	public static final String TYPE_FLOAT = "float";
	public static final String TYPE_SHORT = "short";

	/** Measurement가 속한 App의 ID */
	public int AppId;
	/** Measurement의 ID */
	public int MeasurementId;
	/** Measurement의 이름 */
	public String MeasurementName;
	/** Measurement의 Schema */
	public String Measurement;
	/** Measurement의 타입 */
	public String Type;
	/** Measurement의 설명 */
	public String Description;

	public Measurement() {
		this.AppId = -1;
		this.Description = "";
	}

	public Measurement(String MeasurementName, String Measurement, String Type,
			String Description) {
		this.MeasurementName = MeasurementName;
		this.Measurement = Measurement;
		this.Type = Type;
		this.Description = Description;
	}

	public MeasurementData obtainMeasurement() {
		MeasurementData mMeasurementData = new MeasurementData();
		mMeasurementData.MeasurementId = this.MeasurementId;
		return mMeasurementData;
	}

	public String toString() {
		String ret = "";
		ret += "DeviceAppId : " + AppId + "\r\n";
		ret += "MeasurementId : " + MeasurementId + "\r\n";
		ret += "Measurement : " + Measurement + "\r\n";
		ret += "Type : " + Type + "\r\n";
		ret += "Description : " + Description + "\r\n";
		return ret;
	}

	/**
	 * Measurement 테이블의 등록 조건을 만족하는지 확인한다.
	 */
	@Override
	public boolean checkIntegrity() {
		if (Measurement != null && Type != null && Measurement.length() > 0
				&& Type.length() > 0)
			return true;
		return false;
	}

	/*
	 * Parcelable 구현 매소드들
	 */

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<Measurement> CREATOR = new Parcelable.Creator<Measurement>() {
		public Measurement createFromParcel(Parcel in) {
			return new Measurement(in);
		}

		public Measurement[] newArray(int size) {
			return new Measurement[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public Measurement(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		Measurement mMeasurement = JsonManager.gson.fromJson(in.readString(),
				Measurement.class);
		CopyFromOtherObject(mMeasurement);
	}

	public void CopyFromOtherObject(Measurement mMeasurement) {
		this.AppId = mMeasurement.AppId;
		this.MeasurementId = mMeasurement.MeasurementId;
		this.Measurement = mMeasurement.Measurement;
		this.Type = mMeasurement.Type;
		this.Description = mMeasurement.Description;
	}
}
