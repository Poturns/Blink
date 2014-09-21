package kr.poturns.blink.demo.healthmanager;
import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.app.Application;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class HealthManagerApplication extends Application {
	public static int RESPONSE_CODE_INBODY_DATA = 0;
	public static int REPONSE_CODE_HEARTRATE_CHECK = 1;
	
	private BlinkServiceInteraction mBlinkServiceInteraction;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	@Override
	public void onCreate() {
		Log.i("Demo", "HealthManagerApplication onCreate");
		super.onCreate();
		
		mBlinkServiceInteraction = new BlinkServiceInteraction(this, null, mIInternalEventCallback);
		mBlinkServiceInteraction.startService();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		Log.i("Demo", "HealthManagerApplication onTerminate");
		mBlinkServiceInteraction.stopService();
		super.onTerminate();
	}
	public BlinkServiceInteraction getmBlinkServiceInteraction() {
		return mBlinkServiceInteraction;
	}

	public void setmBlinkServiceInteraction(BlinkServiceInteraction mBlinkServiceInteraction) {
		this.mBlinkServiceInteraction = mBlinkServiceInteraction;
	};
	
	IInternalEventCallback mIInternalEventCallback = new IInternalEventCallback() {

		@Override
        public IBinder asBinder() {
	        // TODO Auto-generated method stub
	        return null;
        }

		@Override
        public void onReceiveData(int code, CallbackData data)
                throws RemoteException {
	        // TODO Auto-generated method stub
			if(data.Result==false){
				Toast.makeText(HealthManagerApplication.this, "���ῡ �����߽��ϴ�.", Toast.LENGTH_SHORT).show();
			}
			//�ιٵ�����κ��� ������ ����
			else if(code==RESPONSE_CODE_INBODY_DATA){
				Toast.makeText(HealthManagerApplication.this, "�ιٵ�κ��� �����͸� �޾ҽ��ϴ�.", Toast.LENGTH_SHORT).show();
				MeasurementData receivedData = gson.fromJson(data.OutDeviceData,MeasurementData.class);
				mBlinkServiceInteraction.local.registerMeasurementData(receivedData);
	        }
			//������κ��� �ɹڼ� üũ ��� ���࿡ ���� ������ ����
			else if(code==REPONSE_CODE_HEARTRATE_CHECK){
	        	Toast.makeText(HealthManagerApplication.this, "�ɹڼ�üũ�� ����Ǿ����ϴ�.", Toast.LENGTH_SHORT).show();
	        }
        }

	};
	
	public void obtainData(){
		ArrayList<MeasurementData> MeasurementDataList = mBlinkServiceInteraction.local.obtainMeasurementData(MeasurementData.class,new TypeToken<ArrayList<MeasurementData>>(){}.getType());
	}
	
	public void startHeartrateCheck(){
		//�ɹڼ� üũ ��� ����
		List<Function> mFunctionList = mBlinkServiceInteraction.local.clear().queryApp("AppName='�����̾�'").queryFunction("Function='����̸�'").getFunctionList();
		for(int i=0;i<mFunctionList.size();i++){
			mBlinkServiceInteraction.remote.startFunction(mFunctionList.get(i), HealthManagerApplication.REPONSE_CODE_HEARTRATE_CHECK);
		}
		if(mFunctionList.size()==0)Toast.makeText(this, "�ɹڼ� üũ ����� �����ϴ�.", Toast.LENGTH_SHORT).show();
	}
}
