
package kr.poturns.blink.demo.healthmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class MainActivity extends Activity{
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_healthmanager_list);
	   
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
		case R.id.button_main_record:
			intent = new Intent(MainActivity.this, RecordActivity.class);
		//	intent.putExtra("HISTORY", HisotryActivity.HISTORY_HEART);
			startActivity(intent);
			break;
		default:
			break;
		}
		 
	}
}

