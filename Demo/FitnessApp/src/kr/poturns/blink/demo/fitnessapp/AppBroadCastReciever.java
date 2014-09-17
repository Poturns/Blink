package kr.poturns.blink.demo.fitnessapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppBroadCastReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			// TODO blink app info 등록 삭제
		} else if (action.equals(Intent.ACTION_PACKAGE_FIRST_LAUNCH)) {
			// TODO blink app info 등록
		}
	}

}
