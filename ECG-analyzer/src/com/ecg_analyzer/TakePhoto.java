package com.ecg_analyzer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TakePhoto extends Activity implements SensorEventListener{

	// Variables preview camera
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private Camera.Size size = null;
	
	//Variables photo
	private String namePrev = "";
	SaveBuf out = new SaveBuf();
	private MediaPlayer mp;
	
	//Variables para pitch and roll
	private SensorManager sManager;
	float Rot[]=null; //for gravity rotational data
	float I[]=null; //for magnetic rotational data
	float accels[]=new float[3];
	float mags[]=new float[3];
	float[] values = new float[3];
	
	float pitch;
	boolean isRed=true;
	boolean inRange=false;
	LinearLayout ll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_take_photo);
		
		Toast.makeText(this, getString(R.string.TouchScreen), Toast.LENGTH_LONG).show();
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		ll = (LinearLayout) findViewById(R.id.layoutTf);

		preview = (SurfaceView) findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		mp = MediaPlayer.create(this, R.raw.camera_click);
		
		preview.setOnClickListener(new SurfaceView.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(inRange){
					String path = makePhoto(out.getOut());
					if(path!=""){
						shootSound(mp);
						Intent i = new Intent(TakePhoto.this, Photo_Preview.class);
						i.putExtra("photoPath", path);
						i.putExtra("photoName", namePrev);
						TakePhoto.this.recreate();
						startActivity(i);
						
					}
				}else {
					Toast.makeText(TakePhoto.this, getString(R.string.MustHoriz), Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		
		sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
	    sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

		camera = Camera.open();
		startPreview();
	}

	@Override
	public void onPause() {

		if (inPreview) {
			camera.stopPreview();
		}

		camera.setPreviewCallback(null);
		camera.release();
		camera = null;
		inPreview = false;
		super.onPause();
	}
	
	public void shootSound(MediaPlayer mp){
	    AudioManager meng = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
	    int volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);
	    
	    if (volume != 0){
	   
	        if (mp != null)
	            mp.start();
	    }
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}

	private void initPreview(int width, int height) {
		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
				camera.setPreviewCallback(callback);
			} catch (Throwable t) {
				Toast.makeText(TakePhoto.this, t.getMessage(), Toast.LENGTH_LONG)
						.show();
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = camera.getParameters();
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				size = getBestPreviewSize(width, height, parameters);

				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);
					camera.setParameters(parameters);
					cameraConfigured = true;
				}
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.startPreview();
			inPreview = true;
		}
	}

	// Handler para detectar cuando se crea,cambia o se destruye la preview
	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};

	Camera.PreviewCallback callback = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			// Create JPEG

			if (size != null) {
				Rect rectangle = new Rect();
				rectangle.bottom = size.height;
				rectangle.top = 0;
				rectangle.left = 0;
				rectangle.right = size.width;

				YuvImage image = new YuvImage(data, ImageFormat.NV21,
						size.width, size.height, null /* strides */);
				
				ByteArrayOutputStream ba = new ByteArrayOutputStream();
				image.compressToJpeg(rectangle, 71, ba);
				out.setOut(ba.toByteArray());

			}
		}
	};
	
	public String makePhoto(byte [] photoArry){
		
		String currentDateAndTime = new SimpleDateFormat(
				"dd-MM-yyyy-HH-mm-ss").format(new Date());

		String path = Environment.getExternalStorageDirectory()
				+ "/ECG-Analyzer";
		String name = "ECG-" + currentDateAndTime + ".jpg";
		namePrev = name;
		try {
			File photo = new File(path, name);

			FileOutputStream fos = new FileOutputStream(photo);

			fos.write(photoArry);
			fos.close();
			
			return path+"/"+name;
		} catch (java.io.IOException e) {
			System.out.println(e);
		}
	
		return "";
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()){
	    case Sensor.TYPE_MAGNETIC_FIELD:
	        mags = event.values.clone();
	        break;
	    case Sensor.TYPE_ACCELEROMETER:
	        accels = event.values.clone();
	        break;
	    }

	    if (mags != null && accels != null) {
	        Rot = new float[9];
	        I= new float[9];
	        SensorManager.getRotationMatrix(Rot, I, accels, mags);
	        // Correct if screen is in Landscape

	        float[] outR = new float[9];
	        SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_X,SensorManager.AXIS_Z, outR);
	        SensorManager.getOrientation(outR, values);

	        pitch =values[1] * 57.2957795f;
	        mags = null; //retrigger the loop when things are repopulated
	        accels = null; ////retrigger the loop when things are repopulated
	        
	        if (pitch >= 80.0F && pitch < 91.0F && isRed) {
	        	ll.setBackgroundResource(R.drawable.take_foto_border_green);
	        	isRed = false;
	        	inRange = true;
	        }else if (pitch < 80.0F || pitch >= 91.0F){
	        	if(!isRed){
		        	ll.setBackgroundResource(R.drawable.take_foto_border_red);
		        	isRed = true;
		        	inRange = false;
	        	}
	        }
	    }
		
	}

}
