package kr.poturns.blink.db;

import java.util.List;

import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import android.content.Context;

public class SyncDatabaseManager extends BlinkDatabaseManager{
	
	private List<SystemDatabaseObject> oldSystemDatabaseObject;
	private List<SystemDatabaseObject> newSystemDatabaseObject;
	
	public SyncDatabaseManager(Context context) {
	    super(context);
	    // TODO Auto-generated constructor stub
    }
	
	public boolean syncSystemDatabase(List<SystemDatabaseObject> systemDatabaseObjctList){
		List<Measurement> oldMeasurementList;
		List<Measurement> newMeasurementList;
		
		oldSystemDatabaseObject = obtainSystemDatabase();
		newSystemDatabaseObject = systemDatabaseObjctList;
		
		//새로운 SystemDatabaseObject 리스트의 인덱스
		for(int i=0;i<newSystemDatabaseObject.size();i++){
			//기존 SystemDatabaseObject 리스트의 인덱스
			for(int j=0;i<oldSystemDatabaseObject.size();j++){
				//동일한 systemDatabaseObject 검색
				if(newSystemDatabaseObject.get(i).mDevice.MacAddress.contentEquals(oldSystemDatabaseObject.get(j).mDevice.MacAddress)
						&& newSystemDatabaseObject.get(i).mApp.PackageName.contentEquals(oldSystemDatabaseObject.get(j).mApp.PackageName)){
					oldMeasurementList = oldSystemDatabaseObject.get(j).mMeasurementList;
					newMeasurementList = newSystemDatabaseObject.get(i).mMeasurementList;
					
					for(int k=0;k<newMeasurementList.size();k++){
						for(int l=0;l<oldMeasurementList.size();l++){
							
						}
					}
				}
			}
		}
			
		return true;
	}
}
