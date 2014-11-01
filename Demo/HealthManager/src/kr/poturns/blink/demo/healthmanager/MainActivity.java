package kr.poturns.blink.demo.healthmanager;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.schema.HeartBeat;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class MainActivity extends Activity{
	public static String ACTION_REMOTE_LIGHT_ON = "kr.poturns.blink.demo.visualizer.action.lighton";
	public static String ACTION_REMOTE_LIGHT_OFF = "kr.poturns.blink.demo.visualizer.action.lightoff";
	public static String ACTION_TAKE_PICTURE = "kr.poturns.blink.demo.visualizer.action.takepicture";
	
	BlinkServiceInteraction mBlinkServiceInteraction;
	
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_healthmanager_list);
	   
		HealthManagerApplication mHealthManagerApplication = (HealthManagerApplication)getApplication();
		mBlinkServiceInteraction = mHealthManagerApplication.getBlinkServiceInteraction();
	}

	 public void onClick(View v) {
		// TODO Auto-generated method stub
		 Intent intent;
		 switch (v.getId()) {
		case R.id.button_main_indoby:
			intent = new Intent(MainActivity.this, HisotryActivity.class);
			intent.putExtra("HISTORY", HisotryActivity.HISTORY_INBODY);
			startActivity(intent);
			break;
		case R.id.button_main_excercise:
			intent = new Intent(MainActivity.this, HisotryActivity.class);
			intent.putExtra("HISTORY", HisotryActivity.HISTORY_EXERCISE);
			startActivity(intent);
			break;
		case R.id.button_main_heart:
			intent = new Intent(MainActivity.this, HisotryActivity.class);
			intent.putExtra("HISTORY", HisotryActivity.HISTORY_HEART);
			startActivity(intent);
			break;
		case R.id.button_main_lighton:
			mBlinkServiceInteraction.obtainBlinkApp();
			if (mBlinkServiceInteraction != null) {
				for (BlinkAppInfo info : mBlinkServiceInteraction.local.obtainBlinkAppAll()) {
					for(Function function : info.mFunctionList){
						if(function.Action.equals(ACTION_REMOTE_LIGHT_ON)){
							mBlinkServiceInteraction.remote.startFunction(function, HealthManagerApplication.RESPONSE_CODE_LIGHT_ACTION);
						}
					}
				}
			}
			break;
		case R.id.button_main_lightoff:
			mBlinkServiceInteraction.obtainBlinkApp();
			if (mBlinkServiceInteraction != null) {
				for (BlinkAppInfo info : mBlinkServiceInteraction.local.obtainBlinkAppAll()) {
					for(Function function : info.mFunctionList){
						if(function.Action.equals(ACTION_REMOTE_LIGHT_OFF)){
							mBlinkServiceInteraction.remote.startFunction(function, HealthManagerApplication.RESPONSE_CODE_LIGHT_ACTION);
						}
					}
				}
			}
			break;
		case R.id.button_main_takepicture:
			mBlinkServiceInteraction.obtainBlinkApp();
			if (mBlinkServiceInteraction != null) {
				for (BlinkAppInfo info : mBlinkServiceInteraction.local.obtainBlinkAppAll()) {
					for(Function function : info.mFunctionList){
						if(function.Action.equals(ACTION_TAKE_PICTURE)){
							mBlinkServiceInteraction.remote.startFunction(function, HealthManagerApplication.RESPONSE_CODE_TAKE_PICTURE_ACTION);
						}
					}
				}
			}
			break;
		default:
			break;
		}
		 
	}
}
