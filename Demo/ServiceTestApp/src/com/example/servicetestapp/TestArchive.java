package com.example.servicetestapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.schema.Body;
import kr.poturns.blink.schema.Eye;
import kr.poturns.blink.schema.Heart;
import kr.poturns.blink.service.BlinkDatabaseServiceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TestArchive {
	private final String tag = "TestArchive";
	
	JsonManager mJsonManager;
	SystemDatabaseObject mSystemDatabaseObject;
	List<SystemDatabaseObject> mSystemDatabaseObjectList;
	BlinkDatabaseServiceManager mBlinkDatabaseServiceManager = null;
	
	public TestArchive(BlinkDatabaseServiceManager mBlinkDatabaseServiceManager){
		this.mBlinkDatabaseServiceManager = mBlinkDatabaseServiceManager;
	}
	public void run(){
//		exampleRegisterSystemDatabase();
//		exampleObtainSystemDatabaseAll();
//		exampleObtainMeasurementDataById();
//		exampleObtainSystemDatabase();
//		exampleRegisterMeasurementDatabase();
//		exampleObtainMeasurementDatabase();
//		exampleRemoveMeasurementDatabase();
//		exampleLogAll();
		exampleStartFuntion();
	}
	
	private void exampleStartFuntion() {
		// TODO Auto-generated method stub
		mSystemDatabaseObject = mBlinkDatabaseServiceManager.obtainSystemDatabase();
		ArrayList<Function> mFunctionList;
		if(mSystemDatabaseObject.isExist){
			mFunctionList = mSystemDatabaseObject.mFunctionList;
			for(int i=0;i<mFunctionList.size();i++){
				
			}
		}
	}
	/**
	 * MeasurementData를 삭제하는 예제'
	 * 파라미터로 넘겨준 클래스와 일치하는 데이터를 삭제한다.
	 * 시간을 조건으로 줄 수 있다.
	 */
	private void exampleRemoveMeasurementDatabase() {
		// TODO Auto-generated method stub
		int ret = mBlinkDatabaseServiceManager.removeMeasurementData(Eye.class, null, null);
		Log.i(tag,"remove : "+ret);
	}
	/**
	 * SystemDatabase를 등록하는 예제
	 * 먼저 obtainSystemDatabase()를 통해 SystemDatabase를 Device 이름과 App 패키지명으로
	 * 기존에 등록되어있는 것이 있는지 확인한다.
	 * mSystemDatabaseObject.isExist 통해 등록되어있지 않으면 기능과 측정값을 등록하고
	 * mBlinkServiceManager.registerSystemDatabase() 호출을 통해 등록한다.
	 * 측정값 등록은 클래스를 넣으면 자동으로 필드에 맞추어 등록을 해준다.
	 */
	public void exampleRegisterSystemDatabase(){
		Log.i(tag, "exampleRegisterSystemDatabase");
		//SystemDatabase 객체 얻기, 기존에 등록되어있으면 등록되어있는 값을 넣어준다.
		mSystemDatabaseObject = mBlinkDatabaseServiceManager.obtainSystemDatabase();
		if(!mSystemDatabaseObject.isExist){
			//등록되어있지 않으면 추가적으로 등록할 함수, 측정값을 추가하고 등록한다.
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY);
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_SERIVCE);
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_BROADCAST);
			mSystemDatabaseObject.addMeasurement(Eye.class);
			mSystemDatabaseObject.addMeasurement(Body.class);
			mSystemDatabaseObject.addMeasurement(Heart.class);
			//sqlite에 등록하는 함수
			mBlinkDatabaseServiceManager.registerSystemDatabase(mSystemDatabaseObject);
		}
	}
	
	/**
	 * 등록되어있는 mSystemDatabaseObject를 얻어오는 예제
	 */
	public void exampleObtainSystemDatabase(){
		Log.i(tag, "exampleObtainSystemDatabase");
		mSystemDatabaseObject = mBlinkDatabaseServiceManager.obtainSystemDatabase();
		if(mSystemDatabaseObject.isExist){
			Log.i(tag, "등록된 디바이스와 어플리케이션이 있으면");
		}else {
			Log.i(tag, "등록된 디바이스와 어플리케이션이 없으면");
		}
	}
	
	/**
	 * 등록되어있는 모든 mSystemDatabaseObject를 얻어오는 예제
	 */
	public void exampleObtainSystemDatabaseAll(){
		Log.i(tag, "exampleObtainSystemDatabaseAll");
		mSystemDatabaseObjectList = mBlinkDatabaseServiceManager.obtainSystemDatabaseAll();
		SystemDatabaseObject systemDatabaseObject = null;
		for(int i=0;i<mSystemDatabaseObjectList.size();i++){
			systemDatabaseObject = mSystemDatabaseObjectList.get(i);
			Log.i(tag, i+" sdo :"+systemDatabaseObject.toString());
		}
	}
	
	/**
	 * mSystemDatabaseObject의 ID로 얻어오는 예제
	 */
	public void exampleObtainMeasurementDataById(){
		Log.i(tag, "exampleObtainMeasurementDataById");
		mSystemDatabaseObjectList = mBlinkDatabaseServiceManager.obtainSystemDatabaseAll();
		for(int i=0;i<mSystemDatabaseObjectList.size();i++){
			List<MeasurementData> mMeasurementDataList = mBlinkDatabaseServiceManager.obtainMeasurementData(mSystemDatabaseObjectList.get(i).mMeasurementList, null, null);
			for(int j=0;j<mMeasurementDataList.size();j++){
				Log.i(tag, "MeasurementData "+j+" \n"+mMeasurementDataList.get(j).toString());
			}
		}
	}
	
	/**
	 * 측정값을 저장하는 예제
	 * 이 예제에서는 Eye, Body, Heart 세 개의 클래스에서
	 * 랜덤하게 100개씩 생성하여 등록하는 예제이다.
	 * mBlinkServiceManager.registerMeasurementData(mSystemDatabaseObject,mEye) 에서
	 * mSystemDatabaseObject와 해당 객체를 매개변수로 전달하면 등록을 해준다.
	 */
	public void exampleRegisterMeasurementDatabase(){
		Log.i(tag, "exampleRegisterMeasurementDatabase");
		mSystemDatabaseObject = mBlinkDatabaseServiceManager.obtainSystemDatabase();
		if(mSystemDatabaseObject.isExist){
			try {
				Eye mEye;
				Body mBody;
				Heart mHeart;
				Random random = new Random();
				for(int i=0;i<10;i++){
					mEye = new Eye();
					mEye.left_sight = Math.round(random.nextDouble()*10d)/10d;
					mEye.right_sight = Math.round(random.nextDouble()*10d)/10d;
					mBlinkDatabaseServiceManager.registerMeasurementData(mSystemDatabaseObject,mEye);
				}
				for(int i=0;i<10;i++){
					mBody = new Body();
					mBody.height = Math.round(random.nextFloat()*10f)/10f+random.nextInt(50)+140;
					mBody.weight = Math.round(random.nextFloat()*10f)/10f+random.nextInt(50)+40;
					mBlinkDatabaseServiceManager.registerMeasurementData(mSystemDatabaseObject,mBody);
				}
				
				for(int i=0;i<10;i++){
					mHeart = new Heart();
					mHeart.beatrate = random.nextInt(20)+60;
					mBlinkDatabaseServiceManager.registerMeasurementData(mSystemDatabaseObject,mHeart);
				}
			
				
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			return;
		}
	}
	
	/**
	 * 측정값을 얻어오는 예제
	 * mBlinkServiceManager.obtainMeasurementData(class); 와 같이 얻고 싶은 데이터의 
	 * 클래스를 넘기면 ArrayList<class> 형식으로 반환해준다.
	 * 반환받는 형식에 맞추어 리턴을 해주기 때문에 대입되는 변수의 타입이 중요하다.
	 * ArrayList<Eye> mEyeList = mBlinkServiceManager.obtainMeasurementData(Eye.class);
	 * 위의 경우 해당 함수는 ArrayList<Eye>로 반환해준다.
	 */
	public void exampleObtainMeasurementDatabase(){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		ArrayList<Eye> mEyeList = mBlinkDatabaseServiceManager.obtainMeasurementData(Eye.class,new TypeToken<ArrayList<Eye>>(){}.getType());
		for(int i=0;i<mEyeList.size();i++){
			Log.i(tag, "Eye - left_sight : "+mEyeList.get(i).left_sight+" right_sight : "+mEyeList.get(i).right_sight+ " DateTime : "+mEyeList.get(i).DateTime);
		}
		ArrayList<Body> mBodyList = mBlinkDatabaseServiceManager.obtainMeasurementData(Body.class,new TypeToken<ArrayList<Body>>(){}.getType());
		for(int i=0;i<mBodyList.size();i++){
			Log.i(tag, "Body - height : "+mBodyList.get(i).height+" weight : "+mBodyList.get(i).weight+ " DateTime : "+mBodyList.get(i).DateTime);
		}
		ArrayList<Heart> mHeartList = mBlinkDatabaseServiceManager.obtainMeasurementData(Heart.class,new TypeToken<ArrayList<Heart>>(){}.getType());
		for(int i=0;i<mHeartList.size();i++){
			Log.i(tag, "Heart - beatrate : "+mHeartList.get(i).beatrate+" DateTime : "+mHeartList.get(i).DateTime);
		}
	}
	
	
	public void exampleLogAll(){
		Log.i(tag, "exampleLogAll");
		for(int i=0;i<100;i++){
			mBlinkDatabaseServiceManager.registerLog("Device"+i, "App"+i, i, "test"+i);
		}
		
		List<BlinkLog> mDeviceAppLogList = mBlinkDatabaseServiceManager.obtainLog();
		for(int i=0;i<mDeviceAppLogList.size();i++){
			Log.i(tag, mDeviceAppLogList.get(i).toString());
		}
	}
}
