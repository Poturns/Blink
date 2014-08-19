package kr.poturns.blink.internal.comm;

import kr.poturns.blink.internal.BlinkLocalService;
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
	
	private IBlinkEventBroadcast mBlinkEventBroadcast;
	
	public BlinkServiceInteraction(Context context, IBlinkEventBroadcast iBlinkEventBroadcast) {
		CONTEXT = context;
		EVENT_BR = new EventBroadcastReceiver();
		FILTER = new IntentFilter();
		
		FILTER.addAction(BROADCAST_DEVICE_DISCOVERED);
		FILTER.addAction(BROADCAST_DEVICE_CONNECTED);
		FILTER.addAction(BROADCAST_DEVICE_DISCONNECTED);
		
		mBlinkEventBroadcast = iBlinkEventBroadcast;
	}
	
	public BlinkServiceInteraction(Context context) {
		this(context, null);
	}
	
	@Override
	public final void onServiceConnected(ComponentName name, IBinder service) {
		CONTEXT.registerReceiver(EVENT_BR, FILTER);
		
		if (service == null)
			onServiceFailed();
		else
			onServiceConnected(BlinkSupportBinder.asInterface(service));
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		CONTEXT.unregisterReceiver(EVENT_BR);
		
		onServiceDisconnected();
	}
	
	public final void startService() {
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra(BlinkLocalService.INTENT_EXTRA_SOURCE_PACKAGE, CONTEXT.getPackageName());
		
		CONTEXT.startService(intent);
		CONTEXT.bindService(intent, this, Context.BIND_AUTO_CREATE);
	}
	
	public final void stopService() {
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra(BlinkLocalService.INTENT_EXTRA_SOURCE_PACKAGE, CONTEXT.getPackageName());
		
		CONTEXT.unbindService(this);
		CONTEXT.stopService(intent);
	}
	
	public final void startBroadcastReceiver() {
		CONTEXT.registerReceiver(EVENT_BR, FILTER);
	}

	public final void stopBroadcastReceiver() {
		CONTEXT.unregisterReceiver(EVENT_BR);
	}
	
	public final void setOnBlinkEventBroadcast(IBlinkEventBroadcast iBlinkEventBroadcast) {
		mBlinkEventBroadcast = iBlinkEventBroadcast;
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
	 * <br>Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceDiscovered(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceDiscovered(device);
	}	

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 디바이스가 연결되었을 때 호출된다.
	 * <br>Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceConnected(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceConnected(device);
	}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 디바이스가 해제되었을 때 호출된다.
	 * <br>Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceDisconnected(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceDisconnected(device);

	}
	
	/**
	 * Service에 Binding 되었을 때 호출된다.
	 * 
	 * @param iSupport
	 */
	public abstract void onServiceConnected(IInternalOperationSupport iSupport);
	
	/**
	 * Service에서 Unbinding 되었을 때 호출된다.
	 */
	public abstract void onServiceDisconnected();

	/**
	 * Service에서 Binding이 실패하였을 때 호출된다.
	 */
	public abstract void onServiceFailed();
}
