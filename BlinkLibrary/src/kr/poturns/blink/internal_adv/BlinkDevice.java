package kr.poturns.blink.internal_adv;

import java.util.HashSet;
import java.util.UUID;

import kr.poturns.blink.internal.comm.BlinkProfile;
import kr.poturns.blink.internal_adv.BlinkResources.BlinkResourceException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;


/**
 * Blink-Engine에서 블루투스 디바이스를 관리하기 위한 기본 객체.
 * 
 * @author Yeonho.Kim
 * @since 2014.08.15
 * @since 2014.10.26
 * 
 */
public final class BlinkDevice {
	
	/******************************************************************
	 	FIELDS
	 ******************************************************************/
	/** */
	// BluetoothDevice-info
	private String Address;
	private String Name;
	private int Type;
	private HashSet<String> UUIDs;
		
	// Device-dependent
	private boolean AutoConnect;
	private boolean SecureConnect;
	private boolean BlinkSupported;

	private volatile boolean Connected;
	private volatile boolean Discovered;

	private long Timestamp;

	
	
	/******************************************************************
    	CONSTRUCTORS
	 ******************************************************************/
	/** */
	BlinkDevice(String address) throws BlinkDeviceException{
		if (address == null || !BluetoothAdapter.checkBluetoothAddress(address))
			throw new BlinkDeviceException(BlinkDeviceException.INAPPOPRIATE_ADDRESS);
		
		Address = address;
		Name = null;
		Type = BluetoothDevice.DEVICE_TYPE_UNKNOWN;
		UUIDs = new HashSet<String>();

		AutoConnect = false;
		SecureConnect = true;

		BlinkSupported = false;
		Connected = false;
		Discovered = false;

		Timestamp = System.currentTimeMillis();
	}
	
	BlinkDevice(BluetoothDevice device) throws BlinkDeviceException{
		this(device.getAddress());
		update(device);
	}

	
	
	/******************************************************************
    	METHODS
	 ******************************************************************/
	/**
	 * 전달받은 BluetoothDevice를 통해 현 BlinkDevice의 정보를 최신화한다.
	 * 
	 * @param device
	 * @return
	 */
	public BlinkDevice update(BluetoothDevice device) {
		String name = device.getName();
		Name = (name == null)? Name : name;

		int type = device.getType();
		Type = (type == BluetoothDevice.DEVICE_TYPE_UNKNOWN)? Type : type;

		ParcelUuid[] uuids = device.getUuids();
		if (uuids != null) {
			BlinkSupported = false;
			UUIDs.clear();
			
			for (ParcelUuid parcelUuid : uuids) {
				UUID uuid = parcelUuid.getUuid();
				UUIDs.add(uuid.toString());

				BlinkSupported = BlinkProfile.UUID_BLINK.equals(uuid);
			}
		}

		Timestamp = System.currentTimeMillis();
		return this;
	}
	
	/**
	 * 매칭되는 {@link BluetoothDevice}객체를 얻어온다.
	 * 
	 * @return
	 */
	public BluetoothDevice asBluetoothDevice() {
		return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(Address);
	}
	
	
	/******************************************************************
    	OVERRIDES
	 ******************************************************************/
	/** */
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[').append(Name).append(']');
		builder.append(Address);
		builder.append('@').append(Timestamp);

		return builder.toString();
	}
	

	
	/******************************************************************
    	GETTER & SETTER
	 ******************************************************************/
	/** */
	public boolean isAutoConnect() {
		return AutoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		AutoConnect = autoConnect;
	}

	public boolean isSecureConnect() {
		return SecureConnect;
	}

	public void setSecureConnect(boolean secureConnect) {
		SecureConnect = secureConnect;
	}

	public String getAddress() {
		return Address;
	}

	public String getName() {
		return Name;
	}
	
	public int getType() {
		return Type;
	}
	
	public String[] getUuids() {
		String[] uuids = new String[UUIDs.size()];
		UUIDs.toArray(uuids);
		return uuids;
	}

	public boolean isBlinkSupported() {
		return BlinkSupported;
	}
	
	public boolean isLeSupported() {
		switch (Type){
		case BluetoothDevice.DEVICE_TYPE_LE:
		case BluetoothDevice.DEVICE_TYPE_DUAL:
			return true;
		default:
			return false;
		}
	}

	public boolean isConnected() {
		return Connected;
	}
	
	void setConnected(boolean connected) {
		Connected = connected;
	}

	public boolean isDiscovered() {
		return Discovered;
	}
	
	void setDiscovered(boolean discovered) {
		Discovered = discovered;
	}

	public long getTimestamp() {
		return Timestamp;
	}

	

	/******************************************************************
    	INNER CLASSES
	 ******************************************************************/
	/**
	 * {@link BlinkDevice}에서 발생할 수 있는 예외 클래스.
	 * 
	 * @author Yeonho.Kim
	 * @since 2014.10.26
	 *
	 */
	public static class BlinkDeviceException extends BlinkResourceException {
		/*-----------------------------------------------------------------
	    	CONSTANTS
		 -----------------------------------------------------------------*/
		private static final long serialVersionUID = -8674488718733041744L;
		
		public static final int INAPPOPRIATE_ADDRESS = 0x10;

		
		/*-----------------------------------------------------------------
	    	CONSTRUCTORS
		 -----------------------------------------------------------------*/
		public BlinkDeviceException(int code) {
			super(code);
		}
		

		/*-----------------------------------------------------------------
	    	OVERRIDES
		 -----------------------------------------------------------------*/
		@Override
		public String getMessage() {
			String errMsg = null;
			
			switch (ExceptionCode) {
			case INAPPOPRIATE_ADDRESS:
				errMsg = "";
				break;
			}
			
			return errMsg;
		}
	}
}
