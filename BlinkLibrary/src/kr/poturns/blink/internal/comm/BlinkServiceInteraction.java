package kr.poturns.blink.internal.comm;

import java.lang.reflect.Type;
import java.util.List;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.internal.BlinkLocalService;
import kr.poturns.blink.internal.DeviceAnalyzer;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Yeonho.Kim
 * @author Jiwon.Kim
 * @since 2014.08.19
 *
 */
public abstract class BlinkServiceInteraction implements ServiceConnection, IBlinkEventBroadcast {
	private final String tag = "BlinkServiceInteraction";
	private final Context CONTEXT;
	private final EventBroadcastReceiver EVENT_BR;
	private final IntentFilter FILTER;
	
	private IBlinkEventBroadcast mBlinkEventBroadcast;
	private IInternalOperationSupport mInternalOperationSupport;
	private IInternalEventCallback mIInternalEventCallback;
	/**
	 * Application Info
	 */
	String mDeviceName = "";
	String mPackageName = "";
	String mAppName = "";

	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public BlinkServiceInteraction(Context context, IBlinkEventBroadcast iBlinkEventBroadcast,IInternalEventCallback iInternalEventCallback) {
		CONTEXT = context;
		EVENT_BR = new EventBroadcastReceiver();
		FILTER = new IntentFilter();
		
		FILTER.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);		// 블루투스 탐색 시작
		FILTER.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);		// 블루투스 탐색 종료
		
		FILTER.addAction(BROADCAST_DEVICE_DISCOVERED);
		FILTER.addAction(BROADCAST_DEVICE_CONNECTED);
		FILTER.addAction(BROADCAST_DEVICE_DISCONNECTED);
		FILTER.addAction(BROADCAST_DEVICE_IDENTITY_CHANGED);
		
		FILTER.addAction(BROADCAST_CONFIGURATION_CHANGED);
		
		mBlinkEventBroadcast = iBlinkEventBroadcast;
		mIInternalEventCallback = iInternalEventCallback;
		/**
		 * Setting Application Info
		 */
		mDeviceName = Build.MODEL;
		mPackageName = context.getPackageName();
		mAppName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
	}
	public BlinkServiceInteraction(Context context) {
		this(context, null,null);
	}
	
	@Override
	public final void onServiceConnected(ComponentName name, IBinder service) {
		CONTEXT.registerReceiver(EVENT_BR, FILTER);
		
		if (service == null)
			onServiceFailed();
		else {
			mInternalOperationSupport = BlinkSupportBinder.asInterface(service);
			if(mInternalOperationSupport==null){
				onServiceFailed();
				
			}else {
				try {
					if(mIInternalEventCallback!=null)
						mInternalOperationSupport.registerCallback(mIInternalEventCallback);
					
		        } catch (RemoteException e) {
			        e.printStackTrace();
		        }
			}
				
			onServiceConnected(mInternalOperationSupport);
		}
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		CONTEXT.unregisterReceiver(EVENT_BR);
		
		onServiceDisconnected();
	}
	
	public final void startService() {
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra(BlinkLocalService.INTENT_EXTRA_SOURCE_PACKAGE, CONTEXT.getPackageName());
		
		CONTEXT.startService(intent);
		CONTEXT.bindService(intent, this, Context.BIND_AUTO_CREATE);
	}
	
	public final void stopService() {
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra(BlinkLocalService.INTENT_EXTRA_SOURCE_PACKAGE, CONTEXT.getPackageName());
		
		CONTEXT.unbindService(this);
		//CONTEXT.stopService(intent);
	}
	
	public final void startBroadcastReceiver() {
		CONTEXT.registerReceiver(EVENT_BR, FILTER);
	}

	public final void stopBroadcastReceiver() {
		CONTEXT.unregisterReceiver(EVENT_BR);
	}
	
	public final void requestConfigurationChange(String[] keys) {
		if (keys != null) {
			for (String key : keys) {
				
			}
		}
		
		Intent intent = new Intent(BROADCAST_REQUEST_CONFIGURATION_CHANGE);
		CONTEXT.sendBroadcast(intent, PERMISSION_LISTEN_STATE_MESSAGE);
	}
	
	public final void setOnBlinkEventBroadcast(IBlinkEventBroadcast iBlinkEventBroadcast) {
		mBlinkEventBroadcast = iBlinkEventBroadcast;
	}
	
	public final BlinkDevice obtainSelfDevice() {
		try {
			if (mInternalOperationSupport != null)
				return mInternalOperationSupport.obtainSelfDevice();
			
		} catch (RemoteException e) { }
		return null;
	}
	
	private class EventBroadcastReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				onDiscoveryStarted();
				return;
				
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				onDiscoveryFinished();
				return;
			}
			
			
			BlinkDevice device = (BlinkDevice) intent.getSerializableExtra(EXTRA_DEVICE);
			
			if (BROADCAST_DEVICE_DISCOVERED.equals(action)) {
				onDeviceDiscovered(device);
				
			} else if (BROADCAST_DEVICE_CONNECTED.equals(action)) {
				onDeviceConnected(device);
				
			} else if (BROADCAST_DEVICE_DISCONNECTED.equals(action)) {
				onDeviceDisconnected(device);
				
			} else if (BROADCAST_DEVICE_IDENTITY_CHANGED.equals(action)) {
				onIdentityChanged(device.getIdentity());
				
			} else if (BROADCAST_CONFIGURATION_CHANGED.equals(action)) {
				
			}
		}
	}
	
	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 탐색이 시작되었을 때, 호출된다.
	 * 
	 */
	public void onDiscoveryStarted() {}
	
	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 탐색 수행시, 디바이스가 발견되었을 때 호출된다.
	 * <br>Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceDiscovered(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceDiscovered(device);
	}	

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 탐색이 종료되었을 때, 호출된다.
	 * 
	 */
	public void onDiscoveryFinished() {}
	
	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 디바이스가 연결되었을 때 호출된다.
	 * <br>Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceConnected(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceConnected(device);
	}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 디바이스가 해제되었을 때 호출된다.
	 * <br>Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceDisconnected(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceDisconnected(device);

	}
	
	/**
	 * 
	 * @param identity
	 */
	public void onIdentityChanged(DeviceAnalyzer.Identity identity) { }
	
	/**
	 * 
	 */
	public void onConfigurationChanged() { }
	
	
	
	/**
	 * Service에 Binding 되었을 때 호출된다.
	 * 
	 * @param iSupport
	 */
	public abstract void onServiceConnected(IInternalOperationSupport iSupport);
	
	/**
	 * Service에서 Unbinding 되었을 때 호출된다.
	 */
	public abstract void onServiceDisconnected();

	/**
	 * Service에서 Binding이 실패하였을 때 호출된다.
	 */
	public abstract void onServiceFailed();
	
	
	
	/**
	 * Database Interaction
	 */
	
	public boolean registerSystemDatabase(SystemDatabaseObject mSystemDatabaseObject){
		mSystemDatabaseObject.mApp.PackageName = mPackageName;
		mSystemDatabaseObject.mApp.AppName = mAppName;
		mSystemDatabaseObject.mDevice.Device = mDeviceName;
		try {
			mInternalOperationSupport.registerSystemDatabase(mSystemDatabaseObject);
				return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean registerExternalSystemDatabase(SystemDatabaseObject mSystemDatabaseObject){
		try {
			mInternalOperationSupport.registerSystemDatabase(mSystemDatabaseObject);
				return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public SystemDatabaseObject obtainSystemDatabase(){
		return obtainSystemDatabase(mDeviceName,mPackageName);
	}
	
	public SystemDatabaseObject obtainSystemDatabase(String DeviceName,String PackageName){
		try {
			return mInternalOperationSupport.obtainSystemDatabase(DeviceName, PackageName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public List<SystemDatabaseObject> obtainSystemDatabaseAll(){
		try {
			return mInternalOperationSupport.obtainSystemDatabaseAll();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean registerMeasurementData(SystemDatabaseObject mSystemDatabaseObject,Object obj){
		String ClassName = obj.getClass().getName();
		String jsonObj = gson.toJson(obj);
		try {
			mInternalOperationSupport.registerMeasurementData(mSystemDatabaseObject, ClassName,jsonObj);
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public <Object> Object obtainMeasurementData(Class<?> obj,Type type){
		return obtainMeasurementData(obj,null,null,SqliteManager.CONTAIN_DEFAULT,type);
	}
	
	public <Object> Object obtainMeasurementData(Class<?> obj,int ContainType,Type type){
		return obtainMeasurementData(obj,null,null,ContainType,type);
	}
 	public <Object> Object obtainMeasurementData(Class<?> obj,String DateTimeFrom,String DateTimeTo,int ContainType,Type type){
		String ClassName = obj.getName();
		try{
			String json = mInternalOperationSupport.obtainMeasurementData(ClassName, DateTimeFrom, DateTimeTo, ContainType);
			Log.i(tag, "result : "+json);
			return gson.fromJson(json,type);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public List<MeasurementData> obtainMeasurementData(List<Measurement> mMeasurementList,String DateTimeFrom,String DateTimeTo){
		try{
			return mInternalOperationSupport.obtainMeasurementDataById(mMeasurementList, DateTimeFrom, DateTimeTo);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public int removeMeasurementData(Class<?> obj, String DateTimeFrom, String DateTimeTo){
		int ret = 0;
		return ret;
	}
	
	/**
	 * Log Methods
	 */
	public void registerLog(String Device,String App,int Type,String Content){
		try {
			mInternalOperationSupport.registerLog(Device, App, Type, Content);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public List<BlinkLog> obtainLog(String Device,String App,int Type,String DateTimeFrom,String DateTimeTo){
		try {
			return mInternalOperationSupport.obtainLog(Device, App, Type, DateTimeFrom, DateTimeTo);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<BlinkLog> obtainLog(String Device,String App,String DateTimeFrom,String DateTimeTo){
		return obtainLog(Device,App,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(String Device,String DateTimeFrom,String DateTimeTo){
		return obtainLog(Device,null,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(String DateTimeFrom,String DateTimeTo){
		return obtainLog(null,null,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(){
		return obtainLog(null,null,-1,null,null);
	}
	
	public void startFuntion(Function mFunction){
		try {
			mInternalOperationSupport.startFunction(mFunction);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public BlinkServiceInteraction queryDevice(String where) {
	    // TODO Auto-generated method stub
    	try {
	        mInternalOperationSupport.queryDevice(where);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return this;
    }

    public BlinkServiceInteraction queryApp(String where) {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.queryApp(where);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return this;
    }

    public BlinkServiceInteraction queryFunction(String where) {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.queryFunction(where);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return this;
    }

    public BlinkServiceInteraction queryMeasurement(String where)
             {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.queryMeasurement(where);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return this;
    }

    public BlinkServiceInteraction queryMeasurementData(String where) {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.queryMeasurementData(where);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return this;
    }

    public boolean checkInDeviceByMeasureList(List<Measurement> mMeasurementList) {
	    // TODO Auto-generated method stub
	    try {
	        return mInternalOperationSupport.checkInDeviceByMeasureList(mMeasurementList);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return false;
    }

    public boolean checkInDeviceByFunction(Function mFunction) {
	    // TODO Auto-generated method stub
		try {
	        return mInternalOperationSupport.checkInDeviceByFunction(mFunction);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return false;
    }

    public boolean checkInDeviceByClass(Class<?> obj) {
	    // TODO Auto-generated method stub
		try {
			String ClassName = obj.getName();
	        return mInternalOperationSupport.checkInDeviceByClass(ClassName);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return false;
    }

    public List<Device> getDeviceList() {
	    // TODO Auto-generated method stub
		try {
	        return mInternalOperationSupport.getDeviceList();
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return null;
    }

    public void setDeviceList(List<Device> mDeviceList) {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.setDeviceList(mDeviceList);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
    }

    public List<App> getAppList()  {
	    // TODO Auto-generated method stub
		try {
	        return mInternalOperationSupport.getAppList();
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return null;
    }

    public void setAppList(List<App> mAppList) {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.setAppList(mAppList);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
    }

    public List<Function> getFunctionList() {
	    // TODO Auto-generated method stub
		try {
	        return mInternalOperationSupport.getFunctionList();
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return null;
    }

    public void setFunctionList(List<Function> mFunctionList)
             {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.setFunctionList(mFunctionList);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
    }

    public List<Measurement> getMeasurementList()  {
	    // TODO Auto-generated method stub
		try {
	        return mInternalOperationSupport.getMeasurementList();
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return null;
    }

    public void setMeasurementList(List<Measurement> mMeasurementList)
             {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.setMeasurementList(mMeasurementList);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
    }

    public List<MeasurementData> getMeasurementDataList()
             {
	    // TODO Auto-generated method stub
		try {
	        return mInternalOperationSupport.getMeasurementDataList();
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return null;
    }

    public void setMeasurementDataList(
            List<MeasurementData> mMeasurementDataList)  {
	    // TODO Auto-generated method stub
		try {
	        mInternalOperationSupport.setMeasurementDataList(mMeasurementDataList);
        } catch (RemoteException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
    }
}
