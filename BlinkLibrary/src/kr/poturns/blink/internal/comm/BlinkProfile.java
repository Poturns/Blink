package kr.poturns.blink.internal.comm;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothProfile;

public class BlinkProfile implements BluetoothProfile {

	public static final UUID UUID_BLINK = UUID.fromString("b7121e00-cc1d-1704-c825-0002a5d5c51b");
	
	
	/*
e4de5780-1cc1-11e4-adf7-0002a5d5c51b

f9d96f80-1cc1-11e4-9ac9-0002a5d5c51b

026ba640-1cc2-11e4-87d3-0002a5d5c51b

098805e0-1cc2-11e4-9ac9-0002a5d5c51b

106a69c0-1cc2-11e4-8f69-0002a5d5c51b

16b74460-1cc2-11e4-9bd3-0002a5d5c51b

1ca27700-1cc2-11e4-b752-0002a5d5c51b

242fc0e0-1cc2-11e4-b25a-0002a5d5c51b

2ae2d760-1cc2-11e4-b4c6-0002a5d5c51b

30940e40-1cc2-11e4-8b05-0002a5d5c51b

37b80f00-1cc2-11e4-b92a-0002a5d5c51b

4383c5e0-1cc2-11e4-9d21-0002a5d5c51b

4b543340-1cc2-11e4-8028-0002a5d5c51b

505469a0-1cc2-11e4-96d8-0002a5d5c51b

56e5ee60-1cc2-11e4-aeba-0002a5d5c51b

5d220020-1cc2-11e4-83b6-0002a5d5c51b
	 */
	
	
	@Override
	public List<BluetoothDevice> getConnectedDevices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConnectionState(BluetoothDevice device) {
		// TODO Auto-generated method stub
		return 0;
	}

}
