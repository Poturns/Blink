package kr.poturns.blink.internal.comm;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public class BlinkDevice implements Parcelable{

	public BlinkDevice(Parcel parcel) {
		// TODO Auto-generated constructor stub
	}
	
	public BlinkDevice(BluetoothDevice device) {
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}

}
