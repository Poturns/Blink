package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * App 테이블과 맵핑되는 클래스
 * @author Jiwon
 *
 */
public class App implements IDatabaseObject,Parcelable{

	public int AppId;
	public int DeviceId;
	public String PackageName;
	public String AppName;
	public byte[] AppIcon;
	public int Version;
	public String DateTime;
	
	public App(){
		AppId = -1;
		DeviceId = -1;
		PackageName = "";
		AppName = "";
		AppIcon = null;
		Version = -1;
	}
	
	public String toString(){
		String ret = "";
		ret += "AppId : "+AppId+"\r\n";
		ret += "DeviceId : "+DeviceId+"\r\n";
		ret += "PackageName : "+PackageName+"\r\n";
		ret += "AppName : "+AppName+"\r\n";
		ret += "Version : "+Version+"\r\n";
		return ret;
	}
	
	/**
	 * App 테이블의 등록 조건을 만족하는지 확인한다.
	 */
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * Parcelable 구현 매소드들
	 */

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<App> CREATOR = new Parcelable.Creator<App>() {
		public App createFromParcel(Parcel in) {
			return new App(in);
		}

		public App[] newArray(int size) {
			return new App[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public App(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		App mApp = JsonManager.gson.fromJson(
				in.readString(), App.class);
		CopyFromOtherObject(mApp);
	}

	public void CopyFromOtherObject(App mApp) {
		this.AppId = mApp.AppId;
		this.DeviceId = mApp.DeviceId;
		this.PackageName = mApp.PackageName;
		this.AppName = mApp.AppName;
		this.AppIcon = mApp.AppIcon;
		this.Version = mApp.Version;
		this.DateTime = mApp.DateTime;
	}
}