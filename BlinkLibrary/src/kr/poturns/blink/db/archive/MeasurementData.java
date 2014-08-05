package kr.poturns.blink.db.archive;


public class MeasurementData implements IDatabaseObject{
	public int MeasurementId;
	public int GroupId;
	public String Data;
	public String DateTime;
	
	public MeasurementData(){
		
	}

	public String toString(){
		String ret = "";
		ret += "MeasurementId : "+MeasurementId+"\r\n";
		ret += "MeasurementDataId : "+GroupId+"\r\n";
		ret += "Data : "+Data+"\r\n";
		ret += "DateTime : "+DateTime+"\r\n";
		return ret;
	}
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return false;
	}
}
