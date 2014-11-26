package kr.poturns.blink.external;

import kr.poturns.blink.util.FileUtil;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;

/**
 * Service에서 실행 되어, Blink Service와 일부 상호작용하는 {@link android.app.Activity}
 */
public final class ServiceControlActivity extends Activity {
	ServiceControlActivityDelegate mDelegate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FileUtil.createExternalDirectory();
		mDelegate = ServiceControlActivityDelegate.createDelegate(this);
		mDelegate.onCreate(savedInstanceState);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDelegate.onPostCreate(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDelegate.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mDelegate.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		mDelegate.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDelegate.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mDelegate.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDelegate.onDestroy();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return mDelegate.onTouchEvent(ev) || super.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return mDelegate.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
	}

	/** */
	public final IServiceContolActivity getInterface() {
		return mDelegate;
	}

}