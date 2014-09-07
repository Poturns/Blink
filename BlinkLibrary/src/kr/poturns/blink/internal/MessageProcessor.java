package kr.poturns.blink.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.SyncDatabaseManager;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;
import kr.poturns.blink.internal.comm.BlinkMessage.Builder;
import kr.poturns.blink.internal.comm.IBlinkMessagable;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

	

	
	/**
	 * 블루투스 디바이스로부터 수신한 {@link BlinkDevice} 메세지를 처리한다.
	 * BlinkMessage는 시작, 최종 목표, 데이터를 정의한 클래스
	 * BlinkDevice는 다음 hop으로 연결하기 위한 정보(현재 Device와 직접 연결된 ConntionThread를 얻기 위한 용도)
	 * @param message
	 * @param fromDevice
	 */
	public void acceptBlinkMessage(BlinkMessage blinkMessage, BlinkDevice fromDevice) {
		String currentAddress = null;
		BlinkDevice currentDevice = null;
		
		if(BlinkDevice.HOST==null){
			Log.e("BlinkMesageProcessor", "There is no CurrentDevice");
		}else{
			currentAddress = BlinkDevice.HOST.getAddress();
			currentDevice = BlinkDevice.HOST;
		}
		
		if(blinkMessage.getDestinationAddress().equals(currentAddress)){// Message의 최종 목적지가 현재 디바이스 일 때
			
			BlinkMessage.Builder builder_success = new Builder();
			builder_success.setDestinationDevice(BlinkDevice.load(blinkMessage.getSourceAddress()));// 이 시점에서 못불러오는 경우는 없는가??
			builder_success.setDestinationApplication(blinkMessage.getSourceApplication());
			builder_success.setCode(blinkMessage.getCode());
			int blinkMessage_type = blinkMessage.getType();
			if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC){//
				builder_success.setType(IBlinkMessagable.TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS);
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(OPERATOR_CONTEXT);
				String jsonRequestMessage = blinkMessage.getMessage();
				Type BlinkAppInfoType = new TypeToken<ArrayList<BlinkAppInfo>>(){}.getType();
				ArrayList<BlinkAppInfo> ret = new Gson().fromJson(jsonRequestMessage, BlinkAppInfoType);
				//if(i am main){}
				//else{}
			      if(BlinkDevice.HOST.getAddress().contentEquals(SERVICE_KEEPER.obtainCurrentCenterDevice().getAddress())){
			    		syncDatabaseManager.center.syncBlinkDatabase(ret);
			      }
			      else{
			    	  syncDatabaseManager.wearable.syncBlinkDatabase(ret);
			      }
				
				
			
				ArrayList<BlinkAppInfo> mergedBlinkAppInfoList = new ArrayList<BlinkAppInfo>();
				mergedBlinkAppInfoList = syncDatabaseManager.obtainBlinkApp();
				String jsonResponseMessage = mJsonManager.obtainJsonBlinkAppInfo(mergedBlinkAppInfoList);
				builder_success.setMessage(jsonResponseMessage);
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage, BlinkDevice.load(blinkMessage.getSourceAddress()));
				
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_FUNCTION){//
				//call back
				Function function = JsonManager.obtainJsonFunction(blinkMessage.getMessage());
				startFunction(function);
				builder_success.setType(IBlinkMessagable.TYPE_RESPONSE_FUNCTION_SUCCESS);
				builder_success.setMessage("");	
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage, BlinkDevice.load(blinkMessage.getSourceAddress()));
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA){//
				String message = OPERATOR_CONTEXT.receiveMessageFromProcessor(blinkMessage.getMessage());
				builder_success.setMessage(message);
				builder_success.setType(IBlinkMessagable.TYPE_RESPONSE_FUNCTION_SUCCESS);
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage, BlinkDevice.load(blinkMessage.getSourceAddress()));
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC){//
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage, BlinkDevice.load(blinkMessage.getSourceAddress()));
				//연호꺼 메서드 호출.
			}/* -> 일단 fromdevice로 보내면 되겠네
			if (i am main) {
			BlinkDevice = 
			}
			else if (i am wearable)
			{
				
			}*/
			BlinkMessage responseMessage = builder_success.build();
			sendBlinkMessageTo(responseMessage, fromDevice);
			/*위 까지는 TYPE_REQUEST에 대한 처리*/
			
			if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS){
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(OPERATOR_CONTEXT);
				String jsonResponseMessage = blinkMessage.getMessage();
				ArrayList<BlinkAppInfo>mergedBlinkAppInfo = JsonManager.obtainJsonBlinkAppInfo(jsonResponseMessage);
			      if(BlinkDevice.HOST.getAddress().contentEquals(SERVICE_KEEPER.obtainCurrentCenterDevice().getAddress())){
			    		syncDatabaseManager.center.syncBlinkDatabase(mergedBlinkAppInfo);
			      }
			      else{
			    	  syncDatabaseManager.wearable.syncBlinkDatabase(mergedBlinkAppInfo);
			      }
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_IDENTITY_SUCCESS){
				
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_FUNCTION_SUCCESS){
				
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS){
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(null);
				String jsonResponseMessage = blinkMessage.getMessage();
				ArrayList<Measurement>mergedMesarement = JsonManager.obtainJsonMeasurement(jsonResponseMessage);	
				//syncDatabaseManager.
			}
		
		}
		else{ // message의 최종 목적지가 현재 디바이스가 아니여서 다른 디바이스로 Pass해야 할 때
			if(BlinkDevice.load(blinkMessage.getDestinationAddress()).isConnected()){
				
			}
			else{ // 해당 Device와 연결되지 않아서 Pass가 불가능할 때 FAIL Message를 보낸 디바이스쪽으로 보내준다.
				  // 차후 : 현재는 그룹 간 통신을 고려하지 않아 Source Device가 Main에서 한 단계만 떨어져있다는 가정하에 보내준다. 만약
				  //      그룹별 연결되어있을 시에는 해당 Source로 향하는 next hop을 검색해서 BlinkDevice를 설정한다던지의 처리가 필요함.
				BlinkMessage.Builder builder = new BlinkMessage.Builder(); // Set the type_response on type.
				int blinkMessage_type = blinkMessage.getType();
				
				if(blinkMessage_type==IBlinkMessagable.TYPE_REQUEST_FUNCTION){
					builder.setType(IBlinkMessagable.TYPE_RESPONSE_FUNCTION_FAIL);
					
				}else if(blinkMessage_type==IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA){
					builder.setType(IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_FAIL);
				}else if(blinkMessage_type==IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC){
					//
				}else if(blinkMessage_type==IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC){
					builder.setType(IBlinkMessagable.TYPE_RESPONSE_BlinkAppInfo_SYNC_FAIL);
				}
				builder.setSourceDevice(currentDevice);
				builder.setSourceApplication(blinkMessage.getDestinationApplication());
				
				builder.setDestinationDevice(BlinkDevice.load(blinkMessage.getSourceAddress()));
				builder.setDestinationApplication(blinkMessage.getSourceApplication());
				
				String failMessage = "";
				builder.setMessage(failMessage);
				
				BlinkMessage failBlinkMessage = builder.build();
				sendBlinkMessageTo(failBlinkMessage, BlinkDevice.load(blinkMessage.getSourceAddress()));
				
				
				
				
			}
		}
	}
		
		/*
		if(blinkMessage.getDestinationAddress().equals(BlinkDevice.HOST.getAddress())){
			
		}else{ // 받은 디바이스가 목적지가 아니므로 타겟 디바이스로 패스해준다. -> wearable 일 경우는?
			BlinkDevice targetBlinkDevice = BlinkDevice.load(blinkMessage.getDestinationAddress());
			if(targetBlinkDevice.isConnected()){ // 타겟 디바이스와 연결되어있을 경우 해당 device로 pass
				sendBlinkMessageTo(blinkMessage, targetBlinkDevice);
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
	
	
	/**
	 * 해당 블루투스 디바이스로 {@link BlinkMessage} 메세지를 송신한다.
	 * 
	 * @param message
	 * @param toDevice
	 */
	public void sendBlinkMessageTo(BlinkMessage message, BlinkDevice toDevice) {
//<<<<<<< HEAD
		/*1. Destination MAC = null -> Hop:Main, Node:Main
		 *
		* 2.1 Destination Mac != null -> Hop:Main, Node:Wearable (w-m-w)
		* 2.2 Destination Mac != null -> Hop:Wearble, Node:Wearble(w-w)
		 * 
		 *원래 send쪽에서는 처리안하려 했는데 동기화 때문에......  
		 */
		// obtainCurrentCenterDevice => 현재 연결된 네트워크 중 CenterDevice를 가져온다 없을 시 null.
		BlinkDevice centerDevice = null;
		if(SERVICE_KEEPER.obtainCurrentCenterDevice()!=null){
		centerDevice = SERVICE_KEEPER.obtainCurrentCenterDevice();}
		else{
			// 그냥 fail to Send Message 보내야 함. (현재 프레임워크 구조상 Center가 없을 수가 없다.)
		}
		
		if(message.getDestinationAddress() == null){ // Hop : Main, Node : Main
			message.setSourceAddress(centerDevice.getAddress());
		}else{ //Node : Wearable, Hop : 1. Main 2. X(Wearable 1 to 1 Connect)-> 이 경우도 무조건 Center로 보내면 된다.
			
		}
		
		if(SERVICE_KEEPER.obtainCurrentCenterDevice()!= null){
			sendBlinkMessageTo(message, centerDevice);
			// 스마트폰과 연결된 경우 -> toDevice = main
		}else{ // 스마트폰과 연결되지 않은 경우 -> toDevice = 연결 원하는 다바이스
				
		}
		/*
		 * main 연결되어있을 시 무조건 main 으로
		 * 없을 시 해당 타겟 디바이스로 곧바로 send
		 * */
		
		
		//toDevice== null 일때 main으로 고.
		
		/* 
		 * Connection 시 동기화 -> 단 main일 때만 MearuementData를 보낸다.
		 * 자신이 wearable일 때 동기화 요청하고 main에서 저장한 후 전체 데이터 보낸다. -> 이 때 MeasruementDAta 총 정보는 Main만 가지고 있다. + 자기데이터
		 * 
		 * 2. BlinkAppInfo가 변경될 때는 Wearable-Wearable 연결사이에서도 동기화 시켜준다.(MeasurementData 제외)
		 * 3. Function 일 때는 하면되고.
		 * 4. 데이터 요청 -> MeasurementData -> 두 종류 다 그냥 response.
		 * 
		 * SendBlinkMEssage할 때 main이 연결되 있으면 무조건 main으로 보내고. main이 연결안되있으면 Connection 여부 따져서 보내든지 아니면 null return.
		 * 	
		 * 
		 * 
		 * 1.BlinkAppInfo -> wearable, main 상관없이 동기화.
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
		SERVICE_KEEPER.sendMessageToDevice(toDevice, message);
	}
	
	private void handleSystemMessage(BlinkMessage message) {
		switch(message.getType()) {
		case BlinkMessage.TYPE_REQUEST_IDENTITY_SYNC:
			//SERVICE_KEEPER.
			break;
			
		}
	}
	/**
	 * 함수를 실행시켜주는 매소드
	 * 바인더나 MessageProcessor로부터 호출된다.
	 * @param function
	 */
	public void startFunction(Function function){
		if(function .Type==Function.TYPE_ACTIVITY)
			OPERATOR_CONTEXT.startActivity(new Intent(function.Action).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		else if(function .Type==Function.TYPE_SERIVCE)
			OPERATOR_CONTEXT.startService(new Intent(function.Action));
		else if(function.Type==Function.TYPE_BROADCAST)
			OPERATOR_CONTEXT.sendBroadcast(new Intent(function.Action));
	}
}
