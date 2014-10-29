package kr.poturns.blink.demo.visualizer;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class GlassSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	private Context context;
	
	public GlassSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		this.context = context;
		//mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera = Camera.open();
			mCamera.setPreviewDisplay(mSurfaceHolder);
			
		} catch (Exception e) {
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
				mCamera.setPreviewDisplay(mSurfaceHolder);
				
			} catch (Exception e) {
				if (mCamera != null) {
					mCamera.release();
					mCamera = null;
				}
			}
		}
		
		if (mCamera == null)
			return;
		
		Camera.Parameters mParameters = mCamera.getParameters();
		
		List<Size> mPreviewSizeList = mParameters.getSupportedPreviewSizes();
		
		if (mPreviewSizeList == null){
			mParameters.setPreviewSize(width, height);
			
			Log.i("tag", "width:"+width+"height:"+height);
			
		} else {
			int diff = 10000;
			Size opti = null;
			for(Size s : mPreviewSizeList){
				if(Math.abs(s.height - height) < diff) {
					diff = Math.abs(s.height - height);
					opti = s;
				}
			}
			mParameters.setPreviewSize(opti.width, opti.height);
		}
		mCamera.setParameters(mParameters);
		mCamera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
	        mCamera = null;
		}
	}
	
	public void lightOn(){
		if(mCamera==null)return;
		Camera.Parameters mCameraParameter = mCamera.getParameters();
		List<String> FlashModeList = mCameraParameter.getSupportedFlashModes();
		if(FlashModeList.contains(Parameters.FLASH_MODE_TORCH)){
			mCameraParameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(mCameraParameter);
		}
		else {
			Toast.makeText(context, "플래시를 켤 수 없습니다.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void lightOff(){
		if(mCamera==null)return;
		Camera.Parameters mCameraParameter = mCamera.getParameters();
		List<String> FlashModeList = mCameraParameter.getSupportedFlashModes();
		if(FlashModeList.contains(Parameters.FLASH_MODE_OFF)){
			mCameraParameter.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(mCameraParameter);
		}
		else {
			Toast.makeText(context, "플래시를 끌 수 없습니다.", Toast.LENGTH_SHORT).show();
		}
		
	}
}
