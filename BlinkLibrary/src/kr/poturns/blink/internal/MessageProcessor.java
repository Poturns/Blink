package kr.poturns.blink.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.db.SyncDatabaseManager;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.ServiceKeeper.CONNECTION_STATE;
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
 * {@link BlinkDevice}로 부터 {@link BlinkMessage}를 받아 분석하여 라우팅 해주는 클래스<br>
 * <br>
 * 
 * {@link BlinkMessage}의 {@link BlinkMessage#getType()}으로 얻을 수 있는 Type에 따라 적절한
 * 응답을 보내준다.
 * 
 * @author YeonHo.Kim
 * @author Ho.Kwon
 * @since 2014.07.12
 * 
 */
public class MessageProcessor {

	private final BlinkLocalService OPERATOR_CONTEXT;
	private final ServiceKeeper SERVICE_KEEPER;

	public MessageProcessor(BlinkLocalBaseService context) {
		OPERATOR_CONTEXT = (BlinkLocalService) context;
		SERVICE_KEEPER = ServiceKeeper.getInstance(context);
	}

	/**
	 * 블루투스 디바이스로부터 수신한 {@link BlinkDevice} 메세지를 처리한다. BlinkMessage는 시작, 최종 목표,
	 * 데이터 종류, 데이터를 정의한 클래스 hop으로 연결하기 위한 정보(현재 Device와 직접 연결된 ConntionThread를
	 * 얻기 위한 용도)
	 * 
	 * @author Ho Kwon
	 * @param blinkMessage
	 *            {@link BlinkMessage}
	 * @param fromDevice
	 *            다음 hop으로 연결하기 위한 정보 <br>
	 *            <tr>
	 *            (현재 Device와 직접 연결된 ConntionThread를 얻기 위한 용도)
	 */
	public void acceptBlinkMessage(BlinkMessage blinkMessage,
			BlinkDevice fromDevice) {
		Log.d("acceptBlinkMessage", "accept start!!");
		Log.d("acceptBlinkMessage", "Message target MacAddr="
				+ blinkMessage.getDestinationAddress().toString() + " / type : " + blinkMessage.getType());

		String currentAddress = null;
		BlinkDevice currentDevice = null;

		if (BlinkDevice.HOST == null) {
			Log.e("BlinkMesageProcessor", "There is no CurrentDevice");
		} else {
			currentAddress = BlinkDevice.HOST.getAddress();
			currentDevice = BlinkDevice.HOST;
		}
		if (blinkMessage.getDestinationAddress().equals(currentAddress)) {
			// Message의 최종목적지가 현재 디바이스일때

			// Reponse하기 위한 BlinkMessage의 정보를 세팅한다.
			BlinkMessage.Builder builder_success = new Builder();
			builder_success.setDestinationDevice(BlinkDevice.load(blinkMessage
					.getSourceAddress()));// 이 시점에서 못불러오는 경우는 없는가??
			builder_success.setDestinationApplication(blinkMessage
					.getSourceApplication());
			builder_success.setCode(blinkMessage.getCode());

			int blinkMessage_type = blinkMessage.getType();

			if (blinkMessage_type == IBlinkMessagable.TYPE_ACCEPT_CONNECTION) {
				Log.i("Blink", "TYPE_ACCEPT_CONNECTION");
				ServiceKeeper.getInstance(OPERATOR_CONTEXT).setCurrentState(CONNECTION_STATE.INITIALIZING);
				
				BlinkDevice device = BlinkDevice.load(blinkMessage
						.getSourceAddress());
				// 연결 성립시, 상대의 디바이스로 자신의 BlinkDevice를 넣어 Identity 동기화 요청 메세지를
				// 전송한다.
				SERVICE_KEEPER.transferSystemSync(device,
						IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC);

			} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC) {
				// 동기화 시작할때 Sync 플래그를 true로, 끝날 때 false로 설정하여 추가 동기화를 막는다.
				Log.i("Blink", "TYPE_REQUEST_BlinkAppInfo_SYNC");
				builder_success
						.setType(IBlinkMessagable.TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS);
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(
						OPERATOR_CONTEXT);
				String jsonRequestMessage = blinkMessage.getMessage();
				Type BlinkAppInfoType = new TypeToken<ArrayList<BlinkAppInfo>>() {
				}.getType();
				ArrayList<BlinkAppInfo> ret = new Gson().fromJson(
						jsonRequestMessage, BlinkAppInfoType);
				// 메인 디바이스면 BlinkAppInfo 통합 후 브로드캐스트 실행
				if (BlinkDevice.HOST.getAddress()
						.contentEquals(
								SERVICE_KEEPER.obtainCurrentCenterDevice()
										.getAddress())) {
					syncDatabaseManager.center.syncBlinkDatabase(ret);
					ArrayList<BlinkAppInfo> mergedBlinkAppInfoList = new ArrayList<BlinkAppInfo>();
					mergedBlinkAppInfoList = syncDatabaseManager
							.obtainBlinkApp();
					
					String jsonResponseMessage = JsonManager
							.obtainJsonBlinkAppInfo(mergedBlinkAppInfoList);
					builder_success.setMessage(jsonResponseMessage);
					BlinkMessage successBlinkMessage = builder_success.build();
					sendBroadCast(successBlinkMessage);
				}
				// 웨어러블은 이 요청이 올 일이 없다.
				else {
					// syncDatabaseManager.wearable.syncBlinkDatabase(ret);
				}

			}
			if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA_SYNC) {
				Log.i("acceptBlinkMessage", "TYPE_REQUEST_MEASUREMENTDATA_SYNC");
				builder_success
						.setType(IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SYNC_SUCCESS);
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(
						OPERATOR_CONTEXT);

				String jsonRequestMessage = blinkMessage.getMessage();
				Type MeasurementDataType = new TypeToken<ArrayList<MeasurementData>>() {
				}.getType();
				ArrayList<MeasurementData> ret = new Gson().fromJson(
						jsonRequestMessage, MeasurementDataType);

				if (BlinkDevice.HOST.getAddress()
						.contentEquals(
								SERVICE_KEEPER.obtainCurrentCenterDevice()
										.getAddress())) {
					builder_success.setMessage(""
							+ syncDatabaseManager.center
									.insertMeasurementData(ret));
				} else {
					builder_success.setMessage("");
				}

				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage,
						BlinkDevice.load(blinkMessage.getSourceAddress()));
			} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_FUNCTION) {//
				Log.i("acceptBlinkMessage", "TYPE_REQUEST_FUNCTION");
				// call back
				Function function = JsonManager.obtainJsonFunction(blinkMessage
						.getMessage());
				startFunction(function);
				builder_success
						.setType(IBlinkMessagable.TYPE_RESPONSE_FUNCTION_SUCCESS);
				builder_success.setMessage("");
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage,
						BlinkDevice.load(blinkMessage.getSourceAddress()));
			} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA) {//
				Log.i("acceptBlinkMessage", "TYPE_REQUEST_MEASUREMENTDATA");
				String message = OPERATOR_CONTEXT
						.receiveMessageFromProcessor(blinkMessage.getMessage());
				builder_success.setMessage(message);
				builder_success
						.setType(IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS);
				BlinkMessage successBlinkMessage = builder_success.build();
				sendBlinkMessageTo(successBlinkMessage,
						BlinkDevice.load(blinkMessage.getSourceAddress()));
			} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC) {//
				BlinkDevice device = blinkMessage.getMessage(BlinkDevice.class);
				ServiceKeeper.getInstance(OPERATOR_CONTEXT).handleIdentitySync(
						device);

			} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_NETWORK_SYNC) {
				JsonArray mJsonArray = new JsonParser().parse(
						blinkMessage.getMessage()).getAsJsonArray();
				
				HashSet<BlinkDevice> mHashSet = new HashSet<BlinkDevice>();
				Gson gson = new Gson();
				for (JsonElement element : mJsonArray)
					mHashSet.add(gson.fromJson(element, BlinkDevice.class));

				BlinkDevice device = BlinkDevice.load(blinkMessage
						.getSourceAddress());
				ServiceKeeper.getInstance(OPERATOR_CONTEXT).handleNetworkSync(
						mHashSet, device.getGroupID());

				if(ServiceKeeper.getInstance(OPERATOR_CONTEXT).getCurrnentState()!=CONNECTION_STATE.CONNECTED){
					ServiceKeeper
					.getInstance(OPERATOR_CONTEXT)
					.transferSystemSync(device,
							IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC);
				}

			}/*
			 * -> 일단 fromdevice로 보내면 되겠네 if (i am main) { BlinkDevice = } else
			 * if (i am wearable) {
			 * 
			 * }
			 */
			/* 위 까지는 TYPE_REQUEST에 대한 처리 */
			// Sync 플래그를 false로 변경하여 동기화 요청을 할 수 있도록 한다.
			if (blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS) {
				Log.i("Blink", "TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS");
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(
						OPERATOR_CONTEXT);
				String jsonResponseMessage = blinkMessage.getMessage();
				ArrayList<BlinkAppInfo> mergedBlinkAppInfo = JsonManager
						.obtainJsonBlinkAppInfo(jsonResponseMessage);
				// 메인 디바이스에 이 요청이 올일이 없다.
				if (BlinkDevice.HOST.getAddress()
						.contentEquals(
								SERVICE_KEEPER.obtainCurrentCenterDevice()
										.getAddress())) {
					// syncDatabaseManager.center.syncBlinkDatabase(mergedBlinkAppInfo);
				} else {
					syncDatabaseManager.wearable
							.syncBlinkDatabase(mergedBlinkAppInfo);
				}
				ServiceKeeper.getInstance(OPERATOR_CONTEXT).setCurrentState(CONNECTION_STATE.CONNECTED);
			} else if (blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_IDENTITY_SUCCESS) {

			} else if (blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_FUNCTION_SUCCESS) {
				Log.i("acceptBlinkMessage", "TYPE_RESPONSE_FUNCTION_SUCCESS");
				SERVICE_KEEPER.obtainBinder().callbackData(
						blinkMessage.getCode(), blinkMessage.getMessage(),
						true, blinkMessage.getDestinationApplication());
			} else if (blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS) {
				Log.i("acceptBlinkMessage",
						"TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS");
				if (SERVICE_KEEPER.obtainBinder() == null)
					Log.i("Blink", "binder nul1!!");
				SERVICE_KEEPER.obtainBinder().callbackData(
						blinkMessage.getCode(), blinkMessage.getMessage(),
						true, blinkMessage.getDestinationApplication());
			}
			// Sync 플래그를 false로 변경하여 동기화 요청을 할 수 있도록 한다.
			else if (blinkMessage_type == IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SYNC_SUCCESS) {
				Log.i("acceptBlinkMessage",
						"TYPE_RESPONSE_MEASUREMENTDATA_SYNC_SUCCESS");
				SyncDatabaseManager syncDatabaseManager = new SyncDatabaseManager(
						OPERATOR_CONTEXT);
				syncDatabaseManager.wearable.syncMeasurementDatabase(
						SERVICE_KEEPER.obtainCurrentCenterDevice(),
						Integer.parseInt(blinkMessage.getMessage()));
			}

		} else { // message의 최종 목적지가 현재 디바이스가 아니여서 다른 디바이스로 Pass해야 할 때
			if (BlinkDevice.load(blinkMessage.getDestinationAddress())
					.isConnected()) {
				Log.i("AcceptBlinkMessage", "Toss to OtherDevice");
				sendBlinkMessageTo(blinkMessage,
						BlinkDevice.load(blinkMessage.getDestinationAddress()));
				// Toast.makeText(SERVICE_KEEPER, text, duration)

			} else {
				// 해당 Device와 연결되지 않아서 Pass가 불가능할 때 FAIL Message를 보낸 디바이스쪽으로
				// 보내준다.

				// Set the type_response on type.
				BlinkMessage.Builder builder = new BlinkMessage.Builder();
				int blinkMessage_type = blinkMessage.getType();

				if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_FUNCTION) {
					Log.i("Blink", "TYPE_RESPONSE_FUNCTION_FAIL");
					builder.setType(IBlinkMessagable.TYPE_RESPONSE_FUNCTION_FAIL);

				} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA) {
					Log.i("Blink", "TYPE_RESPONSE_MEASUREMENTDATA_FAIL");
					builder.setType(IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_FAIL);
				} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC) {
					//
				} else if (blinkMessage_type == IBlinkMessagable.TYPE_REQUEST_BlinkAppInfo_SYNC) {
					Log.i("Blink", "TYPE_RESPONSE_BlinkAppInfo_SYNC_FAIL");
					builder.setType(IBlinkMessagable.TYPE_RESPONSE_BlinkAppInfo_SYNC_FAIL);
				}

				builder.setSourceDevice(currentDevice);
				builder.setSourceApplication(blinkMessage
						.getDestinationApplication());

				builder.setDestinationDevice(BlinkDevice.load(blinkMessage
						.getSourceAddress()));
				builder.setDestinationApplication(blinkMessage
						.getSourceApplication());

				String failMessage = "";
				builder.setMessage(failMessage);

				BlinkMessage failBlinkMessage = builder.build();
				sendBlinkMessageTo(failBlinkMessage,
						BlinkDevice.load(blinkMessage.getSourceAddress()));

			}
		}
	}

	/**
	 * 해당 블루투스 디바이스로 {@link BlinkMessage} 메세지를 송신한다.
	 * 
	 * @param message
	 * @param toDevice
	 *            다음 hop으로 연결하기 위한 정보 <br>
	 *            <tr>
	 *            (현재 Device와 직접 연결된 ConntionThread를 얻기 위한 용도)
	 * @author Ho.Kwon
	 */
	public void sendBlinkMessageTo(BlinkMessage message, BlinkDevice toDevice) {

		BlinkDevice centerDevice = null;
		if (SERVICE_KEEPER.obtainCurrentCenterDevice() != BlinkDevice.HOST) {
			Log.d("sendBlinkMessageTo", "i am not center");
			centerDevice = SERVICE_KEEPER.obtainCurrentCenterDevice();
			// Hop : Main, Node : Main
			if (message.getDestinationAddress() == null) {
				message.setDestinationAddress(centerDevice.getAddress());

			} else { // Node : Wearable, Hop : 1. Main 2. X(Wearable 1 to 1
						// Connect)-> 이 경우도 무조건 Center로 보내면 된다.

			}
			toDevice = centerDevice;
		} else {
			Log.d("sendBlinkMessageTo", "i am center");
		}
		Log.d("SendBlinkMessage",
				"targetMac ==" + message.getDestinationAddress());
		Log.d("SendBlinkMessage", "nextMac ==" + toDevice.getAddress());

		SERVICE_KEEPER.sendMessageToDevice(toDevice, message);

	}

	/**
	 * 함수를 실행시켜주는 매소드 바인더나 MessageProcessor로부터 호출된다.
	 * 
	 * @param function
	 * @author Ho.Kwon
	 */
	public void startFunction(Function function) {
		if (function.Type == Function.TYPE_ACTIVITY)
			OPERATOR_CONTEXT.startActivity(new Intent(function.Action)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		else if (function.Type == Function.TYPE_SERIVCE)
			OPERATOR_CONTEXT.startService(new Intent(function.Action));
		else if (function.Type == Function.TYPE_BROADCAST)
			OPERATOR_CONTEXT.sendBroadcast(new Intent(function.Action));
	}

	/**
	 * 현재 디바이스에서 연결된 모든 디바이스들에게 blinkMessage를 broadcast 해준다.
	 * 
	 * @param blinkMessage
	 * @author Ho.Kwon
	 */
	public void sendBroadCast(BlinkMessage blinkMessage) {
		Log.i("sendBlinkMessageTo",
				"sendBroadCast : "
						+ SERVICE_KEEPER.obtainConnectedDevices().length);
		for (int i = 0; i < SERVICE_KEEPER.obtainConnectedDevices().length; i++) {
			BlinkDevice toDevice = SERVICE_KEEPER.obtainConnectedDevices()[i];
			Log.i("sendBlinkMessageTo",
					"sendBroadCastTo : " + toDevice.getName() + "/"
							+ toDevice.getAddress());
			blinkMessage.setDestinationAddress(toDevice.getAddress());
			if (toDevice != SERVICE_KEEPER.obtainCurrentCenterDevice()) {
				sendBlinkMessageTo(blinkMessage, toDevice);
			}
		}
	}
}
