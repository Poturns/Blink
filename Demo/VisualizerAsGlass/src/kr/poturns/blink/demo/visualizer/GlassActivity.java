package kr.poturns.blink.demo.visualizer;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class GlassActivity extends FragmentActivity{

	private BlinkServiceInteraction mInteraction;
	
	private GoogleMap mGoogleMap;
	private ListView mAlertList;
	private GlassAlertAdapter mAlertAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.glass_activity);
		
		initiateComponent();
		
		IInternalEventCallback.Stub mIInternalEventCallback = new IInternalEventCallback.Stub() {
		      
		      @Override
		      public void onReceiveData(int arg0, CallbackData arg1) throws RemoteException {
		    	  if (mAlertAdapter == null)
		    		  return;
		    	  
		    	  // Data 받음..
		    	  String data = arg1.InDeviceData == null? arg1.OutDeviceData : (arg1.InDeviceData + arg1.OutDeviceData);
		    	  mAlertAdapter.pushNewMessage(data);
		      }

			@Override
			public void onDeviceConnected(BlinkDevice arg0) throws RemoteException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDeviceConnectionFailed(BlinkDevice arg0)
					throws RemoteException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDeviceDisconnected(BlinkDevice arg0)
					throws RemoteException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDeviceDiscovered(BlinkDevice arg0)
					throws RemoteException {
				// TODO Auto-generated method stub
				
			}
		   }; 
		
		mInteraction = new BlinkServiceInteraction(this, null, mIInternalEventCallback) {
			
			@Override
			public void onServiceFailed() {
				Toast.makeText(getApplicationContext(), "Failed...", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onServiceDisconnected() {
				Toast.makeText(getApplicationContext(), "Disconnected...", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onServiceConnected(IInternalOperationSupport iSupport) {
				Toast.makeText(getApplicationContext(), "Binder Service Connected!", Toast.LENGTH_SHORT).show();

				BlinkAppInfo mBlinkAppInfo = mInteraction.obtainBlinkApp();
				//TODO: Type은 추가할 수 있는 상수 타입으로.. (C에서 다른 타입명으로 정의하듯..String이지만 타입을 다르게.. )
				mBlinkAppInfo.addMeasurement("Location", "Location_Axis", "String", "Location Axis");
				//TODO: AppInfo에 Function을 등록하더라도 실제 Function을 제공하는 것에 대한 신뢰성 보장이 되지 않음..
				mBlinkAppInfo.addFunction("LightOn", "Turn On the Light", "kr.poturns.blink.demo.visualizer.action.lighton", Function.TYPE_BROADCAST);
				mInteraction.registerBlinkApp(mBlinkAppInfo);
			}
			
			@Override
			public void onDeviceConnected(BlinkDevice device) {
				boolean isDeviceConnected = mInteraction.isDeviceConnected();
				setControlActivityVisibility(!isDeviceConnected);
				setMapVisibility(isDeviceConnected);
			}
			
			@Override
			public void onDeviceDisconnected(BlinkDevice device) {
				boolean isDeviceConnected = mInteraction.isDeviceConnected();
				setControlActivityVisibility(!isDeviceConnected);
				setMapVisibility(isDeviceConnected);
			}
			
		};
		
		if (mInteraction != null) {
			mInteraction.startService();
		}
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();

		if (mInteraction != null) {
			mInteraction.startBroadcastReceiver();
			
			boolean isDeviceConnected = mInteraction.isDeviceConnected();
			setControlActivityVisibility(!isDeviceConnected);
			setMapVisibility(isDeviceConnected);
		}
		
		// TEST
		mAlertAdapter.pushNewMessage("HELLO");
		updateLocation();
	}
	
	@Override
	protected void onPause() {
		if (mInteraction != null)
			mInteraction.stopBroadcastReceiver();
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		if (mInteraction != null)
			mInteraction.stopService();
		
		super.onDestroy();
	}

	private void initiateComponent() {
		SupportMapFragment mGoogleMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.glass_map);
		mGoogleMap = mGoogleMapFragment.getMap();
		mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mGoogleMap.setMyLocationEnabled(true);
		
		//mGoogleMapFragment.getView().setVisibility(View.GONE);
		Button mControllerBtn = (Button) findViewById(R.id.glass_btn_controller);
		mControllerBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mInteraction != null)
					mInteraction.openControlActivity();
			}
		});
		
		mAlertList = (ListView) findViewById(R.id.glass_alertlist);
		mAlertAdapter = new GlassAlertAdapter(this);
		mAlertList.setAdapter(mAlertAdapter);
	}
	
	public void setMapVisibility(boolean enabled) {
		SupportMapFragment mGoogleMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.glass_map);
		mGoogleMapFragment.getView().setVisibility(enabled? View.VISIBLE : View.INVISIBLE);
		
		if (enabled)
			updateLocation();
	}
	
	public void updateLocation() {
		LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		Criteria mCriteria = new Criteria();
		//mCriteria.setAccuracy(Criteria.ACCURACY_HIGH);
		
		String mProvider = mLocationManager.getBestProvider(mCriteria, true);
		mLocationManager.requestLocationUpdates(mProvider, 10000, 10, new LocationListener() {
			
			@Override
			public void onLocationChanged(Location location) {
				LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
				mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13));
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				Log.d("Glass Activity", "onProviderEnabled : " + provider);
			}

			@Override
			public void onProviderDisabled(String provider) {
				Log.d("Glass Activity", "onProviderDisabled : " + provider);
				
			}
		});
	}
	
	private void setControlActivityVisibility(boolean enabled) {

		Button mControllerBtn = (Button) findViewById(R.id.glass_btn_controller);
		mControllerBtn.setVisibility(enabled? View.VISIBLE : View.INVISIBLE);
		mControllerBtn.setClickable(enabled);
	}
}
