package kr.poturns.blink.internal;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public class BlinkTopView extends FrameLayout implements OnTouchListener {

	public BlinkTopView(Context context) {
		super(context);

		setOnTouchListener(this);
		
		final Button b = new Button(context);
		b.setFocusable(true);
		b.setOnClickListener(new OnClickListener() {
			int num = 0;
			
			@Override
			public void onClick(View v) {
				b.setText("TEST BUTTON " + "_" + (num++));
			}
		});
		b.setText("TEST BUTTON");
		addView(b);
		
		setVisibility(GONE);
	}
	
	public void show() {
		setVisibility(VISIBLE);
	}
	
	public void hide() {
		setVisibility(GONE);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("onTouch", event.getX() + ", " + event.getY());
		return false;
	}

	
}
