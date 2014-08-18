package kr.poturns.blink.internal.comm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.08.19
 *
 */
public abstract class BlinkServiceInteraction implements ServiceConnection, IBlinkEventBroadcast {

	private final Context CONTEXT;
	private final EventBroadcastReceiver EVENT_BR;
	private final IntentFilter FILTER;
	
	public BlinkServiceInteraction(Context context) {
		CONTEXT = context;
		EVENT_BR = new EventBroadcastReceiver();
		FILTER = new IntentFilter();
		
		FILTER.addAction(BROADCAST_DEVICE_DISCOVERED);
		FILTER.addAction(BROADCAST_DEVICE_CONNECTED);
		FILTER.addAction(BROADCAST_DEVICE_DISCONNECTED);
	}
	
	@Override
	public final void onServiceConnected(ComponentName name, IBinder service) {
		CONTEXT.registerReceiver(EVENT_BR, FILTER);
		
		// TODO Auto-generated method stub
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		CONTEXT.unregisterReceiver(EVENT_BR);
		
		// TODO Auto-generated method stub
	}
	
	public final void startBroadcastReceiver() {
		CONTEXT.registerReceiver(EVENT_BR, FILTER);
	}

	public final void stopBroadcastReceiver() {
		CONTEXT.unregisterReceiver(EVENT_BR);
	}
	
	private class EventBroadcastReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			BlinkDevice device = (BlinkDevice) intent.getSerializableExtra(EXTRA_DEVICE);
			
			if (BROADCAST_DEVICE_DISCOVERED.equals(action)) {
				onDeviceDiscovered(device);
				
			} else if (BROADCAST_DEVICE_CONNECTED.equals(action)) {
				onDeviceConnected(device);
				
			} else if (BROADCAST_DEVICE_DISCONNECTED.equals(action)) {
				onDeviceDisconnected(device);
				
			}
		}
	}
	
	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 탐색 수행시, 디바이스가 발견되었을 때 호출된다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceDiscovered(BlinkDevice device) {}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 디바이스가 연결되었을 때 호출된다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceConnected(BlinkDevice device) {}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 디바이스가 해제되었을 때 호출된다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceDisconnected(BlinkDevice device) {}
}
