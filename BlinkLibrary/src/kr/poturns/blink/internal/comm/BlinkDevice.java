package kr.poturns.blink.internal.comm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import kr.poturns.blink.internal.DeviceAnalyzer;
import kr.poturns.blink.util.EncryptionUtil;
import kr.poturns.blink.util.FileUtil;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

/**
 * Blink 서비스의 블루투스 디바이스들을 관리하기 위한 기본 객체.
 * 
 * <p>Cache와 System Repository(로컬파일)을 통해 데이터를 관리한다.
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
	
	
	/**
	 * BlinkDevice Cache, System Repository 순으로 해당 디바이스의 주소를 탐색하여 반환한다.
	 * <br>BluetoothDevice에 포함되어있는 부가 데이터들은 기존 객체에 추가/갱신된다.
	 * 
	 * <p>BlinkDevice.{@link #load(String)}에 Address를 매개변수로 넣는다.
	 *  
	 * @param device
	 * @return
	 * @see #load(String)
	 * @see #update(BluetoothDevice)
	 */
	public static BlinkDevice load(BluetoothDevice device) {
		BlinkDevice mDevice = BlinkDevice.load(device.getAddress());
		
		if (mDevice != null)
			return mDevice.update(device);
		return null;
	}
	
	/**
	 * BlinkDevice Cache, System Repository 순으로 해당 주소를 갖는 디바이스를 탐색하여 반환한다.
	 * <br>해당 디바이스 주소는 해쉬암호화되어 Repository의 파일명으로 저장된다.
	 * 
	 * <p>잘못된 주소가 전달되었을 경우, null을 리턴한다.
	 * 
	 * @param address
	 * @return
	 * 
	 * @see FileUtil.EXTERNAL_SYSTEM_DEVICE_REPOSITORY_NAME
	 */
	public static BlinkDevice load(String address) {
		if (!BluetoothAdapter.checkBluetoothAddress(address))
			return null;
		
		if (CACHE_MAP.containsKey(address))
			return CACHE_MAP.get(address);
		
		else {
			BlinkDevice device = null;
			
			// System Repository에서 추출한다.
			File repo = FileUtil.obtainExternalDirectory(FileUtil.EXTERNAL_SYSTEM_DEVICE_REPOSITORY_NAME);
			String hashed = EncryptionUtil.grantHashMessage(address);
			
			for (File file : repo.listFiles()) {
				if (hashed.equals(file.getName())) {
					device = readFromRepository(hashed);
					device.setConnected(false);
					device.setDiscovered(false);
					break;
				}
			}

			// System Repository에  존재하지 않을 경우, Cache에 새로 생성한다.
			if (device == null)
				device = new BlinkDevice(address);
			
			CACHE_MAP.put(address, device);
			return device;
		}
	}

	/**
	 * System Repository에서 해당 파일명의 디바이스 정보를 읽어온다.
	 * 
	 * @param hashedName
	 * @return
	 */
	private static BlinkDevice readFromRepository(final String hashedName) {
		BlinkDevice device = null;
		try {
			FileInputStream fis = new FileInputStream(new File(FileUtil.EXTERNAL_SYSTEM_DEVICE_REPOSITORY_PATH, hashedName));
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			device = (BlinkDevice) ois.readObject();
			
			ois.close();
			fis.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return device;
	}
	
	/**
	 * Cache에서 해당 주소 값의 디바이스 객체를 제거한다.
	 * 
	 * @param address
	 */
	public static void removeDeviceCache(String address) {
		if (CACHE_MAP.containsKey(address)) {
			CACHE_MAP.get(address).writeToRepository();
			CACHE_MAP.remove(address);
		}
	}
	
	/**
	 * Cache에 등록되어있던 정보는 Repository에 저장하고, Cache를 비운다.
	 */
	public static void clearCache() {
		for (BlinkDevice device : CACHE_MAP.values())
			device.writeToRepository();
		
		CACHE_MAP.clear();
	}
	

	// *** FIELD DECLARATION *** //
	private String Address;
	private String Name;
	private int Type;
	private HashSet<String> Uuids = new HashSet<String>();
	
	private int Identity;
	private int IdentityPoint;

	private boolean AutoConnect;
	private boolean SecureConnect;

	private boolean BlinkSupported;
	private boolean Connected;
	private boolean Discovered;

	private long Timestamp;
	
	
	private BlinkDevice(String address) {
		Address = address;
		Name = null;
		Type = BluetoothDevice.DEVICE_TYPE_UNKNOWN;
		
		Identity = DeviceAnalyzer.Identity.UNKNOWN.ordinal();
		IdentityPoint = 0;
		
		AutoConnect = false;
		SecureConnect = true;
		
		BlinkSupported = false;
		Connected = false;
		Discovered = false;
		
		Timestamp = System.currentTimeMillis();
	}
	
	BlinkDevice(Parcel parcel) {	
		readFromParcel(parcel);
	}
	
	void readFromParcel(Parcel parcel) {
		Address = parcel.readString();
		Name = parcel.readString();
		Type = parcel.readInt();
		
		Identity = parcel.readInt();
		IdentityPoint = parcel.readInt();
		
		boolean[] bools = new boolean[5];
		parcel.readBooleanArray(bools);
		
		AutoConnect = bools[0];
		SecureConnect = bools[1];

		BlinkSupported = bools[2];
		Connected = bools[3];
		Discovered = bools[4];
		
		Timestamp = parcel.readLong();
	}

	BlinkDevice(BluetoothDevice device) {
		this(device.getAddress());
		update(device);
	}

	
	/**
	 * 전달받은 Device를 통해 현 디바이스의 정보를 갱신한다.
	 * 
	 * @param device
	 * @return
	 */
	public BlinkDevice update(BluetoothDevice device) {
		String name = device.getName();
		if ((name != null) && (!name.equals(Name)))
			Name = name;
		
		int type = device.getType();
		if (type != BluetoothDevice.DEVICE_TYPE_UNKNOWN)
			Type = type;
		
		ParcelUuid[] uuids = device.getUuids();
		if (uuids != null) {
			for (ParcelUuid parcelUuid : uuids) {
				UUID uuid = parcelUuid.getUuid();
				
				BlinkSupported = BlinkProfile.UUID_BLINK.equals(uuid);
				Uuids.add(uuid.toString());
			}
		}
		
		
		Timestamp = System.currentTimeMillis();
		
		return this;
	}
	
	
	/**
	 * {@link BluetoothDevice}객체를 얻어온다.
	 * @return
	 */
	public BluetoothDevice obtainBluetoothDevice() {
		return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(Address);
	}

	
	/**
	 * System Repository에 해당 디바이스 정보를 기록한다.
	 */
	private void writeToRepository() {
		try {
			String hashedName = EncryptionUtil.grantHashMessage(Address);
			File devFile = new File(FileUtil.EXTERNAL_SYSTEM_DEVICE_REPOSITORY_PATH, hashedName);
			
			if (!devFile.exists())
				devFile.createNewFile();
			
			else if (devFile.lastModified() > Timestamp)
				return;
			
			FileOutputStream fos = new FileOutputStream(devFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeObject(this);
			
			oos.close();
			fos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			AutoConnect, SecureConnect, BlinkSupported, Connected, Discovered 	
		});
		
		dest.writeLong(Timestamp);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[").append(Name).append(",");
		builder.append(Type).append("]");
		builder.append(Address);
		builder.append("@").append(Timestamp);
		
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Address.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		return hashCode() == o.hashCode();
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

	public String[] getUuids() {
		String[] uuids = new String[Uuids.size()];
		Uuids.toArray(uuids);
		return uuids;
	}

	public int getIdentityPoint() {
		return IdentityPoint;
	}

	public void setIdentityPoint(int identityPoint) {
		IdentityPoint = identityPoint;
	}

	public DeviceAnalyzer.Identity getIdentity() {
		DeviceAnalyzer.Identity[] identities = DeviceAnalyzer.Identity.values();
		if (Identity < 0 || Identity > identities.length)
			return DeviceAnalyzer.Identity.UNKNOWN;
		
		return DeviceAnalyzer.Identity.values()[Identity];
	}
	
	public void setIdentity(int identity) {
		Identity = identity;
	}
	
	public boolean isAutoConnect() {
		return AutoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		AutoConnect = autoConnect;
	}

	public boolean isSecureConnect() {
		return SecureConnect;
	}

	public void setSecureConnect(boolean secure) {
		SecureConnect = secure;
	}

	public boolean isBlinkSupported() {
		return BlinkSupported;
	}
	
	public boolean isConnected() {
		return Connected;
	}

	public void setConnected(boolean connected) {
		Connected = connected;
	}

	public boolean isDiscovered() {
		return Discovered;
	}

	public void setDiscovered(boolean discovered) {
		Discovered = discovered;
	}

	public long getTimestamp() {
		return Timestamp;
	}

}
