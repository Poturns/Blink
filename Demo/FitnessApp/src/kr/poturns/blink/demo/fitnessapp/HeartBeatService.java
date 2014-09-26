package kr.poturns.blink.demo.fitnessapp;

import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.schema.HeartBeat;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
public class HeartBeatService extends Service {
	/** 심장박동수를 측정하는 Thread */
	private Thread mHeartBeatBackgroundThread;
	/** 심장박동수를 측정하기까지 걸리는 시간 (초) */
	private static final int HEART_BEAT_COUNT_INTERVAL = 10;
	/** intent action */
	public final static String WIDGET_HEART_BEAT_ACTION = "kr.poturns.blink.demo.fitnessapp.heartbeat";
	/** intent extra value (heartbeat), (Integer) */
	public final static String WIDGET_HEART_BEAT_VALUE = "heartbeat";
	private static final String TAG = HeartBeatService.class.getSimpleName();
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mIInternalOperationSupport;

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
		super.onDestroy();
	}

	/** 심장박동수를 생성하기위한 Random */
	private Random mRandom = new Random(System.currentTimeMillis());

	/** 심장박동수를 생성한다. 범위는 50-150 */
	private int generateHeartBeat() {
		return mRandom.nextInt(20) + mRandom.nextInt(20) + mRandom.nextInt(20)
				+ mRandom.nextInt(20) + mRandom.nextInt(20) + 50;
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
					int bpm = generateHeartBeat();
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
			SQLiteHelper.getInstance(HeartBeatService.this).insert(bpm);
			if (mInteraction != null) {
				mInteraction.local.registerMeasurementData(new HeartBeat(bpm,
						DateTimeUtil.getTimeString()));
			}
		}

		private void sendHeartBeatRemote(int bpm) {
			if(bpm < 1)
				return;
			if (mIInternalOperationSupport != null) {
				for (BlinkAppInfo info : mInteraction.local.obtainBlinkAppAll()) {
					if (info.mApp.PackageName.equals(REMOTE_APP_PACKAGE_NAME)) {
						mInteraction.remote.sendMeasurementData(info, mGson
								.toJson(new HeartBeat(bpm, DateTimeUtil
										.getTimeString())), REQUEST_CODE);
						Log.d(TAG, "send HeartBeat : " + bpm + " // to "
								+ REMOTE_APP_PACKAGE_NAME);
						return;
					}
				}
				Log.e(TAG, "Cannot reach remote device : "
						+ REMOTE_APP_PACKAGE_NAME);
			} else {
				Log.e(TAG, "Blink Service Support == null");
			}
		}
	};
}
