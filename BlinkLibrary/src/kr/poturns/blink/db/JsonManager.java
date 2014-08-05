package kr.poturns.blink.db;

import java.lang.reflect.Type;
import java.util.ArrayList;

import kr.poturns.blink.db.archive.DeviceAppMeasurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Blink json convert help manager
 * 
 * @author Jiwon
 *
 */
public class JsonManager {
	Gson gson;
	private final String tag = "JsonManager";
	public JsonManager(){
		gson = new GsonBuilder().setPrettyPrinting().create();
	}
	public String obtainJsonSystemDatabaseObject(ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList){
		String ret = gson.toJson(mSystemDatabaseObjectList);
		Log.i(tag,"obtainJsonSystemDatabaseObject : Object->String");
		Log.i(tag,ret);
		return ret;
	}
	public ArrayList<SystemDatabaseObject> obtainJsonSystemDatabaseObject(String json){
		Type SystemDatabaseObjectType = new TypeToken<ArrayList<SystemDatabaseObject>>(){}.getType();
		ArrayList<SystemDatabaseObject> ret = new Gson().fromJson(json, SystemDatabaseObjectType);
		
//		new Gson().fromJson(json, SystemDatabaseObjectType);
		Log.i(tag,"obtainJsonSystemDatabaseObject : String->Object");
		Log.i(tag,ret.toString());
		return ret;
	}
	
	public String obtainJsonDeviceAppMeasurement(ArrayList<DeviceAppMeasurement> mDeviceAppMeasurement){
		String ret = gson.toJson(mDeviceAppMeasurement);
		Log.i(tag,"obtainJsonDeviceAppMeasurement : Object->String");
		Log.i(tag,ret);
		return ret;
	}
	
	public ArrayList<DeviceAppMeasurement> obtainJsonDeviceAppMeasurement(String Json){
		Type DeviceAppMeasurementType = new TypeToken<ArrayList<DeviceAppMeasurement>>(){}.getType();
		ArrayList<DeviceAppMeasurement> ret = new Gson().fromJson(Json, DeviceAppMeasurementType);
		Log.i(tag,"obtainJsonDeviceAppMeasurement : String->Object");
		Log.i(tag,ret.toString());
		return ret;
	}
	
	public String obtainJsonMeasurementData(ArrayList<MeasurementData> mMeasurementData){
		String ret = gson.toJson(mMeasurementData);
		Log.i(tag,"obtainJsonMeasurementData : Object->String");
		Log.i(tag,ret);
		return ret;
	}
	
	public ArrayList<MeasurementData> obtainJsonMeasurementData(String json){
		Type MeasurementDataType = new TypeToken<ArrayList<MeasurementData>>(){}.getType();
		ArrayList<MeasurementData> ret = new Gson().fromJson(json, MeasurementDataType);
		Log.i(tag,"obtainJsonMeasurementData : String->Object");
		Log.i(tag,ret.toString());
		return ret;
	}
}
