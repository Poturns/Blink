package kr.poturns.blink.test;

import java.util.ArrayList;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.BlinkLocalService;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ServiceTestActivity extends Activity implements OnClickListener {

	TextView resultView;
	Button button1, button2, button3, button4, button5, button6;
	Button button7, button8, button9, button10, button11, button12;
	
	IInternalOperationSupport iSupport;
	BlinkDevice Xdevice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.service_test);
		
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra("FROM", getPackageName());
		
		startService(intent);
		bindService(intent, new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				iSupport = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				iSupport = IInternalOperationSupport.Stub.asInterface(service);
			}
		}, 0);
		
		
		resultView = (TextView) findViewById(R.id.result_textView);
		
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(this);
		button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(this);
		button3 = (Button) findViewById(R.id.button3);
		button3.setOnClickListener(this);
		button4 = (Button) findViewById(R.id.button4);
		button4.setOnClickListener(this);
		button5 = (Button) findViewById(R.id.button5);
		button5.setOnClickListener(this);
		button6 = (Button) findViewById(R.id.button6);
		button6.setOnClickListener(this);
		button7 = (Button) findViewById(R.id.button7);
		button7.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Log.d("ServiceTestAcitivity", "onClick() : ");
		if (iSupport == null)
			return;
		
		Log.d("ServiceTestAcitivity", "onClick() : ");
		try {
			switch (v.getId()) {
			case R.id.button1:
				iSupport.registerCallback(IInternalEventCallback.Stub.asInterface(eventCallback));
				break;
	
			case R.id.button2:
				iSupport.unregisterCallback(eventCallback);
				break;
				
			case R.id.button3:
				iSupport.disconnectDevice(Xdevice);
				break;
				
			case R.id.button4:
				iSupport.startDiscovery(BluetoothDevice.DEVICE_TYPE_CLASSIC);
				break;
				
			case R.id.button5:
				iSupport.stopDiscovery();
				break;
				
			case R.id.button6:
				final BlinkDevice[] devices = iSupport.obtainCurrentDiscoveryList();
				
				final ArrayList<String> addresses = new ArrayList<String>();
				for (BlinkDevice device : devices)
					addresses.add(device.getAddress());
				
				ListView listView = new ListView(this);
				listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addresses));
				listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
						try {
							Xdevice = devices[position];
							iSupport.connectDevice(Xdevice);
							
						} catch (Exception e) {
							e.printStackTrace();
							Xdevice = null;
						}
					}
				});
				
				new AlertDialog.Builder(this)
				.setTitle("Choose device to connect:")
				.setView(listView)
				.setPositiveButton("Close", null)
				.show();
				
				break;
				
			case R.id.button7:
				iSupport.sendBlinkMessages(Xdevice, "Hello " + Xdevice.getName());
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	IInternalEventCallback.Stub eventCallback = new IInternalEventCallback.Stub() {

		@Override
		public void onDeviceDiscovered(final BlinkDevice deviceX)
				throws RemoteException {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					resultView.append("DISCOVERED : " + deviceX.getAddress() + "\n"); 
				}
				
			});
		}

		@Override
		public void onDeviceConnected(BlinkDevice deviceX)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeviceDisconnected(BlinkDevice deviceX)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeviceConnectionFailed(BlinkDevice deviceX)
				throws RemoteException {
			Xdevice = null;
			
		}
		
	};

}
