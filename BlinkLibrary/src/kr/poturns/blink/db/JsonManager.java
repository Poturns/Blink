package kr.poturns.blink.db;

import java.lang.reflect.Type;
import java.util.ArrayList;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * DB에서 사용되는 여러 객체들을 Json으로 만들어주는 전역 함수들을 선언하고 있다.
 * 
 * @author Jiwon
 *
 */
public class JsonManager {
	private final String tag = "JsonManager";
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public static String obtainJsonBlinkAppInfo(ArrayList<BlinkAppInfo> mList){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ret = gson.toJson(mList);
		return ret;
	}
	public static ArrayList<BlinkAppInfo> obtainJsonBlinkAppInfo(String json){
		Type Type = new TypeToken<ArrayList<BlinkAppInfo>>(){}.getType();
		ArrayList<BlinkAppInfo> ret = new Gson().fromJson(json, Type);
		return ret;
	}
	public static String obtainJsonMeasurement(ArrayList<Measurement> mMeasurement){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ret = gson.toJson(mMeasurement);
		return ret;
	}
	
	public static ArrayList<Measurement> obtainJsonMeasurement(String Json){
		Type MeasurementType = new TypeToken<ArrayList<Measurement>>(){}.getType();
		ArrayList<Measurement> ret = new Gson().fromJson(Json, MeasurementType);
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
	
	public static Function obtainJsonFunction(String jsonFunction){
	      Type FunctionType = new TypeToken<Function>(){}.getType();
	      Function ret = new Gson().fromJson(jsonFunction, FunctionType);
	      return ret;
	   }
}
