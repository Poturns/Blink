package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.schema.DefaultSchema;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * MeasurementData 테이블과 맵핑되는 클래스
 * @author Jiwon
 *
 */
public class MeasurementData extends DefaultSchema implements IDatabaseObject, Parcelable {
	public int MeasurementId;
	public int MeasurementDataId;
	/**
	 * GroupId를 통해 같이 등록된 데이터인지 확인할 수 있다.
	 * 시간상의 일치가 아닌 동일한 객체를 통해 등록된 데이터인지 확인한다.
	 */
	public int GroupId;
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
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Parcelable 구현 매소드들
	 */
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
