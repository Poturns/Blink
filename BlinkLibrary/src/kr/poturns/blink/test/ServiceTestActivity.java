package kr.poturns.blink.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import kr.poturns.blink.R;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.SyncDatabaseManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.DatabaseMessage;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.schema.Body;
import kr.poturns.blink.schema.Eye;
import kr.poturns.blink.schema.Heart;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceTestActivity extends Activity implements OnClickListener {
	private final String tag = "ServiceTestActivity";
	
	TextView resultView;
	Button button1, button2, button3, button4, button5, button6;
	Button button7, button8, button9, button10, button11, button12;
	Button btn_sendMessage, btn_registerBlinkApp, btn_registerMeasurementData;
	BlinkAppInfo mBlinkAppInfo;
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
				resultView.setText("");
			}

			@Override
			public void onServiceConnected(IInternalOperationSupport support) {
				iSupport = support;
				exampleRegisterBlinkApp();
				exampleRegisterMeasurementDatabase();
			}

			@Override
			public void onDeviceDiscovered(BlinkDevice device) {
				resultView.append("DISCOVERED : " + device.getAddress()
						+ " >> " + device.getName() + "\n"); 
			}

			@Override
			public void onDeviceConnected(BlinkDevice device) {
				Xdevice = device;
				resultView.append("CONNECTED!! " + device.getAddress() + " >> "
						+ device.getName() + "\n");
			}

			@Override
			public void onDeviceDisconnected(BlinkDevice device) {
				resultView.append("DISCONNECTED!! " + device.getAddress()
						+ " >> " + device.getName() + "\n");
			}
			
			@Override
			public void onIdentityChanged(Identity identity) {
				resultView.append("IDENTITY CHANGED!! " + identity.toString() + "\n");
			}
			
			@Override
			public void onDiscoveryStarted() {
				resultView.append("DISCOVERY START\n"); 
			}
			
			@Override
			public void onDiscoveryFinished() {
				resultView.append("DISCOVERY FINISH\n"); 
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
		btn_sendMessage = (Button) findViewById(R.id.btn_sendMessage);
		btn_sendMessage.setOnClickListener(this);
		btn_registerBlinkApp = (Button) findViewById(R.id.btn_registerBlinkApp);
		btn_registerBlinkApp.setOnClickListener(this);
		btn_registerMeasurementData = (Button) findViewById(R.id.btn_registerMeasurementData);
		btn_registerMeasurementData.setOnClickListener(this);
		
		
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
			if (v.getId() == R.id.button1) {
				// iSupport.registerCallback(IInternalEventCallback.Stub.asInterface(eventCallback));
				Toast.makeText(this, "Unimplemented RegisterCallback",
						Toast.LENGTH_SHORT).show();

			} else if (v.getId() == R.id.button2) {
				// iSupport.unregisterCallback(eventCallback);
				Toast.makeText(this, "Unimplemented RegisterCallback",
						Toast.LENGTH_SHORT).show();

			} else if (v.getId() == R.id.button3) {
				iSupport.openControlActivity();

			} else if (v.getId() == R.id.button4) {
				iSupport.startDiscovery(BluetoothDevice.DEVICE_TYPE_CLASSIC);

			} else if (v.getId() == R.id.button5) {
				iSupport.stopDiscovery();

			} else if (v.getId() == R.id.button6) {
				final BlinkDevice[] devices = iSupport
						.obtainCurrentDiscoveryList();

				final ArrayList<String> addresses = new ArrayList<String>();
				for (BlinkDevice device : devices)
					addresses.add(device.getAddress());

				ListView listView = new ListView(this);

				final AlertDialog dialog = new AlertDialog.Builder(this)
						.setTitle("Choose device to connect:")
						.setView(listView).setPositiveButton("Close", null)
						.create();

				listView.setAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, addresses));
				listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

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

			} else if (v.getId() == R.id.button7) {
				//iSupport.sendBlinkMessages(Xdevice, "Hello " + Xdevice.getName());

			} else if (v.getId() == R.id.button8) {

			} else if (v.getId() == R.id.button9) {
				final BlinkDevice[] devices = iSupport.obtainConnectedDeviceList();
				
				
				final ArrayList<String> addresses = new ArrayList<String>();
				if (devices != null)
					for (BlinkDevice device : devices)
						addresses.add(device.getAddress());

				ListView listView = new ListView(this);

				final AlertDialog dialog = new AlertDialog.Builder(this)
						.setTitle("Choose device to disconnect:")
						.setView(listView).setPositiveButton("Close", null)
						.create();

				listView.setAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, addresses));
				listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

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

			} else if (v.getId() == R.id.button10) {
				iSupport.startListeningAsServer();

			} else if (v.getId() == R.id.button11) {
				iSupport.stopListeningAsServer();

			} else if (v.getId() == R.id.button12) {

			} else if (v.getId() == R.id.btn_sendMessage) {
				//SyncMeasurementData
				interaction.sendSyncMessage();
			} else if (v.getId() == R.id.btn_registerBlinkApp) {
				//function
				SyncDatabaseManager sdm = new SyncDatabaseManager(this);
				sdm.queryDevice("Device='SHV-E250L'").queryApp("").queryFunction("");
				List<Function> fl = sdm.getFunctionList();
				Log.i("Blink","function size : "+fl.size());
				if(fl.size()>0){
					interaction.remote.startFunction(fl.get(0), 100);
				}
			} else if (v.getId() == R.id.btn_registerMeasurementData) {
				//remote measurement data
				interaction.remote.obtainMeasurementData(Eye.class, 101);
			}
		} catch (RemoteException e) {

		}
	}

	IInternalEventCallback.Stub eventCallback = new IInternalEventCallback.Stub() {
		@Override
		public void onReceiveData(int arg0, CallbackData arg1)
				throws RemoteException {
			// TODO Auto-generated method stub
			Log.i(tag, "Code : "+arg0);
			Log.i(tag, "InDeviceData : "+arg1.InDeviceData);
			Log.i(tag, "OutDeviceData : "+arg1.OutDeviceData);
			Log.i(tag, "Result : "+arg1.Result);
			Log.i(tag, "ResultDetail : "+arg1.ResultDetail);
			
//			if(arg0==0){
//				if(arg1.InDeviceData!=null){
//					List<Eye> mEyeList = gson.fromJson(arg1.InDeviceData, new TypeToken<List<Eye>>(){}.getType());
//					for(int i=0;i<mEyeList.size();i++){
//						Log.i(tag, "Eye - left_sight : "+mEyeList.get(i).left_sight+" right_sight : "+mEyeList.get(i).right_sight+ " DateTime : "+mEyeList.get(i).DateTime);
//					}
//				}
//				if(arg1.OutDeviceData!=null){
//					List<Eye> mEyeList = gson.fromJson(arg1.OutDeviceData, new TypeToken<List<Eye>>(){}.getType());
//					for(int i=0;i<mEyeList.size();i++){
//						Log.i(tag, "Eye - left_sight : "+mEyeList.get(i).left_sight+" right_sight : "+mEyeList.get(i).right_sight+ " DateTime : "+mEyeList.get(i).DateTime);
//					}
//				}
//			}
//			else if(arg0==1){
//				if(arg1.InDeviceData!=null){
//					List<MeasurementData> mMeasurementList = gson.fromJson(arg1.InDeviceData, new TypeToken<List<MeasurementData>>(){}.getType());
//					Log.i(tag, mMeasurementList.toString());
//				}
//				if(arg1.OutDeviceData!=null){
//					List<MeasurementData> mMeasurementList = gson.fromJson(arg1.OutDeviceData, new TypeToken<List<MeasurementData>>(){}.getType());
//					Log.i(tag, mMeasurementList.toString());
//				}
//			}
		}

	};

	public void run(){
		Log.i(tag, "TestArchive Run!!");
//		exampleShowActivity();
//		exampleRemoteCall();
//		exampleRegisterBlinkApp();
//		exmampleRegisterExternalBlinkApp();
//		exampleObtainBlinkAppAll();
//		exampleObtainMeasurementDataById();
//		exampleObtainBlinkApp();
//		exampleRegisterMeasurementDatabase();
//		exampleObtainMeasurementDatabase();
//		exampleLogAll();
//		exampleStartFuntion();
//		exampleBlinkDatabaseManager();
//		exampleRemoteCall();
//		exampleSyncDatabase();
//		exampleDatabaseMessage();
	}
	
	public void exampleShowActivity() {
		// TODO Auto-generated method stub
		interaction.openControlActivity();
	}
	private void exampleDatabaseMessage() {
	    // TODO Auto-generated method stub
		DatabaseMessage mDatabaseMessage = new DatabaseMessage.Builder()
		.setCondition("Condition1")
		.setDateTimeFrom("DateTimeFrom1")
		.setDateTimeTo("DateTimeTo1")
		.setType(DatabaseMessage.OBTAIN_DATA_BY_CLASS)
		.build();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ret = gson.toJson(mDatabaseMessage);
		Log.i(tag, ret);
		mDatabaseMessage = gson.fromJson(ret, DatabaseMessage.class);
		Log.i(tag, mDatabaseMessage.getCondition());
		Log.i(tag, mDatabaseMessage.getDateTimeFrom());
		Log.i(tag, mDatabaseMessage.getDateTimeTo());
		Log.i(tag, ""+mDatabaseMessage.getType());
    }
	private void exampleSyncDatabase() {
	    // TODO Auto-generated method stub
		SyncDatabaseManager mSyncDatabaseManager = new SyncDatabaseManager(this);
		ArrayList<BlinkAppInfo> BlinkAppInfoList = new ArrayList<BlinkAppInfo>();
		BlinkAppInfo mBlinkAppInfo;
		
		BlinkAppInfoList.clear();
		int testMeasurementId = 1;
		//wearable 테스트케이스 생성
		for(int i=1;i<31;i++){
			mBlinkAppInfo = new BlinkAppInfo();
			mBlinkAppInfo.mDevice.DeviceId = i;
			mBlinkAppInfo.mDevice.Device = "MJ-G2";
			mBlinkAppInfo.mDevice.MacAddress = "58:A2:B5:54:2D:A9"+i;
			
			mBlinkAppInfo.mApp.PackageName = "TestPackageName"+i;
			mBlinkAppInfo.mApp.AppName = "TestAppName"+i;
			mBlinkAppInfo.mApp.AppId = i;
			mBlinkAppInfo.mApp.DeviceId = i;
			
			mBlinkAppInfo.addFunction("TestFunction"+i, "TestFunction"+i,"com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY);
			mBlinkAppInfo.addFunction("TestFunction"+i, "TestFunction"+i,"com.example.servicetestapp.TestActivity",Function.TYPE_SERIVCE);
			mBlinkAppInfo.addFunction("TestFunction"+i, "TestFunction"+i,"com.example.servicetestapp.TestActivity",Function.TYPE_BROADCAST);
			for(int j=0;j<mBlinkAppInfo.mFunctionList.size();j++){
				mBlinkAppInfo.mFunctionList.get(j).AppId = i;
			}
			mBlinkAppInfo.addMeasurement(Eye.class);
			mBlinkAppInfo.addMeasurement(Body.class);
			mBlinkAppInfo.addMeasurement(Heart.class);
			for(int j=0;j<mBlinkAppInfo.mMeasurementList.size();j++){
				mBlinkAppInfo.mMeasurementList.get(j).AppId = i;
				mBlinkAppInfo.mMeasurementList.get(j).MeasurementId = testMeasurementId++;
			}
			
			BlinkAppInfoList.add(mBlinkAppInfo);
		}
		mSyncDatabaseManager.wearable.syncBlinkDatabase(BlinkAppInfoList);
		
//		BlinkAppObjctList.clear();
//		//main 테스트케이스 생성
//		for(int i=1;i<31;i++){
//			mBlinkAppInfo = new BlinkAppInfo();
//			mBlinkAppInfo.mDevice.DeviceId = i;
//			mBlinkAppInfo.mDevice.Device = "MJ-G2";
//			mBlinkAppInfo.mDevice.MacAddress = "58:A2:B5:54:2D:A9";
//			mBlinkAppInfo.mApp.PackageName = "TestPackageName"+i;
//			mBlinkAppInfo.mApp.AppName = "TestAppName"+i;
//			mBlinkAppInfo.addFunction("TestFunction"+i, "TestFunction"+i,"com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY);
//			mBlinkAppInfo.addFunction("TestFunction"+i, "TestFunction"+i,"com.example.servicetestapp.TestActivity",Function.TYPE_SERIVCE);
//			mBlinkAppInfo.addFunction("TestFunction"+i, "TestFunction"+i,"com.example.servicetestapp.TestActivity",Function.TYPE_BROADCAST);
//			mBlinkAppInfo.addMeasurement(Eye.class);
//			mBlinkAppInfo.addMeasurement(Body.class);
//			mBlinkAppInfo.addMeasurement(Heart.class);
//			BlinkAppObjctList.add(mBlinkAppInfo);
//		}
//		mSyncDatabaseManager.main.syncBlinkApp(BlinkAppObjctList);
    }
	private void exampleRemoteCall() {
		// TODO Auto-generated method stub
		interaction.remote.obtainMeasurementData(Eye.class, 0);
		
//		interaction.local.queryDevice("").queryApp("").queryMeasurement("");
//		interaction.remote.obtainMeasurementData(interaction.local.getMeasurementList(), null, null, 1);
	}
	
	/**
	 * BlinkDatabaseManager 사용법을 설명하는 예제
	 */
	private void exampleBlinkDatabaseManager() {
	    // TODO Auto-generated method stub
		interaction.local.queryDevice("");
		List<Device> mDeviceList = interaction.local.getDeviceList();
		interaction.local.queryApp("");
		List<App> mAppList = interaction.local.getAppList();
		interaction.local.queryFunction("");
		List<Function> mFunctionList = interaction.local.getFunctionList();
		interaction.local.queryMeasurement("");
		List<Measurement> mMeasurementList = interaction.local.getMeasurementList();
		interaction.local.queryMeasurementData("");
		List<MeasurementData> mMeasurementDataList = interaction.local.getMeasurementDataList();
		
		Log.i(tag,"Device List :");
		for(int i=0;i<mDeviceList.size();i++){
			Log.i(tag,mDeviceList.get(i).toString());
		}
		Log.i(tag,"App List :");
		for(int i=0;i<mAppList.size();i++){
			Log.i(tag,mAppList.get(i).toString());
		}
		Log.i(tag,"Function List :");
		for(int i=0;i<mFunctionList.size();i++){
			Log.i(tag,mFunctionList.get(i).toString());
		}
		Log.i(tag,"Measurement List :");
		for(int i=0;i<mMeasurementList.size();i++){
			Log.i(tag,mMeasurementList.get(i).toString());
		}
		Log.i(tag,"MeasurementData List :");
		for(int i=0;i<mMeasurementDataList.size();i++){
			Log.i(tag,mMeasurementDataList.get(i).toString());
		}
    }
	/**
	 * Function의 인텐트를 실행하는 예제
	 * Function 클래스를 startFunction 매서드에 매개변수로 넘겨주면 서비스에서 Intent를 날려준다.
	 */
	private void exampleStartFuntion() {
		// TODO Auto-generated method stub
		Log.i(tag, "exampleStartFuntion");
		mBlinkAppInfo = interaction.local.obtainBlinkApp();
		ArrayList<Function> mFunctionList;
		if(mBlinkAppInfo.isExist){
			mFunctionList = mBlinkAppInfo.mFunctionList;
			Log.i(tag, "mFunctionList size : "+mFunctionList.size());
			for(int i=0;i<mFunctionList.size();i++){
				Log.i(tag, mFunctionList.get(i).toString());
				interaction.local.startFunction(mFunctionList.get(i));
			}
		}
	}
	/**
	 * BlinkApp를 등록하는 예제
	 * 먼저 obtainBlinkApp()를 통해 BlinkApp를 Device 이름과 App 패키지명으로
	 * 기존에 등록되어있는 것이 있는지 확인한다.
	 * mBlinkAppInfo.isExist 통해 등록되어있지 않으면 기능과 측정값을 등록하고
	 * mBlinkServiceManager.registerBlinkApp() 호출을 통해 등록한다.
	 * 측정값 등록은 클래스를 넣으면 자동으로 필드에 맞추어 등록을 해준다.
	 */
	public void exampleRegisterBlinkApp(){
		Log.i(tag, "exampleRegisterBlinkApp");
		//BlinkApp 객체 얻기, 기존에 등록되어있으면 등록되어있는 값을 넣어준다.
		mBlinkAppInfo = interaction.local.obtainBlinkApp();
		if(!mBlinkAppInfo.isExist){
			//등록되어있지 않으면 추가적으로 등록할 함수, 측정값을 추가하고 등록한다.
			mBlinkAppInfo.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY);
			mBlinkAppInfo.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_SERIVCE);
			mBlinkAppInfo.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_BROADCAST);
			mBlinkAppInfo.addMeasurement(Eye.class);
			mBlinkAppInfo.addMeasurement(Body.class);
			mBlinkAppInfo.addMeasurement(Heart.class);
			//sqlite에 등록하는 함수
			interaction.registerBlinkApp(mBlinkAppInfo);
		}
	}
	
	public void exmampleRegisterExternalBlinkApp(){
		//외부 디바이스 임시 등록
		mBlinkAppInfo = interaction.local.obtainBlinkApp();
		mBlinkAppInfo.mDevice.Device = "MJ-G2";
		mBlinkAppInfo.mDevice.MacAddress = "58:A2:B5:54:2D:A9";
		mBlinkAppInfo.mApp.PackageName = "TextPackageName";
		mBlinkAppInfo.mApp.AppName = "TextAppName";
		mBlinkAppInfo.mFunctionList.clear();
		mBlinkAppInfo.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY);
		mBlinkAppInfo.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_SERIVCE);
		mBlinkAppInfo.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_BROADCAST);
		mBlinkAppInfo.mMeasurementList.clear();
		mBlinkAppInfo.addMeasurement(Eye.class);
		mBlinkAppInfo.addMeasurement(Body.class);
		mBlinkAppInfo.addMeasurement(Heart.class);
		interaction.registerExternalBlinkApp(mBlinkAppInfo);
	}
	
	/**
	 * 등록되어있는 mBlinkAppInfo를 얻어오는 예제
	 */
	public void exampleObtainBlinkApp(){
		Log.i(tag, "exampleObtainBlinkApp");
		mBlinkAppInfo = interaction.local.obtainBlinkApp();
		if(mBlinkAppInfo.isExist){
			Log.i(tag, "등록된 디바이스와 어플리케이션이 있으면");
			Log.i(tag, "length : "+mBlinkAppInfo.mApp.AppIcon.length);
			/**
			 * 이미지 사용방법
			 * byte[]를 Bitmap으로 변환 후 ImageView에 설정
			 */
		}else {
			Log.i(tag, "등록된 디바이스와 어플리케이션이 없으면");
		}
	}
	
	/**
	 * 등록되어있는 모든 mBlinkAppInfo를 얻어오는 예제
	 */
	public void exampleObtainBlinkAppAll(){
		Log.i(tag, "exampleObtainBlinkAppAll");
		List<BlinkAppInfo> mBlinkAppInfoList = interaction.local.obtainBlinkAppAll();
		BlinkAppInfo BlinkAppInfo = null;
		for(int i=0;i<mBlinkAppInfoList.size();i++){
			BlinkAppInfo = mBlinkAppInfoList.get(i);
			Log.i(tag, i+" sdo :"+BlinkAppInfo.toString());
		}
	}
	
	/**
	 * mBlinkAppInfo의 ID로 얻어오는 예제
	 */
	public void exampleObtainMeasurementDataById(){
		Log.i(tag, "exampleObtainMeasurementDataById");
		List<BlinkAppInfo> mBlinkAppInfoList = interaction.local.obtainBlinkAppAll();
		for(int i=0;i<mBlinkAppInfoList.size();i++){
			List<MeasurementData> mMeasurementDataList = interaction.local.obtainMeasurementData(mBlinkAppInfoList.get(i).mMeasurementList, null, null);
			for(int j=0;j<mMeasurementDataList.size();j++){
				Log.i(tag, "MeasurementData "+j+" \n"+mMeasurementDataList.get(j).toString());
			}
		}
	}
	
	/**
	 * 측정값을 저장하는 예제
	 * 이 예제에서는 Eye, Body, Heart 세 개의 클래스에서
	 * 랜덤하게 100개씩 생성하여 등록하는 예제이다.
	 * mBlinkServiceManager.registerMeasurementData(mBlinkAppInfo,mEye) 에서
	 * mBlinkAppInfo와 해당 객체를 매개변수로 전달하면 등록을 해준다.
	 */
	public void exampleRegisterMeasurementDatabase(){
		Log.i(tag, "exampleRegisterMeasurementDatabase");
//		mBlinkAppInfo = interaction.local.obtainBlinkApp();
		List<BlinkAppInfo> mBlinkAppInfoList = interaction.local.obtainBlinkAppAll();
		for(int j=0;j<mBlinkAppInfoList.size();j++){
			Log.i(tag, mBlinkAppInfoList.get(j).toString());
			mBlinkAppInfo = mBlinkAppInfoList.get(j);
			if(mBlinkAppInfo.isExist){
				try {
					Eye mEye;
					Body mBody;
					Heart mHeart;
					Random random = new Random();
					for(int i=0;i<10;i++){
						mEye = new Eye();
						mEye.left_sight = Math.round(random.nextDouble()*10d)/10d;
						mEye.right_sight = Math.round(random.nextDouble()*10d)/10d;
						interaction.local.registerMeasurementData(mEye);
					}
					for(int i=0;i<10;i++){
						mBody = new Body();
						mBody.height = Math.round(random.nextFloat()*10f)/10f+random.nextInt(50)+140;
						mBody.weight = Math.round(random.nextFloat()*10f)/10f+random.nextInt(50)+40;
						interaction.local.registerMeasurementData(mBody);
					}
					
					for(int i=0;i<10;i++){
						mHeart = new Heart();
						mHeart.beatrate = random.nextInt(20)+60;
						interaction.local.registerMeasurementData(mHeart);
					}
				
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				return;
			}
		}
	}
	
	/**
	 * 측정값을 얻어오는 예제
	 * mBlinkServiceManager.obtainMeasurementData(class); 와 같이 얻고 싶은 데이터의 
	 * 클래스를 넘기면 ArrayList<class> 형식으로 반환해준다.
	 * 반환받는 형식에 맞추어 리턴을 해주기 때문에 대입되는 변수의 타입이 중요하다.
	 * ArrayList<Eye> mEyeList = mBlinkServiceManager.obtainMeasurementData(Eye.class);
	 * 위의 경우 해당 함수는 ArrayList<Eye>로 반환해준다.
	 */
	public void exampleObtainMeasurementDatabase(){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		ArrayList<Eye> mEyeList = interaction.local.obtainMeasurementData(Eye.class,SqliteManager.CONTAIN_FIELD,new TypeToken<ArrayList<Eye>>(){}.getType());
		for(int i=0;i<mEyeList.size();i++){
			Log.i(tag, "Eye - left_sight : "+mEyeList.get(i).left_sight+" right_sight : "+mEyeList.get(i).right_sight+ " DateTime : "+mEyeList.get(i).DateTime);
		}
		ArrayList<Body> mBodyList = interaction.local.obtainMeasurementData(Body.class,new TypeToken<ArrayList<Body>>(){}.getType());
		for(int i=0;i<mBodyList.size();i++){
			Log.i(tag, "Body - height : "+mBodyList.get(i).height+" weight : "+mBodyList.get(i).weight+ " DateTime : "+mBodyList.get(i).DateTime);
		}
		ArrayList<Heart> mHeartList = interaction.local.obtainMeasurementData(Heart.class,new TypeToken<ArrayList<Heart>>(){}.getType());
		for(int i=0;i<mHeartList.size();i++){
			Log.i(tag, "Heart - beatrate : "+mHeartList.get(i).beatrate+" DateTime : "+mHeartList.get(i).DateTime);
		}
	}
	
	
	public void exampleLogAll(){
		Log.i(tag, "exampleLogAll");
		List<BlinkLog> mDeviceAppLogList = interaction.local.obtainLog();
		for(int i=0;i<mDeviceAppLogList.size();i++){
			Log.i(tag, mDeviceAppLogList.get(i).toString());
		}
	}
}
