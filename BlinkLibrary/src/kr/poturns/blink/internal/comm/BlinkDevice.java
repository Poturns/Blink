package kr.poturns.blink.internal.comm;

import java.io.Serializable;
import java.util.HashMap;

import kr.poturns.blink.internal.DeviceAnalyzer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Blink 서비스에서 블루투스 디바이스들을 관리하기 위한 기본 객체.
 * 
 * @author Yeonho.Kim
 * @since 2014.08.15
 *
 */
public class BlinkDevice implements Parcelable, Serializable {

	// *** CONSTANT DECLARATION *** //
	/**
	 * 
	 */
	private static final long serialVersionUID = 1515234603753308973L;

	/**
	 * Blink Device 객체에 대한 Cache.
	 */
	private static final HashMap<String, BlinkDevice> CACHE_MAP = new HashMap<String, BlinkDevice>();
	
	public static BlinkDevice load(BluetoothDevice device) {
		BlinkDevice mDevice = BlinkDevice.load(device.getAddress());
		
		if (mDevice != null)
			return mDevice.update(device);
		return null;
	}
	
	public static BlinkDevice load(String address) {
		// TODO: Address 유효성 판단 할 것.
		
		if (CACHE_MAP.containsKey(address))
			return CACHE_MAP.get(address);
		
		else {
			// TODO : System 파일에서 추출해온다.
			
			// TODO : System 파일에서 없을 경우, 새로 만든다.
			
			BlinkDevice device = new BlinkDevice(address);
			
			CACHE_MAP.put(address, device);
			return device;
		}
	}
	
	public static void removeDeviceCache(String address) {
		CACHE_MAP.remove(address);
	}
	
	public static void clearCache() {
		CACHE_MAP.clear();
	}
	

	// *** FIELD DECLARATION *** //
	private String Address;
	private String Name;
	private int Type;
	
	private int Identity;
	private int IdentityPoint;

	private boolean AutoConnect;
	private boolean Secure;
	
	private boolean Connected;
	private boolean Discovered;

	private long Timestamp;
	
	private BluetoothDevice tempDevice;
	
	private BlinkDevice(String address) {
		this.Address = address;
		
		Type = BluetoothDevice.DEVICE_TYPE_UNKNOWN;
		
		Identity = DeviceAnalyzer.Identity.UNKNOWN.ordinal();
		IdentityPoint = 0;
		
		AutoConnect = false;
		Secure = true;
		
		Timestamp = System.currentTimeMillis();
	}
	
	BlinkDevice(Parcel parcel) {	
		readFromParcel(parcel);
	}
	
	BlinkDevice(BluetoothDevice device) {
		this(device.getAddress());
		
		Name = device.getName();
		Type = device.getType();
		device.getUuids();
		
		tempDevice = device;
	}

	void readFromParcel(Parcel parcel) {
		Address = parcel.readString();
		Name = parcel.readString();
		Type = parcel.readInt();
		
		Identity = parcel.readInt();
		IdentityPoint = parcel.readInt();
		
		boolean[] bools = new boolean[4];
		parcel.readBooleanArray(bools);
		
		AutoConnect = bools[0];
		Secure = bools[1];
		
		Connected = bools[2];
		Discovered = bools[3];
		
		Timestamp = parcel.readLong();
	}
	
	public BlinkDevice update(BluetoothDevice device) {
		String name = device.getName();
		if (name != null)
			Name = name;
		
		int type = device.getType();
		if (type != BluetoothDevice.DEVICE_TYPE_UNKNOWN)
			Type = type;
		
		
		
		Timestamp = System.currentTimeMillis();
		
		tempDevice = device;
		return this;
	}
	
	public BluetoothDevice obtainBluetoothDevice() {
		tempDevice = (tempDevice == null)? 
				BluetoothAdapter.getDefaultAdapter().getRemoteDevice(Address) : tempDevice;
	
		return tempDevice;
	}
	

	// *** CALLBACK DECLARATION *** //
	 public static final Parcelable.Creator<BlinkDevice> CREATOR = new Parcelable.Creator<BlinkDevice>() {
	        public BlinkDevice createFromParcel(Parcel in) {
	            return new BlinkDevice(in);
	        }
	        
	        public BlinkDevice[] newArray( int size ) {
	            return new BlinkDevice[size];
	        }
	};
	    
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(Address);
		dest.writeString(Name);
		dest.writeInt(Type);
		
		dest.writeInt(Identity);
		dest.writeInt(IdentityPoint);
		
		dest.writeBooleanArray(new boolean[]{
			AutoConnect, Secure, Connected, Discovered	
		});
		
		dest.writeLong(Timestamp);
	}

	@Override
	public int hashCode() {
		return Address.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(Address);
		builder.append(" [ ").append(Name).append(" ] ");
		builder.append("@").append(Timestamp);
		
		return builder.toString();
	}

	
	// *** Getter & Setter *** //
	public final String getAddress() {
		return Address;
	}
	
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public int getType() {
		return Type;
	}

	public boolean isLESupported() {
		return BluetoothDevice.DEVICE_TYPE_LE == (Type & BluetoothDevice.DEVICE_TYPE_LE);
	}
	
	public void setType(int type) {
		Type = type;
	}

	public int getIdentityPoint() {
		return IdentityPoint;
	}

	public void setIdentityPoint(int identityPoint) {
		IdentityPoint = identityPoint;
	}

	public boolean isAutoConnect() {
		return AutoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		AutoConnect = autoConnect;
	}

	public boolean isSecure() {
		return Secure;
	}

	public void setSecure(boolean secure) {
		Secure = secure;
	}

	public long getTimestamp() {
		return Timestamp;
	}

	public void setIdentity(int identity) {
		Identity = identity;
	}
	
	public DeviceAnalyzer.Identity getIdentity() {
		DeviceAnalyzer.Identity[] identities = DeviceAnalyzer.Identity.values();
		if (Identity < 0 || Identity > identities.length)
			return DeviceAnalyzer.Identity.UNKNOWN;
		
		return DeviceAnalyzer.Identity.values()[Identity];
	}

}
