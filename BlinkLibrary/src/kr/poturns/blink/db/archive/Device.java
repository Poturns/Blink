package kr.poturns.blink.db.archive;

import kr.poturns.blink.db.JsonManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Device 테이블과 맵핑되는 클래스이다. <br>
 * Device 테이블의 칼럼과 동일한 명칭과 타입의 필드를 가지고 있다.<br>
 * <br>
 * <b>Blink Database</b>에서 <b>Application</b>이 속한 <b>Device</b>를 의미한다.
 * 
 * @author Jiwon
 * 
 */
public class Device implements IDatabaseObject, Parcelable {
	/** Database에서 해당 Device의 인덱스 */
	public int DeviceId;
	/** Device의 이름 */
	public String Device;
	/** Device의 UUID */
	public String UUID;
	/** Device의 MacAddress */
	public String MacAddress;
	public String DateTime;

	public Device() {
		DeviceId = -1;
		Device = "";
		UUID = "";
		MacAddress = "";
	}

	public String toString() {
		String ret = "";
		ret += "DeviceId : " + DeviceId + "\r\n";
		ret += "Device : " + Device + "\r\n";
		ret += "UUID : " + UUID + "\r\n";
		ret += "MacAddress : " + MacAddress + "\r\n";
		return ret;
	}

	/**
	 * Device 테이블의 등록 조건을 만족하는지 확인한다.
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
		dest.writeString(JsonManager.gson.toJson(this));
	}

	public Device(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		Device mDevice = JsonManager.gson.fromJson(in.readString(),
				Device.class);
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