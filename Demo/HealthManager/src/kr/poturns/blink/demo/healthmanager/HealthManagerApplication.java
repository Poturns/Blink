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
				Toast.makeText(HealthManagerApplication.this, "연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
			}
			//인바디앱으로부터 데이터 받음
			else if(code==RESPONSE_CODE_INBODY_DATA){
				Toast.makeText(HealthManagerApplication.this, "인바디로부터 데이터를 받았습니다.", Toast.LENGTH_SHORT).show();
				Inbody mInbodyDomain = gson.fromJson(data.OutDeviceData,Inbody.class);
				mBlinkServiceInteraction.local.registerMeasurementData(mInbodyDomain);
	        }
        }

	};
	
}