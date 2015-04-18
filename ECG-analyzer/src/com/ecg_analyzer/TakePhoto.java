package com.ecg_analyzer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
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
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class TakePhoto extends Activity implements SensorEventListener{

	// Variables preview camera
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private Camera.Size size = null;
	private int numCompress;
	private int numBestPrev;
	private int numTipoFormat;
	private int flashOn;
	
	//Checkar lo de la extension
	//checkar bmp
	
	//hacer el rectangulo
	//OpenCV
	
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
	FrameLayout ll;
	OutputStreamWriter fichLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_take_photo);
		
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		ll = (FrameLayout) findViewById(R.id.layoutTf);

		preview = (SurfaceView) findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		mp = MediaPlayer.create(this, R.raw.camera_click);
		
		try {
			fichLog = new OutputStreamWriter(openFileOutput(
					"Log.txt", Context.MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		DrawView.points = new Point[4];
		DrawView.colorballs = new ArrayList<ColorBall>();
		
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		//optima para jpg 71, para png se ignora
		numCompress = Integer.parseInt(pref.getString("typeComp", "71")+"0");
		numBestPrev = Integer.parseInt(pref.getString("typeCal", "3"));
		//0 png, 1 jpg , 2 bmp
		numTipoFormat = Integer.parseInt(pref.getString("typeFormat", "1"));
		flashOn = Integer.parseInt(pref.getString("setFlash", "0"));
		
		sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
	    sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

		camera = Camera.open();
		startPreview();
	}

	@Override
	public void onPause() {
		
		try {
			fichLog.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (inPreview) {
			camera.stopPreview();
		}
		
		DrawView.points[3] = null;
		DrawView.colorballs.clear();
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
	
	public void butTakePhoto(View view) {
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
		
		Toast.makeText(this, getString(R.string.Optimal)+" "+result.width+"x"+result.height, Toast.LENGTH_LONG).show();
		
		return parameters.getSupportedPreviewSizes().get(numBestPrev);

	}
	
	public void writeInLog(String s){
		try {
			fichLog.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				parameters.setJpegQuality(numCompress);
				if(flashOn == 1){
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
				}
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
				out.setOut(data);
			}
		}
	};
	
	//probar a eliminar todos los onclick poco a poco.
	//probar si no a hacer getX del fichero a.llava
	//generar un .raw con y sin decode
	//Hacer y enviar fotos a inma
	//1º detec paralelo, 2º make rect
	//Habilitar flash o no?
	//Revisar el default de los dpi
	//Nombre de las fotos.(revisado para probar)
	//investigar el no dejar echar foto hasta enfocado.
	//Mirar lo del jpeg, en los parametros de la camara hay gets interesantes (getImgeformat..)
	//Probar android studio
	
	//ver que pasa ahora con el setjpg quality
	//Enviar a inma un correo con las Images supported a ver que sabe ella, el .raw y las fotos.
	
	public String makePhoto(byte [] photoArry){
		
		String retorno = "";
		
		if (size != null) {
			Rect rectangle = new Rect();
			if(DrawView.points[0] != null){
				rectangle = returnRectangle(rectangle,true);
			}else{
				rectangle = returnRectangle(rectangle,false);
			}
			
	
			YuvImage image = new YuvImage(photoArry, ImageFormat.NV21,
					size.width, size.height, null /* strides */);
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			try{
				image.compressToJpeg(rectangle, numCompress, ba);
			}catch (Exception e){
				e.printStackTrace();
			}
			
			
			//0 png, 1 jpg(else=default) , 2 bmp
			if(numTipoFormat == 0 ){ //png compressed and lossless
				Bitmap bmp = BitmapFactory.decodeByteArray(ba.toByteArray(), 0, ba.toByteArray().length);
				if (bmp!=null){
					ByteArrayOutputStream bapng = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.PNG, numCompress, bapng);
					
					retorno = makePhotoFile(bapng.toByteArray(),".png","-png-",100);
				}
			}else if(numTipoFormat == 2){ //bmp compressless and lossless
				Bitmap bmp = BitmapFactory.decodeByteArray(ba.toByteArray(), 0, ba.toByteArray().length);
				if (bmp!=null){
					int bytes = bmp.getByteCount();
	
					ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
					bmp.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

					retorno = makePhotoFile(buffer.array(),".bmp","-bmp-",0);
				}
			}else{	//jpg compressed and lossless 
				retorno = makePhotoFile(ba.toByteArray(),".jpg","-jpg-",numCompress);
				
			}
			
			
		}
		return retorno;
	}
	
	public Rect returnRectangle(Rect rectangle,Boolean haveRect){
		if(haveRect){
			rectangle.left = DrawView.points[0].x;
			rectangle.top = DrawView.points[0].y;
			rectangle.right = DrawView.points[0].x;
			rectangle.bottom = DrawView.points[0].y;
	        for (int i = 1; i < DrawView.points.length; i++) {
	        	rectangle.left = rectangle.left > DrawView.points[i].x ? DrawView.points[i].x:rectangle.left;
	        	rectangle.top = rectangle.top > DrawView.points[i].y ? DrawView.points[i].y:rectangle.top;
	        	rectangle.right = rectangle.right < DrawView.points[i].x ? DrawView.points[i].x:rectangle.right;
	        	rectangle.bottom = rectangle.bottom < DrawView.points[i].y ? DrawView.points[i].y:rectangle.bottom;
	        }
	        
			rectangle.bottom = rectangle.bottom + DrawView.colorballs.get(2).getWidthOfBall() / 2;
			rectangle.top = rectangle.top + DrawView.colorballs.get(0).getWidthOfBall() / 2;
			rectangle.left = rectangle.left + DrawView.colorballs.get(0).getWidthOfBall() / 2;
			rectangle.right = rectangle.right + DrawView.colorballs.get(2).getWidthOfBall() / 2;
		}else{
			rectangle.bottom = size.height;
			rectangle.top = 0;
			rectangle.left = 0;
			rectangle.right = size.width;
		}
		
		return rectangle;
	}
	
	public String makePhotoFile(byte [] photoArry, String extension,String extensionSinp,int compresion){
		String currentDateAndTime = new SimpleDateFormat(
				"dd-MM-yyyy-HH-mm-ss").format(new Date());

		String path = Environment.getExternalStorageDirectory()
				+ "/ECG-Analyzer";
		String name = "ECG-" + currentDateAndTime + extensionSinp + compresion + extension;
		namePrev = name;
		try {
			File photo = new File(path, name);

			FileOutputStream fos = new FileOutputStream(photo);

			fos.write(photoArry);
			fos.close();
			
			return path+"/"+name;
		} catch (Exception e) {
			e.printStackTrace();
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
