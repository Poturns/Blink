package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.schema.HeartBeat;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 심장박동수를 측정하는 서비스 <br>
 * <br>
 * 측정한 심장박동수는
 * {@code intent.getIntExtra(HeartBeatService.WIDGET_HEART_BEAT_VALUE)}로 얻을 수
 * 있다.
 * 
 * @author Myungjin.Kim
 * 
 */
public class HeartBeatService extends Service implements SensorEventListener {
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mIInternalOperationSupport;
	private SensorManager mSensorManager;
	private Sensor mHeartbeatSensor;
	private Gson mGson = new GsonBuilder().setPrettyPrinting().create();
	private long mPrevMeasureTime = 0;

	/** intent action */
	public final static String WIDGET_HEART_BEAT_ACTION = "kr.poturns.blink.demo.fitnessapp.heartbeat";
	/** intent extra value (heartbeat), (Integer) */
	public final static String WIDGET_HEART_BEAT_VALUE = "heartbeat";

	private static final String TAG = HeartBeatService.class.getSimpleName();
	/** remote device 에 전달 요청 코드 */
	private static final int REQUEST_CODE = 1;
	/** remote app package name */
	private static final String REMOTE_APP_PACKAGE_NAME = "kr.poturns.blink.demo.visualizer";
	private static final long MEASURE_DIV = 1000 * 1000 * 100;
	/** 측정 주기 - ( 10 ^ -1 sec) */
	private static final long MEASURE_THRESHOLD = 3 * 10;

	/** G Watch R - Heart Rate Monitor(PPG)의 type code */
	private static final int TYPE_HEART_RATE = 33171018;

	// private static final int TYPE_HEART_RATE_RAW = 33171017;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "HeartBeat Service started");
		if (mInteraction == null) {
			mInteraction = new BlinkServiceInteraction(this) {
				@Override
				public void onServiceConnected(
						IInternalOperationSupport iSupport) {
					HeartBeatService.this.mIInternalOperationSupport = iSupport;
				}
			};
			mInteraction.startService();
			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mHeartbeatSensor = mSensorManager.getDefaultSensor(TYPE_HEART_RATE);
			// for (Sensor sensor :
			// mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
			// Log.d(TAG, "Sensor : " + sensor.getName() + ",\n type : "
			// + sensor.getStringType() + "\n" +
			// "code : "+sensor.getType()+"\n\n");
			// }
			mSensorManager.registerListener(this, mHeartbeatSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "HeartBeat Service finished");
		mInteraction.stopService();
		mInteraction.stopBroadcastReceiver();
		mSensorManager.unregisterListener(this, mHeartbeatSensor);
		super.onDestroy();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.accuracy) {
		case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
		case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
		case SensorManager.SENSOR_STATUS_NO_CONTACT:
		case SensorManager.SENSOR_STATUS_UNRELIABLE:
		default:
			return;
		case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
			final int heartRate = (int) (event.values[0]);
			String result = "Sensor value : ( " + heartRate
					+ " ), timestamp : " + event.timestamp + ", accuracy : "
					+ event.accuracy;
			long thisTime = System.nanoTime();
			long diff = (thisTime - mPrevMeasureTime) / MEASURE_DIV;

			Log.d(TAG, "measure diff : " + diff);
			if (diff < MEASURE_THRESHOLD) {
				// Log.d(TAG, result);
			} else {
				Log.i(TAG, result);
				if (heartRate > 0) {
					AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
						@Override
						public void run() {
							sendBroadcast(new Intent(WIDGET_HEART_BEAT_ACTION)
									.putExtra(WIDGET_HEART_BEAT_VALUE,
											heartRate));
							recordHeartBeat(heartRate);
							sendHeartBeatRemote(heartRate);
						}
					});
				}
				mPrevMeasureTime = thisTime;
			}
			return;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private void recordHeartBeat(int bpm) {
		SQLiteHelper.getInstance(this).insertHeartBeat(bpm);
		if (mInteraction != null) {
			mInteraction.local.registerMeasurementData(new HeartBeat(bpm,
					DateTimeUtil.getTimeString()));
		}
	}

	private void sendHeartBeatRemote(int bpm) {
		if (bpm < 1)
			return;
		if (mIInternalOperationSupport != null && mInteraction != null) {
			int count = 0;
			for (BlinkAppInfo info : mInteraction.local.obtainBlinkAppAll()) {
				if (info.mApp.PackageName.equals(REMOTE_APP_PACKAGE_NAME)) {
					mInteraction.remote.sendMeasurementData(info, mGson
							.toJson(new HeartBeat(bpm, DateTimeUtil
									.getTimeString())), REQUEST_CODE);
					Log.d(TAG, "send HeartBeat : " + bpm + " // to "
							+ REMOTE_APP_PACKAGE_NAME + " // "
							+ info.mDevice.MacAddress);
					count++;
				}
			}
			if (count < 1) {
				Log.e(TAG, "Could not reach remote device : "
						+ REMOTE_APP_PACKAGE_NAME);
			} else {
				Log.d(TAG, "send " + count + " device(s)");
			}
		} else {
			Log.e(TAG, "Blink Service == null");
		}
	}
}
