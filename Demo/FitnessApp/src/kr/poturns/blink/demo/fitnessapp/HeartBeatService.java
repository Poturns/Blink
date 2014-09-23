package kr.poturns.blink.demo.fitnessapp;

import java.util.Random;

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

		@Override
		public void run() {
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
				} else
					progress++;
			}
			Log.d(TAG, "HeartBeat Thread finished");
			return;
		}
	};
}
