package kr.poturns.blink.db.archive;


public class Device implements IDatabaseObject{

	public int DeviceId;
	public String Device;
	public String UUID;
	public String MacAddress;
	public String DateTime;
	
	public Device(){
		DeviceId = -1;
		Device = "";
		UUID = "";
		MacAddress = "";
	}
	
	public String toString(){
		String ret = "";
		ret += "DeviceId : "+DeviceId+"\r\n";
		ret += "Device : "+Device+"\r\n";
		ret += "UUID : "+UUID+"\r\n";
		ret += "MacAddress : "+MacAddress+"\r\n";
		return ret;
	}
	/**
	 * DeviceAppList 테이블에 등록하기 위한 최소한의 조건을 만족하는지 확인
	 * 테이블 구조대로 Device와 App 필드가 null이 아니여야한다.
	 * param	:	void
	 * return	:	boolean (Device와 App 변수가 null이 아니고 길이가 0보다 클 경우) 
	 * 				false (Device와 App 변수가 null이거나 길이가 0인 경우)
	 */
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}
}