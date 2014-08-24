package kr.poturns.blink.internal.comm;

import java.io.Serializable;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public class BlinkMessage implements Serializable {

	// *** CONSTANT DECLARATION *** //
	/**
	 * 
	 */
	private static final long serialVersionUID = 7552162163627874820L;

	
	public static final int TYPE_REQUEST_FUNCTION = 0x1;
	
	public static final int TYPE_REQUEST_MEASUREMENT = 0x2;
	
	
	

	// *** FIELD DECLARATION *** //
	private String SourceAddress;
	private String SourceApplication;
	private String DestinationAddress;
	private String DestinationApplication;
	
	private int Type;
	private boolean Reliable;
	private long Timestamp;
	
	private String message;
	
	private BlinkMessage() {
		
		Type = 0;
		Reliable = false;
		Timestamp = 0;
	}

	/**
	 * BLE에서 사용할 수 있는 Message로 변환한다.
	 * @return
	 */
	public Object toLeMessage() {
		return null;
	}

	
	
	// *** BUILDER DECLARATION *** //
	/**
	 * 
	 * @author Yeonho.Kim
	 *
	 */
	public static class Builder {

		private BlinkMessage mBlinkMessage;
		
		public Builder() {
			mBlinkMessage = new BlinkMessage();
		}
		
		/**
		 * 송신 디바이스를 설정한다.
		 * 
		 * @param device
		 * @return
		 */
		public Builder setSourceDevice(BlinkDevice device) {
			if (device != null)
				mBlinkMessage.SourceAddress = device.getAddress();
			return this;
		}
		
		/**
		 * 송신 애플리케이션을 설정한다.
		 * 
		 * @param packageName
		 * @return
		 */
		public Builder setSourceApplication(String packageName) {
			if (packageName != null)
				mBlinkMessage.SourceApplication = packageName;
			return this;
		}
		
		/**
		 * 수신 디바이스를 설정한다.
		 * <br> device가 null일 경우, Explicit Mode.
		 * 
		 * @param device
		 * @return
		 */
		public Builder setDestinationDevice(BlinkDevice device) {
			mBlinkMessage.DestinationAddress = device.getAddress();
			return this;
		}
		
		/**
		 * 수신 애플리케이션을 설정한다.
		 * <br> packageName이 null일 경우, Explicit Mode.
		 * 
		 * @param packageName
		 * @return
		 */
		public Builder setDestinationApplication(String packageName) {
			mBlinkMessage.DestinationApplication = packageName;
			return this;
		}
		
		/**
		 * 메세지의 속성을 설정한다.
		 * <p> TYPE_1 : 
		 * <br> TYPE_2 :
		 * <hr>
		 * @param type
		 * @return
		 */
		public Builder setType(int type) {
			mBlinkMessage.Type = type;
			return this;
		}
		
		/**
		 * 메세지의 신뢰성 여부를 설정한다.
		 * <br> 신뢰성있는 메세지의 경우, 메세지 전달에 대한 응답이 없다면 결과를 알려준다.
		 * <br> 신뢰성없는 메세지의 경우, 메세지 전달에 대한 응답이 존재하지 않는다.
		 * 
		 * @param reliable
		 * @return
		 */
		public Builder setReliable(boolean reliable) {
			mBlinkMessage.Reliable = reliable;
			return this;
		}
		
		/**
		 * 
		 * @param message
		 * @return
		 */
		public Builder setMessage(String message) {
			return this;
		}
		
		/**
		 * {@link BlinkMessage} 객체를 반환한다.
		 * @return
		 */
		public BlinkMessage build() {
			mBlinkMessage.Timestamp = System.currentTimeMillis();
			return mBlinkMessage;
		}
	}
	
	

	// *** SETTER/GETTER DECLARATION *** //
	public String getSourceAddress() {
		return SourceAddress;
	}

	public String getSourceApplication() {
		return SourceApplication;
	}

	public String getDestinationAddress() {
		return DestinationAddress;
	}

	public String getDestinationApplication() {
		return DestinationApplication;
	}

	public int getType() {
		return Type;
	}

	public boolean isReliable() {
		return Reliable;
	}

	public long getTimestamp() {
		return Timestamp;
	}

	public String getMessage() {
		return message;
	}
	
}
