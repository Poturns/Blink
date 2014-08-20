package kr.poturns.blink.test;

import java.util.ArrayList;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceTestActivity extends Activity implements OnClickListener {

	TextView resultView;
	Button button1, button2, button3, button4, button5, button6;
	Button button7, button8, button9, button10, button11, button12;
	
	BlinkServiceInteraction interaction;
	IInternalOperationSupport iSupport;
	BlinkDevice Xdevice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.service_test);
		
		interaction = new BlinkServiceInteraction(this) {
			
			@Override
			public void onServiceFailed() {
				
			}
			
			@Override
			public void onServiceDisconnected() {
				
			}
			
			@Override
			public void onServiceConnected(IInternalOperationSupport support) {
				iSupport = support;
			}
			
			@Override
			public void onDeviceDiscovered(BlinkDevice device) {
				resultView.append("DISCOVERED : " + device.getAddress() + " >> " + device.getName() + "\n"); 
			}
			
			@Override
			public void onDeviceConnected(BlinkDevice device) {
				Xdevice = device;
				resultView.append("CONNECTED!! " + device.getAddress() + " >> " + device.getName() + "\n");
			}
			
			@Override
			public void onDeviceDisconnected(BlinkDevice device) {
				resultView.append("DISCONNECTED!! " + device.getAddress() + " >> " + device.getName() + "\n");
			}
		};
		interaction.startService();
		
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
		button8 = (Button) findViewById(R.id.button8);
		button8.setOnClickListener(this);
		button9 = (Button) findViewById(R.id.button9);
		button9.setOnClickListener(this);
		button10 = (Button) findViewById(R.id.button10);
		button10.setOnClickListener(this);
		button11 = (Button) findViewById(R.id.button11);
		button11.setOnClickListener(this);
		button12 = (Button) findViewById(R.id.button12);
		button12.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		interaction.stopService();
		super.onDestroy();
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
				//iSupport.registerCallback(IInternalEventCallback.Stub.asInterface(eventCallback));
				Toast.makeText(this, "Unimplemented RegisterCallback", Toast.LENGTH_SHORT).show();
				break;
	
			case R.id.button2:
				//iSupport.unregisterCallback(eventCallback);
				Toast.makeText(this, "Unimplemented RegisterCallback", Toast.LENGTH_SHORT).show();
				break;
				
			case R.id.button3:
				iSupport.openControlActivity();
				break;
				
			case R.id.button4:
				iSupport.startDiscovery(BluetoothDevice.DEVICE_TYPE_CLASSIC);
				break;
				
			case R.id.button5:
				iSupport.stopDiscovery();
				break;
				
			case R.id.button6: {
				final BlinkDevice[] devices = iSupport.obtainCurrentDiscoveryList();
				
				final ArrayList<String> addresses = new ArrayList<String>();
				for (BlinkDevice device : devices)
					addresses.add(device.getAddress());
				
				ListView listView = new ListView(this);

				final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Choose device to connect:")
				.setView(listView)
				.setPositiveButton("Close", null)
				.create();
				
				listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addresses));
				listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
						try {
							Xdevice = devices[position];
							iSupport.connectDevice(Xdevice);
							
							dialog.dismiss();
							
						} catch (Exception e) {
							e.printStackTrace();
							Xdevice = null;
						}
					}
				});
				
				dialog.show();
				
			} break;
				
			case R.id.button7:
				iSupport.sendBlinkMessages(Xdevice, "Hello " + Xdevice.getName());
				break;
				
			case R.id.button8:
				break;
				
			case R.id.button9: {
				final BlinkDevice[] devices = iSupport.obtainConnectedDeviceList();
				
				final ArrayList<String> addresses = new ArrayList<String>();
				for (BlinkDevice device : devices)
					addresses.add(device.getAddress());
				
				ListView listView = new ListView(this);

				final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Choose device to disconnect:")
				.setView(listView)
				.setPositiveButton("Close", null)
				.create();
				
				listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addresses));
				listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
						try {
							Xdevice = devices[position];
							iSupport.disconnectDevice(Xdevice);
							
							dialog.dismiss();
							
						} catch (Exception e) {
							e.printStackTrace();
							Xdevice = null;
						}
					}
				});
				
				dialog.show();
				
			} break;
				
			case R.id.button10:
				iSupport.startListeningAsServer();
				break;
				
			case R.id.button11:
				iSupport.stopListeningAsServer();
				break;
				
			case R.id.button12:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
