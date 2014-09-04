package kr.poturns.blink.internal;

import java.util.ArrayList;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;

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
		ArrayList<BlinkAppInfo> mObjectList = mJsonManager.obtainJsonSystemDatabaseObject(json);
		
		for (BlinkAppInfo object : mObjectList) {
			
		}
	}
	
	/**
	 * 블루투스 디바이스로부터 수신한 {@link BlinkDevice} 메세지를 처리한다.
	 * BlinkMessage는 시작, 최종 목표, 데이터를 정의한 클래스
	 * BlinkDevice는 다음 hop으로 연결하기 위한 정보(현재 Device와 직접 연결된 ConntionThread를 얻기 위한 용도)
	 * @param message
	 * @param fromDevice
	 */
	public void acceptBlinkMessage(BlinkMessage message, BlinkDevice fromDevice) {

		//
		if(message.getDestinationAddress().equals(BlinkDevice.HOST.getAddress())){
			
		}else{ // 받은 디바이스가 목적지가 아니므로 타겟 디바이스로 패스해준다. -> wearable 일 경우는?
			BlinkDevice targetBlinkDevice = BlinkDevice.load(message.getDestinationAddress());
			if(targetBlinkDevice.isConnected()){ // 타겟 디바이스와 연결되어있을 경우 pass
				sendBlinkMessageTo(message, targetBlinkDevice);
			}
			else{ // 타겟 디바이스와 연결되어 있지 않은 경우 요청한 디바이스에 fail message를 보낸다.
				//sendBlinkMessageTo(,fromDevice);
			}
		}/*
=======
		String ret = OPERATOR_CONTEXT.receiveMessageFromProcessor(message.getMessage());
		SERVICE_KEEPER.obtainBinder(message.getSourceApplication()).callbackData(message.getCode(), ret);
		
>>>>>>> refs/remotes/origin/service*/
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
//<<<<<<< HEAD
		
		
		/*
		 * main 연결되어있을 시 무조건 main 으로
		 * 없을 시 해당 타겟 디바이스로 곧바로 send
		 * */
		
		
		//toDevice== null 일때 main으로 고.
		
		/* 
		 * Connection 시 동기화 -> 단 main일 때만 MearuementData를 보낸다.
		 * 자신이 wearable일 때 동기화 요청하고 main에서 저장한 후 전체 데이터 보낸다. -> 이 때 MeasruementDAta 총 정보는 Main만 가지고 있다. + 자기데이터
		 * 
		 * 2. SystemDataObject가 변경될 때는 Wearable-Wearable 연결사이에서도 동기화 시켜준다.(MeasurementData 제외)
		 * 3. Function 일 때는 하면되고.
		 * 4. 데이터 요청 -> MeasurementData -> 두 종류 다 그냥 response.
		 * 
		 * SendBlinkMEssage할 때 main이 연결되 있으면 무조건 main으로 보내고. main이 연결안되있으면 Connection 여부 따져서 보내든지 아니면 null return.
		 * 	
		 * 
		 * 
		 * 1.SystemDataObject -> wearable, main 상관없이 동기화.
		 * 2. Mesarementdata -> main이면 저장, wearable 저장안함. call은 가능한데 db에 저장은 안함. 1회용.
		 * 내가 메인인지 아닌지, 타겟이 나인지 아닌지.
		 * toDevice를 지정해주는데
		 * 
		 * Main 연결되있으면 Main으로
		 * 
		 */
		/*
=======
		Log.i("test", "sendBlinkMessageTo");
		acceptBlinkMessage(message,null);
>>>>>>> refs/remotes/origin/service
		*/
		
		
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
