package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.demo.fitnessapp.measurement.HeartBeat;
import kr.poturns.blink.demo.fitnessapp.measurement.PushUp;
import kr.poturns.blink.demo.fitnessapp.measurement.SitUp;
import kr.poturns.blink.demo.fitnessapp.measurement.Squat;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppBroadcastReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_PACKAGE_FIRST_LAUNCH)) {
			// TODO blink app info 등록
			final BlinkServiceInteraction interaction = new BlinkServiceInteraction(
					context) {

				@Override
				public void onServiceFailed() {
				}

				@Override
				public void onServiceDisconnected() {
				}

				@Override
				public void onServiceConnected(
						IInternalOperationSupport iSupport) {

				}
			};
			interaction.startService();
			BlinkAppInfo info = interaction.obtainBlinkApp();
			info.addMeasurement(SitUp.class);
			info.mMeasurementList.get(0).Description = "Count of Sit Ups";
			info.addMeasurement(PushUp.class);
			info.mMeasurementList.get(1).Description = "Count of Push Ups";
			info.addMeasurement(Squat.class);
			info.mMeasurementList.get(2).Description = "Count of Squats";
			info.addMeasurement(HeartBeat.class);
			info.mMeasurementList.get(3).Description = "Beat per Minute of HeartBeats";
			interaction.registerBlinkApp(info);
			interaction.stopService();
		} // else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			// TODO blink app info 등록 삭제
		// }
	}

}
