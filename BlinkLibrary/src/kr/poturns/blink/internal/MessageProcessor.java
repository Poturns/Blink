package kr.poturns.blink.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.SyncDatabaseManager;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;
import kr.poturns.blink.internal.comm.BlinkMessage.Builder;
import kr.poturns.blink.internal.comm.IBlinkMessagable;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
	private boolean Synchronizing = false;
	
	public MessageProcessor(BlinkLocalBaseService context) {
		OPERATOR_CONTEXT = (BlinkLocalService)context;
		SERVICE_KEEPER = ServiceKeeper.getInstance(context);
		
		mJsonManager = new JsonManager();
		//mSqliteManager = new SqliteManager(context);
	}

	

	
	/**@author Ho Kwon
	 * 블루투스 디바이스로부터 수신한 {@link BlinkDevice} 메세지를 처리한다.
	 * BlinkMessage는 시작, 최종 목표, 데이터 종류, 데이터를 정의한 클래스
	 * BlinkDevice는 다음 hop으로 연결하기 위한 정보(현재 Device와 직접 연결된 ConntionThread를 얻기 위한 용도)
	 * @param message
	 * @param fromDevice
	 */
	public void acceptBlinkMessage(BlinkMessage blinkMessage, BlinkDevice fromDevice) {
		Log.d("acceptBlinkMessage", "accept start!!");
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
			//동기화 시작할때 Sync 플래그를 true로, 끝날 때 false로 설정하여 추가 동기화를 막는다.
			if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC){//
				Log.i("Blink", "TYPE_REQUEST_BlinkAppInfo_SYNC");
				setSynchronizing(true);
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
				setSynchronizing(false);
			}
			//동기화 시작할때 Sync 플래그를 true로, 끝날 때 false로 설정하여 추가 동기화를 막는다.
			if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA_SYNC){
				setSynchronizing(true);
				Log.i("acceptBlinkMessage", "TYPE_REQUEST_MEASUREMENTDATA_SYNC");
				builder_success.setType(IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SYNC_SUCCESS);
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(OPERATOR_CONTEXT);
				String jsonRequestMessage = blinkMessage.getMessage();
				Type MeasurementDataType = new TypeToken<ArrayList<MeasurementData>>(){}.getType();
				ArrayList<MeasurementData> ret = new Gson().fromJson(jsonRequestMessage, MeasurementDataType);

				if(BlinkDevice.HOST.getAddress().contentEquals(SERVICE_KEEPER.obtainCurrentCenterDevice().getAddress())){
					builder_success.setMessage(""+syncDatabaseManager.center.insertMeasurementData(ret));
			    }
			    else{
			    	builder_success.setMessage("");
			    }
				
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage, BlinkDevice.load(blinkMessage.getSourceAddress()));
				setSynchronizing(false);
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_FUNCTION){//
				Log.i("acceptBlinkMessage", "TYPE_REQUEST_FUNCTION");
				//call back
				Function function = JsonManager.obtainJsonFunction(blinkMessage.getMessage());
				startFunction(function);
				builder_success.setType(IBlinkMessagable.TYPE_RESPONSE_FUNCTION_SUCCESS);
				builder_success.setMessage("");	
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage, BlinkDevice.load(blinkMessage.getSourceAddress()));
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA){//
				Log.i("acceptBlinkMessage", "TYPE_REQUEST_MEASUREMENTDATA");
				String message = OPERATOR_CONTEXT.receiveMessageFromProcessor(blinkMessage.getMessage());
				builder_success.setMessage(message);
				builder_success.setType(IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS);
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage, BlinkDevice.load(blinkMessage.getSourceAddress()));
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC){//
				 BlinkDevice device = blinkMessage.getMessage(BlinkDevice.class);
		            ServiceKeeper.getInstance(OPERATOR_CONTEXT).handleIdentitySync(device);
		            
			} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_NETWORK_SYNC) {
				JsonArray mJsonArray = new JsonParser().parse(blinkMessage.getMessage()).getAsJsonArray();

				HashSet<BlinkDevice> mHashSet = new HashSet<BlinkDevice>();
				Gson gson = new Gson();
				for (JsonElement element : mJsonArray)
					mHashSet.add(gson.fromJson(element, BlinkDevice.class));

				BlinkDevice device = BlinkDevice.load(blinkMessage.getSourceAddress());
				ServiceKeeper.getInstance(OPERATOR_CONTEXT).handleNetworkSync(mHashSet, device.getGroupID());
				
				ServiceKeeper.getInstance(OPERATOR_CONTEXT).transferSystemSync(device, IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC);
				//BlinkMessage successBlinkMessage = builder_success.build();
				//sendBlinkMessageTo(successBlinkMessage, device);
			}/*
			 * -> 일단 fromdevice로 보내면 되겠네 if (i am main) { BlinkDevice = } else
			 * if (i am wearable) {
			 * 
			 * }
			 */
			/*위 까지는 TYPE_REQUEST에 대한 처리*/
			//Sync 플래그를 false로 변경하여 동기화 요청을 할 수 있도록 한다.
			if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS){
				Log.i("Blink", "TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS");
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(OPERATOR_CONTEXT);
				String jsonResponseMessage = blinkMessage.getMessage();
				ArrayList<BlinkAppInfo>mergedBlinkAppInfo = JsonManager.obtainJsonBlinkAppInfo(jsonResponseMessage);
			      if(BlinkDevice.HOST.getAddress().contentEquals(SERVICE_KEEPER.obtainCurrentCenterDevice().getAddress())){
			    		syncDatabaseManager.center.syncBlinkDatabase(mergedBlinkAppInfo);
			      }
			      else{
			    	  syncDatabaseManager.wearable.syncBlinkDatabase(mergedBlinkAppInfo);
			      }
			      setSynchronizing(false);
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_IDENTITY_SUCCESS){
				
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_FUNCTION_SUCCESS){
				Log.i("acceptBlinkMessage", "TYPE_RESPONSE_FUNCTION_SUCCESS");
				SERVICE_KEEPER.obtainBinder(blinkMessage.getDestinationApplication()).callbackData(blinkMessage.getCode(), blinkMessage.getMessage(), true);
			}
			else if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS){
				Log.i("acceptBlinkMessage", "TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS");
				SERVICE_KEEPER.obtainBinder(blinkMessage.getDestinationApplication()).callbackData(blinkMessage.getCode(), blinkMessage.getMessage(), true);
			}
			//Sync 플래그를 false로 변경하여 동기화 요청을 할 수 있도록 한다.
			else if(blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SYNC_SUCCESS){
				Log.i("acceptBlinkMessage", "TYPE_RESPONSE_MEASUREMENTDATA_SYNC_SUCCESS");
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(OPERATOR_CONTEXT);
				syncDatabaseManager.wearable.syncMeasurementDatabase(SERVICE_KEEPER.obtainCurrentCenterDevice(), Integer.parseInt(blinkMessage.getMessage()));
				setSynchronizing(false);
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
					Log.i("Blink", "TYPE_RESPONSE_FUNCTION_FAIL");
					builder.setType(IBlinkMessagable.TYPE_RESPONSE_FUNCTION_FAIL);
					
				}else if(blinkMessage_type==IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA){
					Log.i("Blink", "TYPE_RESPONSE_MEASUREMENTDATA_FAIL");
					builder.setType(IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_FAIL);
				}else if(blinkMessage_type==IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC){
					//
				}else if(blinkMessage_type==IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC){
					Log.i("Blink", "TYPE_RESPONSE_BlinkAppInfo_SYNC_FAIL");
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
		Log.d("sendBlinkMessageTo", "Send!!");
//<<<<<<< HEAD
		/*1. Destination MAC = null -> Hop:Main, Node:Main
		 *
		* 2.1 Destination Mac != null -> Hop:Main, Node:Wearable (w-m-w)
		* 2.2 Destination Mac != null -> Hop:Wearble, Node:Wearble(w-w)
		 * 
		 * 
		 * 목적지 디바이스가 연결이 되어 있지 않으면 fail로 보냄
		 * 
		 * 
		 *원래 send쪽에서는 처리안하려 했는데 동기화 때문에......  
		 */
		// obtainCurrentCenterDevice => 현재 연결된 네트워크 중 CenterDevice를 가져온다 없을 시 null.
		
		//동기화 중간에 재동기화 요청을 할 수 없도록 리턴
//		if(message.getType()==IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA_SYNC || message.getType()==IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC){
//			if(isSynchronizing())return;
//		}
		
		BlinkDevice centerDevice = null;
		if(SERVICE_KEEPER.obtainCurrentCenterDevice()!=BlinkDevice.HOST){
			Log.d("sendBlinkMessageTo", "i am not center");
			centerDevice = SERVICE_KEEPER.obtainCurrentCenterDevice();
			if(message.getDestinationAddress() == null){ // Hop : Main, Node : Main
				message.setDestinationAddress(centerDevice.getAddress());
				toDevice = centerDevice;
			
			}else{ //Node : Wearable, Hop : 1. Main 2. X(Wearable 1 to 1 Connect)-> 이 경우도 무조건 Center로 보내면 된다.
				
			}
			}
		else{
			Log.d("sendBlinkMessageTo", "i am center");
			//center일 때도 2가지로 나눠 1. Accept에서 send할 때. 2 send할 때 -> Accept에서 걸러야함.
			// 그냥 fail to Send Message 보내야 함. (현재 프레임워크 구조상 Center가 없을 수가 없다.)
		}
	
	
		SERVICE_KEEPER.sendMessageToDevice(toDevice, message);
		Log.d("Blink", "sendBlinkMessage in Message send!");
		//동기화 메시지를 전송했으므로 동기화중으로 설정
//		if(message.getType()==IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA_SYNC || message.getType()==IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC){
//			setSynchronizing(true);
//		}
		
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




	public boolean isSynchronizing() {
	    return Synchronizing;
    }

	public void setSynchronizing(boolean synchronizing) {
	    Synchronizing = synchronizing;
    }
}
