package com.ecg_analyzer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

public class TakePhoto extends Activity {

	// Variables camera
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private Camera.Size size = null;
	
	
	private String namePrev = "";
	SaveBuf out = new SaveBuf();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_take_photo);
		
		Toast.makeText(this, getString(R.string.TouchScreen), Toast.LENGTH_LONG).show();

		preview = (SurfaceView) findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		
		preview.setOnClickListener(new SurfaceView.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String path = makePhoto(out.getOut());
				if(path!=""){
					Intent i = new Intent(TakePhoto.this, Photo_Preview.class);
					i.putExtra("photoPath", path);
					i.putExtra("photoName", namePrev);
					TakePhoto.this.recreate();
					startActivity(i);
					
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		
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
				parameters.setFocusMode(Camera.Parameters.FLASH_MODE_ON);
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
		
		String currentDateandTime = new SimpleDateFormat(
				"dd-MM-yyyy-HH-mm-ss").format(new Date());

		String path = Environment.getExternalStorageDirectory()
				+ "/ECG-Analyzer";
		String name = "ECG-" + currentDateandTime + ".jpg";
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

}
