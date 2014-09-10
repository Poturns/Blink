package kr.poturns.blink.demo.visualizer;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.os.Bundle;

public class GlassActivity extends Activity {

	private BlinkServiceInteraction mInteraction;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.glass_activity);
		
		mInteraction = new BlinkServiceInteraction(this) {
			
			@Override
			public void onServiceFailed() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onServiceDisconnected() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onServiceConnected(IInternalOperationSupport iSupport) {
				// TODO Auto-generated method stub
				
			}
		};
		
		

		if (mInteraction != null)
			mInteraction.startService();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if (mInteraction != null)
			mInteraction.startBroadcastReceiver();
	}
	
	@Override
	protected void onPause() {
		if (mInteraction != null)
			mInteraction.stopBroadcastReceiver();
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		if (mInteraction != null)
			mInteraction.stopService();
		
		super.onDestroy();
	}
}
