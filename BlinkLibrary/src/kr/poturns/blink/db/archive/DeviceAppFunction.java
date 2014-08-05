package kr.poturns.blink.db.archive;


public class DeviceAppFunction implements IDatabaseObject{
	
	public int DeviceAppId;
	public String Function;
	public String Description;
	
	public DeviceAppFunction(){
		this.Description = "";
	}
	public DeviceAppFunction(String Function,String Description){
		this.Function = Function;
		this.Description = Description;
	}
	
	public String toString(){
		String ret = "";
		ret += "DeviceAppId : "+DeviceAppId+"\r\n";
		ret += "Function : "+Function+"\r\n";
		ret += "Description : "+Description+"\r\n";
		return ret;
	}
	/**
	 * DeviceAppFunction 테이블에 등록하기 위한 최소한의 조건을 만족하는지 확인
	 * 테이블 구조대로 Function 필드가 null이 아니여야한다.
	 * param	:	void
	 * return	:	boolean (Function 변수가 null이 아니고 길이가 0보다 클 경우) 
	 * 				false (Function 변수가 null이거나 길이가 0인 경우)
	 */
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		if(Function!=null&&Function.length()>0)return true;
		return false;
	}
	
}