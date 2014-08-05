package kr.poturns.blink.db.archive;


public class DeviceAppMeasurement implements IDatabaseObject{
	
	public int DeviceAppId;
	public int MeasurementId;
	public String Measurement;
	public String Type;
	public String Description;
	
	public DeviceAppMeasurement(){
		this.Description = "";
	}
	public DeviceAppMeasurement(String Measurement,String Type,String Description){
		this.Measurement = Measurement;
		this.Type = Type;
		this.Description = Description;
	}
	public MeasurementData obtainMeasurement(){
		MeasurementData mMeasurementData = new MeasurementData();
		mMeasurementData.MeasurementId = this.MeasurementId;
		return mMeasurementData;
	}
	
	
	public String toString(){
		String ret = "";
		ret += "DeviceAppId : "+DeviceAppId+"\r\n";
		ret += "MeasurementId : "+MeasurementId+"\r\n";
		ret += "Measurement : "+Measurement+"\r\n";
		ret += "Type : "+Type+"\r\n";
		ret += "Description : "+Description+"\r\n";
		return ret;
	}
	/**
	 * DeviceAppMeasurement 테이블에 등록하기 위한 최소한의 조건을 만족하는지 확인
	 * 테이블 구조대로 Measurement와 Type 필드가 null이 아니여야한다.
	 * param	:	void
	 * return	:	boolean (Measurement와 Type 변수가 null이 아니고 길이가 0보다 클 경우) 
	 * 				false (Measurement와 Type 변수가 null이거나 길이가 0인 경우)
	 */
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		if(Measurement!=null&&Type!=null&&Measurement.length()>0&&Type.length()>0)return true;
		return false;
	}
}