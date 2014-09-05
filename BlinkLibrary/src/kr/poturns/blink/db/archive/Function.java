package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Function 테이블과 맵핑되는 클래스
 * @author Jiwon
 *
 */
public class Function implements IDatabaseObject, Parcelable{
	public static final int TYPE_ACTIVITY = 1;
	public static final int TYPE_SERIVCE = 2;
	public static final int TYPE_BROADCAST = 3;
	
	public int AppId;
	public String Function;
	public String Description;
	public String Action;
	public int Type;
	
	public Function(){
		this.AppId = -1;
		this.Description = "";
	}
	public Function(String Function,String Description,String Action,int Type){
		this.Function = Function;
		this.Description = Description;
		this.Action = Action;
		this.Type = Type;
	}
	
	public String toString(){
		String ret = "";
		ret += "AppId : "+AppId+"\r\n";
		ret += "Function : "+Function+"\r\n";
		ret += "Description : "+Description+"\r\n";
		ret += "Action : "+Action+"\r\n";
		ret += "Type : "+Type+"\r\n";
		return ret;
	}
	
	/**
	 * Function 테이블의 등록 조건을 만족하는지 확인한다.
	 */
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		if(Function!=null&&Function.length()>0)return true;
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
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public Function(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		Function mFunction = JsonManager.gson.fromJson(
				in.readString(), Function.class);
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