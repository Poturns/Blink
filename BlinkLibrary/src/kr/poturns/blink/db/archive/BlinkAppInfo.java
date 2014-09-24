package kr.poturns.blink.db.archive;

import java.lang.reflect.Field;
import java.util.ArrayList;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.util.ClassUtil;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Blink 라이브러리를 사용하는 어플리케이션의 정보를 포괄적으로 나타내는 클래스이다. <br>
 * <li>해당 어플리케이션이 속한 Device 정보 : {@link BlinkAppInfo#mDevice}</li> <li>해당 어플리케이션
 * 정보 : {@link BlinkAppInfo#mApp}</li> <li>해당 어플리케이션이 실행 시킬 수 있는 기능 정보 :
 * {@link BlinkAppInfo#mFunctionList}</li> <li>해당 어플리케이션이 측정하는 데이터의 정보 :
 * {@link BlinkAppInfo#mMeasurementList}</li>
 * 
 * @author Jiwon
 * @see Device
 * @see App
 * @see Function
 * @see Measurement
 */
public class BlinkAppInfo implements Parcelable, IDatabaseObject {
	// private final String tag = "BlinkAppInfo";

	public boolean isExist;
	public Device mDevice;
	public App mApp;
	public ArrayList<Function> mFunctionList;
	public ArrayList<Measurement> mMeasurementList;

	public BlinkAppInfo() {
		onCreate();
	}

	public void onCreate() {
		isExist = false;
		mDevice = new Device();
		mApp = new App();
		mFunctionList = new ArrayList<Function>();
		mMeasurementList = new ArrayList<Measurement>();
	}

	public void addFunction(String Function, String Description, String Action,
			int Type) {
		mFunctionList.add(new Function(Function, Description, Action, Type));
	}

	public void addMeasurement(String MeasurementName, String Measurement,
			String Type, String Description) {
		mMeasurementList.add(new Measurement(MeasurementName, Measurement,
				Type, Description));
	}

	public MeasurementData obtainMeasurementData(String Measurement) {
		for (int i = 0; i < mMeasurementList.size(); i++) {
			if (mMeasurementList.get(i).Measurement.contentEquals(Measurement)) {
				return mMeasurementList.get(i).obtainMeasurement();
			}
		}
		return null;
	}

	// Java reflect을 이용한 Measurement 추가
	public void addMeasurement(Class<?> obj) {
		Field[] mFields = obj.getFields();
		for (int i = 0; i < mFields.length; i++) {
			if (mFields[i].getName().contentEquals("DateTime"))
				continue;
			Measurement mMeasurement = new Measurement(obj.getSimpleName(),
					ClassUtil.obtainFieldSchema(mFields[i]), mFields[i]
							.getType().getName(), "");
			mMeasurementList.add(mMeasurement);
		}
	}

	public String toString() {
		String ret = "";
		ret += mDevice.toString();
		ret += mApp.toString();
		for (int i = 0; i < mFunctionList.size(); i++) {
			ret += mFunctionList.get(i).toString();
		}
		for (int i = 0; i < mMeasurementList.size(); i++) {
			ret += mMeasurementList.get(i).toString();
		}
		return ret;
	}

	/**
	 * BlinkAppInfo 테이블의 등록 조건을 만족하는지 확인한다.
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

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public static final Parcelable.Creator<BlinkAppInfo> CREATOR = new Parcelable.Creator<BlinkAppInfo>() {
		public BlinkAppInfo createFromParcel(Parcel in) {
			return new BlinkAppInfo(in);
		}

		public BlinkAppInfo[] newArray(int size) {
			return new BlinkAppInfo[size];
		}
	};

	public BlinkAppInfo(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		BlinkAppInfo mSystemDatabaseObject = JsonManager.gson.fromJson(
				in.readString(), BlinkAppInfo.class);
		copyFromOtherObject(mSystemDatabaseObject);
	}

	public void copyFromOtherObject(BlinkAppInfo mSystemDatabaseObject) {
		this.isExist = mSystemDatabaseObject.isExist;
		this.mDevice = mSystemDatabaseObject.mDevice;
		this.mApp = mSystemDatabaseObject.mApp;
		this.mFunctionList = mSystemDatabaseObject.mFunctionList;
		this.mMeasurementList = mSystemDatabaseObject.mMeasurementList;
	}

}
