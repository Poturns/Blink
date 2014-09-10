package kr.poturns.blink.demo.visualizer;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GlassSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	
	
	public GlassSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			
		} catch (IOException e) {
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Camera.Parameters mParameters = mCamera.getParameters();
		List<Size> mPreviewSizeList = mParameters.getSupportedPreviewSizes();
		
		if (mPreviewSizeList == null){
			mParameters.setPreviewSize(width, height);
			String msg;
			msg= "width:"+width+"height:"+height;
			Log.i("tag", msg);
			
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
		mCamera.stopPreview();
        mCamera = null;
	}

}
