package com.example.servicetestapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.schema.Body;
import kr.poturns.blink.schema.Eye;
import kr.poturns.blink.schema.Heart;
import kr.poturns.blink.schema.extendsEye;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TestArchive {
	private final String tag = "TestArchive";
	
	JsonManager mJsonManager;
	SystemDatabaseObject mSystemDatabaseObject;
	BlinkServiceInteraction mBlinkServiceInteraction = null;
	
	public TestArchive(BlinkServiceInteraction mBlinkServiceInteraction){
		this.mBlinkServiceInteraction = mBlinkServiceInteraction;
	}
	public void run(){
		exampleRegisterSystemDatabase();
		exmampleRegisterExternalSystemDatabase();
//		exampleObtainSystemDatabaseAll();
//		exampleObtainMeasurementDataById();
//		exampleObtainSystemDatabase();
//		exampleRegisterMeasurementDatabase();
//		exampleObtainMeasurementDatabase();
//		exampleRemoveMeasurementDatabase();
//		exampleLogAll();
//		exampleStartFuntion();
//		exampleBlinkDatabaseManager();
		exampleCheckInDevice();
	}
	
	/**
	 * 데이터가 디바이스 내에 있는지 확인하는 예제
	 */
	private void exampleCheckInDevice() {
	    // TODO Auto-generated method stub
		boolean result = false;
		
		//checkInDeviceByClass
		result = mBlinkServiceInteraction.checkInDeviceByClass(Eye.class);
		Log.i(tag, "checkInDeviceByClass(Eye.class) : "+result);
		
		result = mBlinkServiceInteraction.checkInDeviceByClass(Body.class);
		Log.i(tag, "checkInDeviceByClass(Body.class) : "+result);
		
		//checkInDeviceByFunction
		mBlinkServiceInteraction.queryDevice("");
		mBlinkServiceInteraction.queryFunction("");
		List<Function> mFunctionList = mBlinkServiceInteraction.getFunctionList();
		if(mFunctionList.size()>0){
			result = mBlinkServiceInteraction.checkInDeviceByFunction(mFunctionList.get(0));
			Log.i(tag, "checkInDeviceByFunction : "+result);
		}
		
		//checkInDeviceByMeasureList
		mBlinkServiceInteraction.queryMeasurement("");
		result = mBlinkServiceInteraction.checkInDeviceByMeasureList(mBlinkServiceInteraction.getMeasurementList());
		Log.i(tag, "checkInDeviceByMeasureList : "+result);
    }
	
	/**
	 * BlinkDatabaseManager 사용법을 설명하는 예제
	 */
	private void exampleBlinkDatabaseManager() {
	    // TODO Auto-generated method stub
		mBlinkServiceInteraction.queryDevice("");
		List<Device> mDeviceList = mBlinkServiceInteraction.getDeviceList();
		mBlinkServiceInteraction.queryApp("");
		List<App> mAppList = mBlinkServiceInteraction.getAppList();
		mBlinkServiceInteraction.queryFunction("");
		List<Function> mFunctionList = mBlinkServiceInteraction.getFunctionList();
		mBlinkServiceInteraction.queryMeasurement("");
		List<Measurement> mMeasurementList = mBlinkServiceInteraction.getMeasurementList();
		mBlinkServiceInteraction.queryMeasurementData("");
		List<MeasurementData> mMeasurementDataList = mBlinkServiceInteraction.getMeasurementDataList();
		
		Log.i(tag,"Device List :");
		for(int i=0;i<mDeviceList.size();i++){
			Log.i(tag,mDeviceList.get(i).toString());
		}
		Log.i(tag,"App List :");
		for(int i=0;i<mAppList.size();i++){
			Log.i(tag,mAppList.get(i).toString());
		}
		Log.i(tag,"Function List :");
		for(int i=0;i<mFunctionList.size();i++){
			Log.i(tag,mFunctionList.get(i).toString());
		}
		Log.i(tag,"Measurement List :");
		for(int i=0;i<mMeasurementList.size();i++){
			Log.i(tag,mMeasurementList.get(i).toString());
		}
		Log.i(tag,"MeasurementData List :");
		for(int i=0;i<mMeasurementDataList.size();i++){
			Log.i(tag,mMeasurementDataList.get(i).toString());
		}
    }
	/**
	 * Function의 인텐트를 실행하는 예제
	 * Function 클래스를 startFunction 매서드에 매개변수로 넘겨주면 서비스에서 Intent를 날려준다.
	 */
	private void exampleStartFuntion() {
		// TODO Auto-generated method stub
		Log.i(tag, "exampleStartFuntion");
		mSystemDatabaseObject = mBlinkServiceInteraction.obtainSystemDatabase();
		ArrayList<Function> mFunctionList;
		if(mSystemDatabaseObject.isExist){
			mFunctionList = mSystemDatabaseObject.mFunctionList;
			Log.i(tag, "mFunctionList size : "+mFunctionList.size());
			for(int i=0;i<mFunctionList.size();i++){
				Log.i(tag, mFunctionList.get(i).toString());
				mBlinkServiceInteraction.startFuntion(mFunctionList.get(i));
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
		int ret = mBlinkServiceInteraction.removeMeasurementData(Eye.class, null, null);
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
		mSystemDatabaseObject = mBlinkServiceInteraction.obtainSystemDatabase();
		if(!mSystemDatabaseObject.isExist){
			//등록되어있지 않으면 추가적으로 등록할 함수, 측정값을 추가하고 등록한다.
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY);
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_SERIVCE);
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_BROADCAST);
			mSystemDatabaseObject.addMeasurement(Eye.class);
//			mSystemDatabaseObject.addMeasurement(Body.class);
//			mSystemDatabaseObject.addMeasurement(Heart.class);
//			mSystemDatabaseObject.addMeasurement(extendsEye.class);
			//sqlite에 등록하는 함수
			mBlinkServiceInteraction.registerSystemDatabase(mSystemDatabaseObject);
		}
	}
	
	public void exmampleRegisterExternalSystemDatabase(){
		//외부 디바이스 임시 등록
		mSystemDatabaseObject = mBlinkServiceInteraction.obtainSystemDatabase();
		mSystemDatabaseObject.mDevice.Device = "ExternalDevice";
		mSystemDatabaseObject.mApp.PackageName = "ExternalPackageName";
		mSystemDatabaseObject.mApp.AppName = "ExternalAppName";
		mSystemDatabaseObject.mFunctionList.clear();
		mSystemDatabaseObject.addFunction("ExternalFunction", "ExternalFunction","ExternalFunction",Function.TYPE_ACTIVITY);
		mSystemDatabaseObject.mMeasurementList.clear();
		mSystemDatabaseObject.addMeasurement(Body.class);
		mBlinkServiceInteraction.registerExternalSystemDatabase(mSystemDatabaseObject);
	}
	
	/**
	 * 등록되어있는 mSystemDatabaseObject를 얻어오는 예제
	 */
	public void exampleObtainSystemDatabase(){
		Log.i(tag, "exampleObtainSystemDatabase");
		mSystemDatabaseObject = mBlinkServiceInteraction.obtainSystemDatabase();
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
		List<SystemDatabaseObject> mSystemDatabaseObjectList = mBlinkServiceInteraction.obtainSystemDatabaseAll();
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
		List<SystemDatabaseObject> mSystemDatabaseObjectList = mBlinkServiceInteraction.obtainSystemDatabaseAll();
		for(int i=0;i<mSystemDatabaseObjectList.size();i++){
			List<MeasurementData> mMeasurementDataList = mBlinkServiceInteraction.obtainMeasurementData(mSystemDatabaseObjectList.get(i).mMeasurementList, null, null);
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
		mSystemDatabaseObject = mBlinkServiceInteraction.obtainSystemDatabase();
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
					mBlinkServiceInteraction.registerMeasurementData(mSystemDatabaseObject,mEye);
				}
				for(int i=0;i<10;i++){
					mBody = new Body();
					mBody.height = Math.round(random.nextFloat()*10f)/10f+random.nextInt(50)+140;
					mBody.weight = Math.round(random.nextFloat()*10f)/10f+random.nextInt(50)+40;
					mBlinkServiceInteraction.registerMeasurementData(mSystemDatabaseObject,mBody);
				}
				
				for(int i=0;i<10;i++){
					mHeart = new Heart();
					mHeart.beatrate = random.nextInt(20)+60;
					mBlinkServiceInteraction.registerMeasurementData(mSystemDatabaseObject,mHeart);
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
		ArrayList<extendsEye> mEyeList = mBlinkServiceInteraction.obtainMeasurementData(extendsEye.class,SqliteManager.CONTAIN_FIELD,new TypeToken<ArrayList<extendsEye>>(){}.getType());
		for(int i=0;i<mEyeList.size();i++){
			Log.i(tag, "Eye - left_sight : "+mEyeList.get(i).left_sight+" right_sight : "+mEyeList.get(i).right_sight+ " DateTime : "+mEyeList.get(i).DateTime);
		}
		ArrayList<Body> mBodyList = mBlinkServiceInteraction.obtainMeasurementData(Body.class,new TypeToken<ArrayList<Body>>(){}.getType());
		for(int i=0;i<mBodyList.size();i++){
			Log.i(tag, "Body - height : "+mBodyList.get(i).height+" weight : "+mBodyList.get(i).weight+ " DateTime : "+mBodyList.get(i).DateTime);
		}
		ArrayList<Heart> mHeartList = mBlinkServiceInteraction.obtainMeasurementData(Heart.class,new TypeToken<ArrayList<Heart>>(){}.getType());
		for(int i=0;i<mHeartList.size();i++){
			Log.i(tag, "Heart - beatrate : "+mHeartList.get(i).beatrate+" DateTime : "+mHeartList.get(i).DateTime);
		}
	}
	
	
	public void exampleLogAll(){
		Log.i(tag, "exampleLogAll");
		List<BlinkLog> mDeviceAppLogList = mBlinkServiceInteraction.obtainLog();
		for(int i=0;i<mDeviceAppLogList.size();i++){
			Log.i(tag, mDeviceAppLogList.get(i).toString());
		}
	}
}
