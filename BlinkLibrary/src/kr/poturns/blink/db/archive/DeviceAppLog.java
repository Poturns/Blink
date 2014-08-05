package kr.poturns.blink.db.archive;

public class DeviceAppLog implements IDatabaseObject{
	public int DeviceAppId;
	public String Content;
	public String DateTime;

	public DeviceAppLog() {
		this.DeviceAppId = -1;
		this.Content = "";
		this.DateTime = "";
	}

	public DeviceAppLog(int DeviceAppId, String Content) {
		this.DeviceAppId = DeviceAppId;
		this.Content = Content;
	}

	public String toString() {
		String ret = "";
		ret += "DeviceAppId : " + DeviceAppId + "\r\n";
		ret += "Content : " + Content + "\r\n";
		ret += "DateTime : " + DateTime + "\r\n";
		return ret;
	}

	/**
	 * DeviceAppFunction 테이블에 등록하기 위한 최소한의 조건을 만족하는지 확인 테이블 구조대로 Function 필드가
	 * null이 아니여야한다. param : void return : boolean (Function 변수가 null이 아니고 길이가
	 * 0보다 클 경우) false (Function 변수가 null이거나 길이가 0인 경우)
	 */
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		if (DeviceAppId < 0)
			return false;
		return true;
	}
}
