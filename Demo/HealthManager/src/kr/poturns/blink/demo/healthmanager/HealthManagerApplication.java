package kr.poturns.blink.demo.healthmanager;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.schema.Inbody;
import android.app.Application;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HealthManagerApplication extends Application {
	public static int RESPONSE_CODE_INBODY_DATA = 0;
	public static int RESPONSE_CODE_LIGHT_ACTION = 1;
	public static int RESPONSE_CODE_TAKE_PICTURE_ACTION = 2;
	
	private BlinkServiceInteraction mBlinkServiceInteraction;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	@Override
	public void onCreate() {
		Log.i("Demo", "HealthManagerApplication onCreate");
		super.onCreate();
		
		mBlinkServiceInteraction = new BlinkServiceInteraction(this, null, mIInternalEventCallback){
			@Override
			public void onServiceConnected(IInternalOperationSupport iSupport) {
				// TODO Auto-generated method stub
				super.onServiceConnected(iSupport);
				BlinkAppInfo mBlinkAppInfo = mBlinkServiceInteraction.obtainBlinkApp();
				if(!mBlinkAppInfo.isExist){
					mBlinkAppInfo.addMeasurement(Inbody.class);
					mBlinkServiceInteraction.registerBlinkApp(mBlinkAppInfo);
				}
			}
		};
		mBlinkServiceInteraction.startService();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		Log.i("Demo", "HealthManagerApplication onTerminate");
		mBlinkServiceInteraction.stopService();
		super.onTerminate();
	}
	public BlinkServiceInteraction getBlinkServiceInteraction() {
		return mBlinkServiceInteraction;
	}

	public void setBlinkServiceInteraction(BlinkServiceInteraction mBlinkServiceInteraction) {
		this.mBlinkServiceInteraction = mBlinkServiceInteraction;
	};
	
	
	IInternalEventCallback.Stub mIInternalEventCallback = new IInternalEventCallback.Stub() {
		@Override
        public void onReceiveData(int code, CallbackData data)
                throws RemoteException {
	        // TODO Auto-generated method stub
			if(data.Result==false){
				Log.i("HealthManager", "인바디로부터 데이터를 받을 수 없습니다.");
			}
			//인바디앱으로부터 데이터 받음
			else if(code==RESPONSE_CODE_INBODY_DATA){
				Log.i("HealthManager", data.OutDeviceData);
				Inbody mInbodyDomain = gson.fromJson(data.OutDeviceData,Inbody.class);
				Log.i("HealthManager", "나이 : "+mInbodyDomain.age);
				mBlinkServiceInteraction.local.registerMeasurementData(mInbodyDomain);
	        }
        }

	};
	
}