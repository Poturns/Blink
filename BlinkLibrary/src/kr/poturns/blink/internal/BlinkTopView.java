package kr.poturns.blink.internal;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;
import kr.poturns.blink.internal.comm.IBlinkMessagable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 
 * @author Yeonho.Kim
 * 
 */
public class BlinkTopView extends FrameLayout implements OnClickListener{

	private BlinkDevice DEVICE;
	
	public BlinkTopView(BlinkLocalBaseService context, BlinkDevice device) {
		super(context);
		DEVICE = device;
		
		addView(LayoutInflater.from(context).inflate(R.layout.res_blink_system_connection_dialog, null, false));
		
		Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(this);
		Button btn_accept = (Button) findViewById(R.id.btn_accept);
		btn_accept.setOnClickListener(this);
		
		setVisibility(GONE);
	}

	public void show(BlinkDevice device) {
		DEVICE = device;
		
		TextView content = (TextView) findViewById(R.id.content);
		if (device != null)
			content.setText(device.getName() + content.getText());
		
		show();
	}
	
	public void show() {
		setVisibility(VISIBLE);
	}

	public void hide() {
		setVisibility(GONE);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if (DEVICE == null) {
			setVisibility(GONE);
			return;
		}
		
		ServiceKeeper keeper = ServiceKeeper.getInstance((BlinkLocalBaseService) getContext());
		if (id == R.id.btn_accept) {
			BlinkMessage message = new BlinkMessage.Builder()
									.setSourceDevice(BlinkDevice.HOST)
									.setDestinationDevice(DEVICE)
									.setType(IBlinkMessagable.TYPE_ACCEPT_CONNECTION)
									.build();
			keeper.sendMessageToDevice(DEVICE, message);
			keeper.transferSystemSync(DEVICE, IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC);
			
			setVisibility(GONE);
			
		} else if (id == R.id.btn_cancel) {
			keeper.removeConnection(DEVICE);

			setVisibility(GONE);
		}
	}

}
