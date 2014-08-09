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
	private final String tag = "JsonManager";
	public JsonManager(){
	}
	public static String obtainJsonSystemDatabaseObject(ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ret = gson.toJson(mSystemDatabaseObjectList);
		return ret;
	}
	public static ArrayList<SystemDatabaseObject> obtainJsonSystemDatabaseObject(String json){
		Type SystemDatabaseObjectType = new TypeToken<ArrayList<SystemDatabaseObject>>(){}.getType();
		ArrayList<SystemDatabaseObject> ret = new Gson().fromJson(json, SystemDatabaseObjectType);
		return ret;
	}
	public static String obtainJsonDeviceAppMeasurement(ArrayList<DeviceAppMeasurement> mDeviceAppMeasurement){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ret = gson.toJson(mDeviceAppMeasurement);
		return ret;
	}
	
	public static ArrayList<DeviceAppMeasurement> obtainJsonDeviceAppMeasurement(String Json){
		Type DeviceAppMeasurementType = new TypeToken<ArrayList<DeviceAppMeasurement>>(){}.getType();
		ArrayList<DeviceAppMeasurement> ret = new Gson().fromJson(Json, DeviceAppMeasurementType);
		return ret;
	}
	
	public static String obtainJsonMeasurementData(ArrayList<MeasurementData> mMeasurementData){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ret = gson.toJson(mMeasurementData);
		return ret;
	}
	
	public static ArrayList<MeasurementData> obtainJsonMeasurementData(String json){
		Type MeasurementDataType = new TypeToken<ArrayList<MeasurementData>>(){}.getType();
		ArrayList<MeasurementData> ret = new Gson().fromJson(json, MeasurementDataType);
		return ret;
	}
}
