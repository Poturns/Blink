package kr.poturns.blink.demo.visualizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class GlassSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	private Context context;
	private PhotoHandler mPhotoHandler;
	private ImageView mPhotoImage;
	
	private boolean isTakingPicture = false;
	private boolean isFacingFront = false;
	
	private Toast mResultToast;
	
	public GlassSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		this.context = context;
		
		View mToastView = LayoutInflater.from(context).inflate(R.layout.photo_toast, null, false);
		mPhotoImage = (ImageView) mToastView.findViewById(R.id.taken_photo);
		
		mResultToast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
		mResultToast.setView(mToastView);
		
		mPhotoHandler = new PhotoHandler(context);
		// mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera = Camera.open(isFacingFront? 
					Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
			mCamera.setPreviewDisplay(mSurfaceHolder);

		} catch (Exception e) {
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
	        int height) {
		if (mCamera == null) {
			try {
				mCamera = Camera.open(isFacingFront? 
						Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
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

		if (mPreviewSizeList == null) {
			mParameters.setPreviewSize(width, height);

			Log.i("tag", "width:" + width + "height:" + height);

		} else {
			int diff = 10000;
			Size opti = null;
			for (Size s : mPreviewSizeList) {
				if (Math.abs(s.height - height) < diff) {
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
	
	public void rotate() {
		isFacingFront = !isFacingFront;
		
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			
			try {
				mCamera = Camera.open(isFacingFront? 
						Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.startPreview();
				
			} catch (IOException e) { ; }
		}
	}

	public void lightOn() {
		if (mCamera == null)
			return;
		
		Camera.Parameters mCameraParameter = mCamera.getParameters();
		List<String> FlashModeList = mCameraParameter.getSupportedFlashModes();
		
		if (FlashModeList.contains(Parameters.FLASH_MODE_TORCH)) {
			mCameraParameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(mCameraParameter);
		
		} else {
			Toast.makeText(context, "플래시를 켤 수 없습니다.", Toast.LENGTH_SHORT).show();
		}
	}

	public void lightOff() {
		if (mCamera == null)
			return;
		
		Camera.Parameters mCameraParameter = mCamera.getParameters();
		List<String> FlashModeList = mCameraParameter.getSupportedFlashModes();
		
		if (FlashModeList.contains(Parameters.FLASH_MODE_OFF)) {
			mCameraParameter.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(mCameraParameter);
		
		} else {
			Toast.makeText(context, "플래시를 끌 수 없습니다.", Toast.LENGTH_SHORT).show();
		}

	}

	
	public synchronized boolean lockTakePicture(){
		if(isTakingPicture){
			return false;
		}
		else {
			isTakingPicture=true;
		}
		return true;
	}
	
	public void takePicture() {
		if (mCamera == null)
			return;
		
		if(lockTakePicture())
			mCamera.takePicture(null, null, mPhotoHandler);
	}

	class PhotoHandler implements PictureCallback {

		private final Context context;

		public PhotoHandler(Context context) {
			this.context = context;
		}

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			File pictureFileDir = getDir();

			if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
				Toast.makeText(context, "사진을 저장할 수 없습니다.", Toast.LENGTH_LONG).show();
				return;

			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
			String date = dateFormat.format(new Date());
			String photoFile = "Picture_" + date + ".jpg";
			String filename = pictureFileDir.getPath() + File.separator
			        + photoFile;

			File pictureFile = new File(filename);

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				mPhotoImage.setImageBitmap(Bitmap.createScaledBitmap(
						bitmap, 
						(int)(bitmap.getWidth() * 0.9), 
						(int)(bitmap.getHeight() * 0.65), 
						false));
				mResultToast.show();
				
				
			} catch (Exception error) {
				Toast.makeText(context, "사진을 저장할 수 없습니다.", Toast.LENGTH_LONG).show();
			}

			camera.startPreview();
			isTakingPicture = false;
		}

		private File getDir() {
			File sdDir = Environment
			        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			return new File(sdDir, "VisualizerAsGlass");
		}
	}
}
