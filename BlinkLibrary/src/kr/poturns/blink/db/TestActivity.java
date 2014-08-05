package kr.poturns.blink.db;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;


public class TestActivity extends Activity {
	private final String tag = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		TestArchive mTestArchive = new TestArchive(this);
		mTestArchive.run();
		
		return true;
	}
	
}
