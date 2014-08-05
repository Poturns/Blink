package kr.poturns.blink.internal;

import java.util.ArrayList;

import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.internal.comm.BluetoothDeviceExtended;
import android.content.Context;

/**
 * 
 * @author YeonHo.Kim
 * @since 2014.07.12
 *
 */
class FunctionOperator implements Runnable {
	
	private final Context OPERATOR_CONTEXT;
	
	private JsonManager mJsonManager;
	private SqliteManager mSqliteManager;
	
	public FunctionOperator(Context context) {
		this.OPERATOR_CONTEXT = context;
		
		mJsonManager = new JsonManager();
		mSqliteManager = new SqliteManager(context);
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public void acceptJsonData(String json, BluetoothDeviceExtended deviceX) {
		
		ArrayList<SystemDatabaseObject> mObjectList = mJsonManager.obtainJsonSystemDatabaseObject(json);
		
		for (SystemDatabaseObject object : mObjectList) {
			
		}
		
	}
}
