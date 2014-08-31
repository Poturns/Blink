package kr.poturns.blink.internal;

import java.util.ArrayList;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;
import kr.poturns.blink.internal.comm.IBlinkMessagable;
import android.util.Log;

/**
 * 
 * @author YeonHo.Kim
 * @author Ho.Kwon
 * @since 2014.07.12
 *
 */
public class MessageProcessor {
	
	private final BlinkLocalService OPERATOR_CONTEXT;
	private final ServiceKeeper SERVICE_KEEPER;
	
	private JsonManager mJsonManager;
	private SqliteManager mSqliteManager;
	
	public MessageProcessor(BlinkLocalBaseService context) {
		OPERATOR_CONTEXT = (BlinkLocalService)context;
		SERVICE_KEEPER = ServiceKeeper.getInstance(context);
		
		mJsonManager = new JsonManager();
		//mSqliteManager = new SqliteManager(context);
	}

	
	@Deprecated
	public void acceptJsonData(String json, BlinkDevice deviceX) {
		ArrayList<SystemDatabaseObject> mObjectList = mJsonManager.obtainJsonSystemDatabaseObject(json);
		
		for (SystemDatabaseObject object : mObjectList) {
			
		}
	}
	
	/**
	 * 블루투스 디바이스로부터 수신한 {@link BlinkDevice} 메세지를 처리한다.
	 * 
	 * @param message
	 * @param fromDevice
	 */
	public void acceptBlinkMessage(BlinkMessage message, BlinkDevice fromDevice) {
		String ret = OPERATOR_CONTEXT.receiveMessageFromProcessor(message.getMessage());
		SERVICE_KEEPER.obtainBinder(message.getSourceApplication()).callbackData(message.getCode(), ret);
		
		// EXAMPLE !!
		/*
		BlinkMessage msg = new BlinkMessage.Builder()
							.setSourceDevice(SERVICE_KEEPER.getSelfDevice())
							.setSourceApplication("kr.poturns.example.package_name")
							.setDestinationDevice(null)
							.setDestinationApplication(null)
							.setReliable(false)
							.setType(BlinkMessage.TYPE_REQUEST_FUNCTION)
							.setMessage("{JSON : ...}")
							.build();
		sendBlinkMessageTo(msg, BlinkDevice.load( msg.getDestinationAddress() ));
		*/
	}
	
	/**
	 * 해당 블루투스 디바이스로 {@link BlinkMessage} 메세지를 송신한다.
	 * 
	 * @param message
	 * @param toDevice
	 */
	public void sendBlinkMessageTo(BlinkMessage message, BlinkDevice toDevice) {
		Log.i("test", "sendBlinkMessageTo");
		acceptBlinkMessage(message,null);
		
		
		
		/*
		 * 
		 * SendRemote시에만 발생하고
		 * 여기서 추가 json을 붙여줘야 하나??
		 */
//		SERVICE_KEEPER.sendMessageToDevice(toDevice, message);
	}
	
	private void handleSystemMessage(BlinkMessage message) {
		switch(message.getType()) {
		case BlinkMessage.TYPE_REQUEST_IDENTITY_SYNC:
			//SERVICE_KEEPER.
			break;
			
		}
	}
}
