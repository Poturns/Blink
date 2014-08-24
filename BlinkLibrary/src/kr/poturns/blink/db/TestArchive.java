package kr.poturns.blink.db;

import java.util.ArrayList;
import java.util.Random;

import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.schema.Body;
import kr.poturns.blink.schema.Eye;
import kr.poturns.blink.schema.Heart;
import android.content.Context;
import android.util.Log;

public class TestArchive {
	private final String tag = "TestArchive";
	
	SqliteManager mSqliteManager;
	JsonManager mJsonManager;
	SystemDatabaseObject mSystemDatabaseObject;
	
	public TestArchive(Context context){
		mJsonManager = new JsonManager();
	}
	public void run(){
		exampleRegisterSystemDatabase();
		exampleObtainSystemDatabase();
		exampleRegisterMeasurementDatabase();
		exampleObtainMeasurementDatabase();
//		exampleRemoveMeasurementDatabase();
//		exampleObtainJson();
	}
	
	/**
	 * MeasurementData를 삭제하는 예제'
	 * 파라미터로 넘겨준 클래스와 일치하는 데이터를 삭제한다.
	 * 시간을 조건으로 줄 수 있다.
	 */
	private void exampleRemoveMeasurementDatabase() {
		// TODO Auto-generated method stub
		int ret = mSqliteManager.removeMeasurementData(Eye.class, null, null);
		Log.i(tag,"remove : "+ret);
	}
	/**
	 * SystemDatabase를 등록하는 예제
	 * 먼저 obtainSystemDatabase()를 통해 SystemDatabase를 Device 이름과 App 패키지명으로
	 * 기존에 등록되어있는 것이 있는지 확인한다.
	 * mSystemDatabaseObject.isExist 통해 등록되어있지 않으면 기능과 측정값을 등록하고
	 * mSqliteManager.registerSystemDatabase() 호출을 통해 등록한다.
	 * 측정값 등록은 클래스를 넣으면 자동으로 필드에 맞추어 등록을 해준다.
	 */
	public void exampleRegisterSystemDatabase(){
		Log.i(tag, "exampleRegisterSystemDatabase");
		//SystemDatabase 객체 얻기, 기존에 등록되어있으면 등록되어있는 값을 넣어준다.
		mSystemDatabaseObject = mSqliteManager.obtainSystemDatabase("optimus g pro","com.example.blinkdb1");
		if(!mSystemDatabaseObject.isExist){
			//등록되어있지 않으면 추가적으로 등록할 함수, 측정값을 추가하고 등록한다.
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY);
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_SERIVCE);
			mSystemDatabaseObject.addFunction("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_BROADCAST);
			mSystemDatabaseObject.addMeasurement(Eye.class);
			mSystemDatabaseObject.addMeasurement(Body.class);
			mSystemDatabaseObject.addMeasurement(Heart.class);
			//sqlite에 등록하는 함수
			mSqliteManager.registerSystemDatabase(mSystemDatabaseObject);
		}
	}
	
	/**
	 * 등록되어있는 mSystemDatabaseObject를 얻어오는 예제
	 */
	public void exampleObtainSystemDatabase(){
		Log.i(tag, "exampleObtainSystemDatabase");
		mSystemDatabaseObject = mSqliteManager.obtainSystemDatabase("optimus g pro","com.example.blinkdb1");
		if(mSystemDatabaseObject.isExist){
			Log.i(tag, "등록된 디바이스와 어플리케이션이 있으면");
		}else {
			Log.i(tag, "등록된 디바이스와 어플리케이션이 없으면");
		}
	}
	
	/**
	 * 측정값을 저장하는 예제
	 * 이 예제에서는 Eye, Body, Heart 세 개의 클래스에서
	 * 랜덤하게 100개씩 생성하여 등록하는 예제이다.
	 * mSqliteManager.registerMeasurementData(mSystemDatabaseObject,mEye) 에서
	 * mSystemDatabaseObject와 해당 객체를 매개변수로 전달하면 등록을 해준다.
	 */
	public void exampleRegisterMeasurementDatabase(){
		Log.i(tag, "exampleRegisterMeasurementDatabase");
		mSystemDatabaseObject = mSqliteManager.obtainSystemDatabase("optimus g pro","com.example.blinkdb1");
		if(mSystemDatabaseObject.isExist){
			try {
				Eye mEye;
				Body mBody;
				Heart mHeart;
				Random random = new Random();
				for(int i=0;i<100;i++){
					mEye = new Eye();
					mEye.left_sight = Math.round(random.nextDouble()*10d)/10d;
					mEye.right_sight = Math.round(random.nextDouble()*10d)/10d;
					mSqliteManager.registerMeasurementData(mSystemDatabaseObject,mEye);
				}
				for(int i=0;i<100;i++){
					mBody = new Body();
					mBody.height = Math.round(random.nextFloat()*10f)/10f+random.nextInt(50)+140;
					mBody.weight = Math.round(random.nextFloat()*10f)/10f+random.nextInt(50)+40;
					mSqliteManager.registerMeasurementData(mSystemDatabaseObject,mBody);
				}
				
				for(int i=0;i<100;i++){
					mHeart = new Heart();
					mHeart.beatrate = random.nextInt(20)+60;
					mSqliteManager.registerMeasurementData(mSystemDatabaseObject,mHeart);
				}
			
				
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	 * mSqliteManager.obtainMeasurementData(class); 와 같이 얻고 싶은 데이터의 
	 * 클래스를 넘기면 ArrayList<class> 형식으로 반환해준다.
	 * 반환받는 형식에 맞추어 리턴을 해주기 때문에 대입되는 변수의 타입이 중요하다.
	 * ArrayList<Eye> mEyeList = mSqliteManager.obtainMeasurementData(Eye.class);
	 * 위의 경우 해당 함수는 ArrayList<Eye>로 반환해준다.
	 */
	public void exampleObtainMeasurementDatabase(){
//		Log.i(tag, "exampleObtainMeasurementDatabase");
//		try {
//			ArrayList<Eye> mEyeList = mSqliteManager.obtainMeasurementData(Eye.class);
//			for(int i=0;i<mEyeList.size();i++){
//				Log.i(tag, "Eye - left_sight : "+mEyeList.get(i).left_sight+" right_sight : "+mEyeList.get(i).right_sight+ " DateTime : "+mEyeList.get(i).DateTime);
//			}
//			ArrayList<Body> mBodyList = mSqliteManager.obtainMeasurementData(Body.class);
//			for(int i=0;i<mBodyList.size();i++){
//				Log.i(tag, "Body - height : "+mBodyList.get(i).height+" weight : "+mBodyList.get(i).weight+ " DateTime : "+mBodyList.get(i).DateTime);
//			}
//			ArrayList<Heart> mHeartList = mSqliteManager.obtainMeasurementData(Heart.class);
//			for(int i=0;i<mHeartList.size();i++){
//				Log.i(tag, "Heart - beatrate : "+mHeartList.get(i).beatrate+" DateTime : "+mHeartList.get(i).DateTime);
//			}
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public void exampleObtainJson(){
		Log.i(tag, "exampleObtainJson");
		ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList = new ArrayList<SystemDatabaseObject>();
		exampleObtainSystemDatabase();
		mSystemDatabaseObjectList.add(mSystemDatabaseObject);
		mSystemDatabaseObjectList.add(mSystemDatabaseObject);
		String gson = mJsonManager.obtainJsonSystemDatabaseObject(mSystemDatabaseObjectList);
		mSystemDatabaseObjectList = mJsonManager.obtainJsonSystemDatabaseObject(gson);
	}
}
