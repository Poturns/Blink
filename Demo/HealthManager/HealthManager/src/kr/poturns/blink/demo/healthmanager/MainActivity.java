package kr.poturns.blink.demo.healthmanager;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * HealthManager 기능 명세
 * 1. 인바디
 *  1) 정보 확인
 *  2) 내 정보 설정(나이 성별)
 * 2. 운동
 *  1) 정보 확인
 * 2. 심박수
 *  1) 정보 확인
 *  2) 심박수 알람 설정
 *  
 * @author mementohora
 *
 */
public class MainActivity extends Activity implements OnClickListener {

	boolean bindService = false;
	BlinkServiceInteraction mBlinkServiceInteraction;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		hideAllSubMenu();
		findViewById(R.id.submenu_inbody).setVisibility(View.VISIBLE);
		
		mBlinkServiceInteraction = ((HealthManagerApplication)getApplicationContext()).getmBlinkServiceInteraction();
	}

	@Override
	protected void onDestroy() {
		mBlinkServiceInteraction.stopService();
		super.onDestroy();
	};
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_setting:
			mBlinkServiceInteraction.openControlActivity();
			break;
			
		default:
			break;
		}
	}
	private void hideAllSubMenu(){
		findViewById(R.id.submenu_inbody).setVisibility(View.GONE);
		findViewById(R.id.submenu_excercise).setVisibility(View.GONE);
		findViewById(R.id.submenu_heartrate).setVisibility(View.GONE);
	}

	public void onClickToMainMenu(View v){
		hideAllSubMenu();
		switch (v.getId()) {
			case R.id.button_inbody_main:
				findViewById(R.id.submenu_inbody).setVisibility(View.VISIBLE);
				break;
				
			case R.id.button_excercise_main:
				findViewById(R.id.submenu_excercise).setVisibility(View.VISIBLE);
				break;
			
			case R.id.button_heart_main:
				findViewById(R.id.submenu_heartrate).setVisibility(View.VISIBLE);
				break;
		}
	}

	public void onClickInbodyMenu(View v){
		switch (v.getId()) {
		case R.id.button_inbody_info:
			Log.i("Demo", "button_inbody_info");
			break;

		case R.id.button_user_info:
			UserInfoDialog dialog = new UserInfoDialog(this);
			dialog.show();
			break;
		default:
			break;
		}
	}
	
	
	public void onClickExcerciseMenu(View v){
		switch (v.getId()) {
		case R.id.button_excercise_info:
			Log.i("Demo", "button_excercise_info");
			break;

		default:
			break;
		}
	}
	
	public void onClickHeartrateMenu(View v){
		switch (v.getId()) {
		case R.id.button_heartrate_info:
			Log.i("Demo", "button_heartrate_info");
			break;
		case R.id.button_heartrate_check:
			Log.i("Demo", "button_heartrate_check");
			break;

		default:
			break;
		}
	}
}
