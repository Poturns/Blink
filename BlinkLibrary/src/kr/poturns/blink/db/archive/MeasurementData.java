package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.schema.DefaultSchema;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * MeasurementData 테이블과 맵핑되는 클래스
 * 
 * <br>
 * <br>
 * <b>Blink Database</b>에서 <b>Application</b>이 측정할 수 있는 데이터의 <b>실제 측정 데이터</b>를
 * 의미한다.
 * 
 * @author Jiwon
 * 
 */
public class MeasurementData extends DefaultSchema.Base implements
		IDatabaseObject, Parcelable {
	/** MeasurementData가 속한 Measurement의 ID */
	public int MeasurementId;
	/** MeasurementData의 ID */
	public int MeasurementDataId;
	/**
	 * GroupId를 통해 같이 등록된 데이터인지 확인할 수 있다. 시간상의 일치가 아닌 동일한 객체를 통해 등록된 데이터인지 확인한다.
	 */
	public int GroupId;
	/** 실제 측정된 데이터 */
	public String Data;

	public MeasurementData() {
	}

	public String toString() {
		String ret = "";
		ret += "MeasurementId : " + MeasurementId + "\r\n";
		ret += "MeasurementDataId : " + MeasurementDataId + "\r\n";
		ret += "GroupId : " + GroupId + "\r\n";
		ret += "Data : " + Data + "\r\n";
		ret += "DateTime : " + DateTime + "\r\n";
		return ret;
	}

	/**
	 * MeasurementData 테이블의 등록 조건을 만족하는지 확인한다.
	 */
	@Override
	public boolean checkIntegrity() {
		return false;
	}

	/*
	 * Parcelable 구현 매소드들
	 */

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<MeasurementData> CREATOR = new Parcelable.Creator<MeasurementData>() {
		public MeasurementData createFromParcel(Parcel in) {
			return new MeasurementData(in);
		}

		public MeasurementData[] newArray(int size) {
			return new MeasurementData[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public MeasurementData(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		MeasurementData mMeasurementData = JsonManager.gson.fromJson(
				in.readString(), MeasurementData.class);
		CopyFromOtherObject(mMeasurementData);
	}

	public void CopyFromOtherObject(MeasurementData mMeasurementData) {
		this.MeasurementId = mMeasurementData.MeasurementId;
		this.GroupId = mMeasurementData.GroupId;
		this.Data = mMeasurementData.Data;
		this.DateTime = mMeasurementData.DateTime;
	}
}
