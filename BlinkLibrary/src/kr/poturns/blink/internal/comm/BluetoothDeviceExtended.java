package kr.poturns.blink.internal.comm;

import java.io.Serializable;

import kr.poturns.blink.internal.DeviceAnalyzer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * BluetoothDevice
 * 
 * @author Yeonho.Kim
 * @since 2014.07.26
 *
 */
public class BluetoothDeviceExtended implements Parcelable, Serializable {

	// *** CONSTANT DECLARATION *** //
	/**
	 * 
	 */
	private static final long serialVersionUID = 7531799940476707180L;
	
	
	// *** FIELD DECLARATION *** //
	private BluetoothDevice Device;
	public String DeviceAddress;
	private boolean AutoConnect;
	private boolean Secure;
	
	private DeviceAnalyzer.Identity Identity;
	private int IdentityPoint;
	
	public BluetoothDeviceExtended(Parcel parcel) {
		readFromParcel(parcel);
		
		Identity = DeviceAnalyzer.Identity.UNKNOWN;
		IdentityPoint = 0;
	}

	public BluetoothDeviceExtended(BluetoothDevice device) {
		this.Device = device;
		this.DeviceAddress = device.getAddress();

		// - Default Setting
		DeviceAnalyzer mAnalyzer = DeviceAnalyzer.getInstance(null);
		if (mAnalyzer == null) {
			AutoConnect = true;

			Identity = DeviceAnalyzer.Identity.UNKNOWN;
			IdentityPoint = 0;
			
		} else {
			//mAutoConnect = mAnalyzer.getAnalysis(key)
		}
	}
	
	
	public BluetoothDevice getDevice() {
		return Device;
	}
	
	public void setAutoConnect(boolean auto) {
		this.AutoConnect = auto;
	}
	
	public boolean getAutoConnect() {
		return AutoConnect;
	}

	public void setSecure(boolean secure) {
		this.Secure = secure;
	}
	
	public boolean getSecure() {
		return Secure;
	}
	
	public boolean isLESupported() {
		return (Device.getType() & BluetoothDevice.DEVICE_TYPE_LE) == BluetoothDevice.DEVICE_TYPE_LE;
	}
	
	public void recover(BluetoothAdapter adapter) {
		if (Device == null && DeviceAddress != null) {
			try {
				Device = adapter.getRemoteDevice(DeviceAddress);
			} catch (IllegalArgumentException e) {
				Device = null;
			}
		}
	}

	 public static final Parcelable.Creator<BluetoothDeviceExtended> CREATOR = new Parcelable.Creator<BluetoothDeviceExtended>() {
	        public BluetoothDeviceExtended createFromParcel(Parcel in) {
	            return new BluetoothDeviceExtended(in);
	        }
	        
	        public BluetoothDeviceExtended[] newArray( int size ) {
	            return new BluetoothDeviceExtended[size];
	        }
	};
	    
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (dest != null) {
			dest.writeString(DeviceAddress);
			dest.writeBooleanArray(new boolean[]{AutoConnect, Secure});
			dest.writeInt((Identity == null)? 0 : Identity.ordinal());
			dest.writeInt(IdentityPoint);
		}
	}
	
	@Override
	public int hashCode() {
		return DeviceAddress.hashCode();
	}

	public void readFromParcel(Parcel parcel) {
		this.DeviceAddress = parcel.readString();
		
		boolean[] booleanPool = new boolean[2]; 
		parcel.readBooleanArray(booleanPool);
		
		AutoConnect = booleanPool[0];
		Secure = booleanPool[1];
		
		int identity = Math.max(0, Math.min(3, parcel.readInt()));
		Identity = DeviceAnalyzer.Identity.values()[identity];
		IdentityPoint = parcel.readInt();
	}
	
}
