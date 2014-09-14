package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Device 테이블과 맵핑되는 클래스
 * @author Jiwon
 *
 */
public class Device implements IDatabaseObject,Parcelable{

	public int DeviceId;
	public String Device;
	public String UUID;
	public String MacAddress;
	public String DateTime;
	
	public Device(){
		DeviceId = -1;
		Device = "";
		UUID = "";
		MacAddress = "";
	}
	
	public String toString(){
		String ret = "";
		ret += "DeviceId : "+DeviceId+"\r\n";
		ret += "Device : "+Device+"\r\n";
		ret += "UUID : "+UUID+"\r\n";
		ret += "MacAddress : "+MacAddress+"\r\n";
		return ret;
	}
	
	/**
	 * Device 테이블의 등록 조건을 만족하는지 확인한다.
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

	public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
		public Device createFromParcel(Parcel in) {
			return new Device(in);
		}

		public Device[] newArray(int size) {
			return new Device[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public Device(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		Device mDevice = JsonManager.gson.fromJson(
				in.readString(), Device.class);
		CopyFromOtherObject(mDevice);
	}

	public void CopyFromOtherObject(Device mDevice) {
		this.DeviceId = mDevice.DeviceId;
		this.Device = mDevice.Device;
		this.UUID = mDevice.UUID;
		this.MacAddress = mDevice.MacAddress;
		this.DateTime = mDevice.DateTime;
	}
}