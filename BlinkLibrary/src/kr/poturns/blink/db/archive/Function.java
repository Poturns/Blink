package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Function 테이블과 맵핑되는 클래스
 * 
 * <br>
 * <br>
 * <b>Blink Database</b>에서 <b>Application</b>이 수행할 수 있는 <b>기능</b>을 의미한다.
 * 
 * @author Jiwon
 * 
 */
public class Function implements IDatabaseObject, Parcelable {
	/* Funtion이 어떠한 방식을 거쳐 수행 될 지를 결정하는 변수 */
	/** Activity를 통해 실행하는 기능을 의미한다. */
	public static final int TYPE_ACTIVITY = 1;
	/** Service를 통해 실행하는 기능을 의미한다. */
	public static final int TYPE_SERIVCE = 2;
	/** Broadcase를 통해 실행하는 기능을 의미한다. */
	public static final int TYPE_BROADCAST = 3;

	/** Function이 속한 App의 ID */
	public int AppId;
	/** Function의 이름 */
	public String Function;
	/** Function의 설명 */
	public String Description;
	/** Function의 행동 */
	public String Action;
	/** Function이 수행 될 방식 */
	public int Type;

	public Function() {
		this.AppId = -1;
		this.Description = "";
	}

	public Function(String Function, String Description, String Action, int Type) {
		this.Function = Function;
		this.Description = Description;
		this.Action = Action;
		this.Type = Type;
	}

	public String toString() {
		String ret = "";
		ret += "AppId : " + AppId + "\r\n";
		ret += "Function : " + Function + "\r\n";
		ret += "Description : " + Description + "\r\n";
		ret += "Action : " + Action + "\r\n";
		ret += "Type : " + Type + "\r\n";
		return ret;
	}

	/**
	 * Function 테이블의 등록 조건을 만족하는지 확인한다.
	 */
	@Override
	public boolean checkIntegrity() {
		if (Function != null && Function.length() > 0)
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

	public static final Parcelable.Creator<Function> CREATOR = new Parcelable.Creator<Function>() {
		public Function createFromParcel(Parcel in) {
			return new Function(in);
		}

		public Function[] newArray(int size) {
			return new Function[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public Function(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		Function mFunction = JsonManager.gson.fromJson(in.readString(),
				Function.class);
		CopyFromOtherObject(mFunction);
	}

	public void CopyFromOtherObject(Function mFunction) {
		this.AppId = mFunction.AppId;
		this.Function = mFunction.Function;
		this.Description = mFunction.Description;
		this.Action = mFunction.Action;
		this.Type = mFunction.Type;
	}
}