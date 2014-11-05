package kr.poturns.blink.demo.fitnessapp;

import java.util.Random;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.schema.HeartBeat;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
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
public class HeartBeatService extends Service implements
		GoogleApiClient.OnConnectionFailedListener,
		GoogleApiClient.ConnectionCallbacks, OnDataPointListener {
	/** 심장박동수를 측정하는 Thread */
	private Thread mHeartBeatBackgroundThread;
	/** 심장박동수를 측정하기까지 걸리는 시간 (초) */
	static final int HEART_BEAT_COUNT_INTERVAL = 5;
	/** intent action */
	public final static String WIDGET_HEART_BEAT_ACTION = "kr.poturns.blink.demo.fitnessapp.heartbeat";
	/** intent extra value (heartbeat), (Integer) */
	public final static String WIDGET_HEART_BEAT_VALUE = "heartbeat";
	private static final String TAG = HeartBeatService.class.getSimpleName();
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mIInternalOperationSupport;
	/** 디버깅 용도 */
	private boolean DEBUG = true;
	GoogleApiClient mGoogleClient;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "HeartBeat Service started");
		if (mHeartBeatBackgroundThread == null) {
			mHeartBeatBackgroundThread = new HeartBeatActionThread();
			mHeartBeatBackgroundThread.start();
			Log.d(TAG, "HeartBeat Thread started");

			mInteraction = new BlinkServiceInteraction(this) {
				@Override
				public void onServiceConnected(
						IInternalOperationSupport iSupport) {
					HeartBeatService.this.mIInternalOperationSupport = iSupport;
				}
			};
			mInteraction.startService();
			mGoogleClient = new GoogleApiClient.Builder(getApplicationContext())
					.addApi(Fitness.API).addScope(Fitness.SCOPE_BODY_READ)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).build();
			Log.i(TAG, "google play services connecting...");
			mGoogleClient.connect();
		}
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "HeartBeat Service finished");
		if (mHeartBeatBackgroundThread != null) {
			mHeartBeatBackgroundThread.interrupt();
			Log.d(TAG, "HeartBeat Thread finished");
		}
		mInteraction.stopService();

		Fitness.SensorsApi.remove(mGoogleClient, this).setResultCallback(
				new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) {
							Log.i(TAG,
									"google services Sensor Listener was removed!");
						} else {
							Log.i(TAG,
									"google services Sensor Listener was not removed.");
						}
					}
				});
		Log.i(TAG, "google play services disconnecting...");
		mGoogleClient.disconnect();
		super.onDestroy();
	}

	@Override
	public void onDataPoint(DataPoint dataPoint) {
		for (Field field : dataPoint.getDataType().getFields()) {
			Value val = dataPoint.getValue(field);
			Log.i(TAG, "google services Sensor Detected DataPoint field: "
					+ field.getName());
			Log.i(TAG, "google services Sensor Detected DataPoint value: "
					+ val);
			Toast.makeText(
					HeartBeatService.this,
					field.getName() + "(" + dataPoint.getDataType().getName()
							+ "):" + val, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(TAG, "google play services suspended, cause : " + cause);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "google play services connected");
		Fitness.SensorsApi.findDataSources(
				mGoogleClient,
				new DataSourcesRequest.Builder()
						.setDataTypes(DataType.TYPE_HEART_RATE_BPM)
						.setDataSourceTypes(DataSource.TYPE_RAW).build())
				.setResultCallback(new ResultCallback<DataSourcesResult>() {
					@Override
					public void onResult(DataSourcesResult dataSourcesResult) {
						Log.i(TAG, "Result: "
								+ dataSourcesResult.getStatus().toString());
						for (DataSource dataSource : dataSourcesResult
								.getDataSources()) {
							Log.i(TAG,
									"Data source found: "
											+ dataSource.toString());
							Log.i(TAG, "Data Source type: "
									+ dataSource.getDataType().getName());

							if (dataSource.getDataType().equals(
									DataType.TYPE_HEART_RATE_BPM)) {
								Log.i(TAG,
										"Data source for HEART_RATE_BPM found!  Registering.");
								Fitness.SensorsApi
										.add(mGoogleClient,
												new SensorRequest.Builder()
														.setDataSource(
																dataSource)
														.build(),
												HeartBeatService.this);
							}
						}
					}
				});
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "google play services connection failed, error code : "
				+ result.getErrorCode());
	}

	/** 심장박동수를 생성하기위한 Random */
	private Random mRandom = new Random(System.currentTimeMillis());

	/** 심장박동수를 생성한다. 범위는 50-150 */
	int generateHeartBeat() {
		return mRandom.nextInt(100) + 50;
	}

	private class HeartBeatActionThread extends Thread {
		private int progress = 0;
		/** remote device 에 전달 요청 코드 */
		private static final int REQUEST_CODE = 1;
		/** remote app package name */
		private static final String REMOTE_APP_PACKAGE_NAME = "kr.poturns.blink.demo.visualizer";
		private Gson mGson = new GsonBuilder().setPrettyPrinting().create();

		@Override
		public void run() {
			Log.d(TAG, "HeartBeat Thread started internal");
			progress = 0;
			int count = 0;
			Intent intent = new Intent(WIDGET_HEART_BEAT_ACTION);
			while (true) {
				synchronized (this) {
					try {
						this.wait(1000);
					} catch (InterruptedException e) {
						break;
					}
				}
				if (progress == HEART_BEAT_COUNT_INTERVAL) {
					count++;
					int bpm = generateHeartBeat();
					if (DEBUG && count % 3 == 0) {
						// 140 - 160 을 디버그용으로 생성
						bpm = mRandom.nextInt(20) + 140;
					}
					intent.putExtra(WIDGET_HEART_BEAT_VALUE, bpm);
					sendBroadcast(intent);
					Log.d(TAG, "HeartBeat Thread send broadcast, HeartBeat : "
							+ bpm);
					progress = 0;
					recordHeartBeat(bpm);
					sendHeartBeatRemote(bpm);
				} else
					progress++;
			}
			Log.d(TAG, "HeartBeat Thread finished internal");
			return;
		}

		private void recordHeartBeat(int bpm) {
			SQLiteHelper.getInstance(HeartBeatService.this)
					.insertHeartBeat(bpm);
			if (mInteraction != null) {
				mInteraction.local.registerMeasurementData(new HeartBeat(bpm,
						DateTimeUtil.getTimeString()));
			}
		}

		private void sendHeartBeatRemote(int bpm) {
			if (bpm < 1)
				return;
			if (mIInternalOperationSupport != null && mInteraction != null) {
				boolean result = false;
				for (BlinkAppInfo info : mInteraction.local.obtainBlinkAppAll()) {
					if (info.mApp.PackageName.equals(REMOTE_APP_PACKAGE_NAME)) {
						mInteraction.remote.sendMeasurementData(info, mGson
								.toJson(new HeartBeat(bpm, DateTimeUtil
										.getTimeString())), REQUEST_CODE);
						Log.d(TAG, "send HeartBeat : " + bpm + " // to "
								+ REMOTE_APP_PACKAGE_NAME + " // "
								+ info.mDevice.MacAddress);
						result = true;
					}
				}
				if (!result)
					Log.e(TAG, "Could not reach remote device : "
							+ REMOTE_APP_PACKAGE_NAME);
			} else {
				Log.e(TAG, "Blink Service == null");
			}
		}
	};
}
