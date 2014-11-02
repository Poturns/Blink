package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

/**
 * Watch를 위한 Activity
 * 
 * @author myungjin
 */
public class ServiceControlWatchActivity extends Activity implements
		IServiceContolActivity {
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mBlinkOperation;
	SqliteManagerExtended mSqliteManagerExtended;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.res_blink_activity_service_control_watch);
		transitFragment(0, null);
	}

	@Override
	protected void onDestroy() {
		if (mSqliteManagerExtended != null)
			mSqliteManagerExtended.close();
		super.onDestroy();
	}

	@Override
	public void transitFragment(int position, Bundle arguments) {
		Fragment f;
		switch (position) {
		case 0:
			f = new ConnectionFragment();
			break;
		case 1:
			f = new PreferenceExternalFragment();
			break;
		default:
			return;
		}

		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(android.R.animator.fade_in,
						android.R.animator.fade_out)
				.replace(R.id.res_blink_activity_main_fragment_content, f)
				.commit();
	}

	@Override
	public BlinkServiceInteraction getServiceInteration() {
		return mInteraction;
	}

	@Override
	public void setServiceInteration(BlinkServiceInteraction interaction) {
		mInteraction = interaction;
	}

	@Override
	public void setInternalOperationSupport(
			IInternalOperationSupport blinkOperation) {
		mBlinkOperation = blinkOperation;
	}

	@Override
	public IInternalOperationSupport getInternalOperationSupport() {
		return mBlinkOperation;
	}

	@Override
	public SqliteManagerExtended getDatabaseHandler() {
		return mSqliteManagerExtended;
	}

}
