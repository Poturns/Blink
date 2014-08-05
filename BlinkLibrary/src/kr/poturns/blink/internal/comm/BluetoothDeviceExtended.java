package kr.poturns.blink.internal.comm;

import java.io.Serializable;

import kr.poturns.blink.internal.DeviceAnalyzer;
import android.bluetooth.BluetoothDevice;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.07.26
 *
 */
public class BluetoothDeviceExtended implements Serializable {

	// *** CONSTANT DECLARATION *** //
	/**
	 * 
	 */
	private static final long serialVersionUID = 7531799940476707180L;
	
	
	// *** FIELD DECLARATION *** //
	private final BluetoothDevice DEVICE;
	private boolean AutoConnect;
	private boolean Secure;
	
	private DeviceAnalyzer.Identity Identity;
	private int IdentityPoint;
	
	public BluetoothDeviceExtended(BluetoothDevice device) {
		this.DEVICE = device;

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

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	
	public BluetoothDevice getDevice() {
		return DEVICE;
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
		return (DEVICE.getType() & BluetoothDevice.DEVICE_TYPE_LE) == BluetoothDevice.DEVICE_TYPE_LE;
	}
}
