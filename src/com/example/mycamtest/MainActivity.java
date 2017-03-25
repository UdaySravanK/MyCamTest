package com.example.mycamtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private Button button,delButton,donebutton;
	private CameraPreview mPreview;
	private Camera mCamera;
	private Parameters mCamaraParams;
	public static final String TAG = "cam";
	private int picNo =0;
	private FrameLayout preview;
	private RelativeLayout cameraLayout,qualityCheckLayout;
	private MyImageView imageView;
	private File _currentImagefile = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(checkCameraHardware(getApplicationContext()))
		{
			setContentView(R.layout.activity_main);
			button = (Button) findViewById(R.id.button_capture);
	        button.setOnClickListener(this);
	        delButton = (Button) findViewById(R.id.deleteButton);
	        delButton.setOnClickListener(this);
	        donebutton = (Button) findViewById(R.id.doneButton);
	        donebutton.setOnClickListener(this);
	        cameraLayout = (RelativeLayout) findViewById(R.id.cameraLayout);
	        qualityCheckLayout = (RelativeLayout) findViewById(R.id.checkQualityLayout);
	        imageView = (MyImageView) findViewById(R.id.imageView);
	        mCamera = getCameraInstance();
			startCamera();
		}
		else
		{
			TextView tv = new TextView(this);
			tv.setText("This devices has no camera");
			setContentView(tv);
		}
	}

	private void startCamera() 
	{
        if(mCamera !=null)
        {
        	int rotate = getCameraDisplayOrientation(this,1);
        	mCamera.setDisplayOrientation(rotate);
        	mCamaraParams = mCamera.getParameters();
        	mCamaraParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        	mCamaraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        	mCamaraParams.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        	mCamaraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        	mCamaraParams.setExposureCompensation(0);
        	mCamaraParams.setPictureFormat(ImageFormat.JPEG);
        	mCamaraParams.setJpegQuality(100);
        	mCamaraParams.setRotation(90);
        	mPreview = new CameraPreview(this, mCamera);
	        preview = (FrameLayout) findViewById(R.id.camera_preview);
	        preview.removeAllViews();
	        preview.addView(mPreview);
	        mCamera.startPreview();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
			case R.id.button_capture:
				if(mCamera !=null)
					mCamera.autoFocus(new AutoFocusCallback() {
						
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							// TODO Auto-generated method stub
							camera.takePicture(null, null, mPicture);
						}
					});
				break;
			case R.id.deleteButton:
				_currentImagefile.delete();
				cameraLayout.setVisibility(View.VISIBLE);
	        	qualityCheckLayout.setVisibility(View.GONE);
	        	if(mCamera !=null)
	        		mCamera.startPreview();
				break;
			case R.id.doneButton:
				cameraLayout.setVisibility(View.VISIBLE);
	        	qualityCheckLayout.setVisibility(View.GONE);
	        	if(mCamera !=null)
	        		mCamera.startPreview();
				break;
			default:
				break;
		}
	}
	
	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) 
	{
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
	    {
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	/** A safe way to get an instance of the Camera object. */
	public Camera getCameraInstance(){
	    Camera c = null;
	    try 
	    {
	    	releaseCamera();
	    	c = Camera.open(); 
	    }
	    catch (Exception e){
	    	e.printStackTrace();
	    }
	    return c; // returns null if camera is unavailable
	}
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) 
	    {
	    	_currentImagefile = null;
	    	File myPicsFolder = new File(Environment.getExternalStorageDirectory()+File.separator+"MyPics");
	    	if(!myPicsFolder.exists())
	    		myPicsFolder.mkdirs();
	    	BitmapFactory.Options opt = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	        File pictureFile = new File(myPicsFolder.getAbsolutePath()+File.separator+"myImage"+(picNo++)+".jpg");
	        while(pictureFile.exists())
	        {
	        	pictureFile = new File(myPicsFolder.getAbsolutePath()+File.separator+"myImage"+(picNo++)+".jpg");
	        	if(!pictureFile.exists())
	        		break;
	        }
	        try 
	        {
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            bmp.compress(CompressFormat.JPEG, 100, fos);
	            fos.flush();
	            fos.close();
	            _currentImagefile = pictureFile;
	        } 
	        catch (FileNotFoundException e) {
	            Log.d(TAG, "File not found: " + e.getMessage());
	        } catch (IOException e) {
	            Log.d(TAG, "Error accessing file: " + e.getMessage());
	        }
	        if(_currentImagefile!=null)
	        {
	        	try{
	        	BitmapFactory.Options resample = new BitmapFactory.Options();
				resample.inSampleSize = 1;
	        	imageView.setImage(BitmapFactory.decodeFile(_currentImagefile.getAbsolutePath(), resample));
	        	cameraLayout.setVisibility(View.GONE);
	        	qualityCheckLayout.setVisibility(View.VISIBLE);
	        	}catch(Exception e)
	        	{
	        		e.printStackTrace(); 
	        	}
	        }
	        if(mCamera != null)
	        	mCamera.startPreview();
	    }
	};
	
	public int getCameraDisplayOrientation(Activity activity,int cameraId) 
	{
	     android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	     int degrees = 0;
	     switch (rotation) 
	     {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) 
	     {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } 
	     else 
	     {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     return result;
	 }

	@Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try {
			mCamera = getCameraInstance();
			startCamera();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
    private void releaseCamera()
    {
        if (mCamera != null)
        {
        	mCamera.setPreviewCallback(null);
        	mPreview.getHolder().removeCallback(mPreview);
        	mCamera.stopPreview();
        	mCamera.release();        
            mCamera = null;
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	// TODO Auto-generated method stub
    	super.onConfigurationChanged(newConfig);
    	try {
			mCamera = getCameraInstance();
			startCamera();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
}
