package com.ecg_analyzer;

import java.io.File;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Photo_Preview extends Activity {
	
	String path;
	String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_photo_preview);
		
		Toast.makeText(this, getString(R.string.PhotoSaved), Toast.LENGTH_LONG).show();

		Bundle extras = getIntent().getExtras();
		path = extras.getString("photoPath");
		name = extras.getString("photoName");

		ImageView iV = (ImageView) findViewById(R.id.photoPrev);

		Uri myUri = Uri.parse(path);
		iV.setImageURI(myUri);
		
		iV.setOnClickListener(new ImageView.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				lanzarOptions();
			}
		});

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_photo_prev, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.config:
			lanzarOptions();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void lanzarOptions() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup p = null;
		View entrada = inflater.inflate(R.layout.layout_op_preview, p);
			
		
		final EditText editRen = (EditText) entrada.findViewById(R.id.EditOpPrev);
		String [] nameArry = name.split("\\.");
		
		String nameNoExt = nameArry[0];
		final String Ext = "."+nameArry[nameArry.length-1];
		editRen.setText(nameNoExt);		
		editRen.setTextKeepState(Ext);
		
		new AlertDialog.Builder(this).setTitle(getString(R.string.sett))
				.setMessage(name)
				.setView(entrada)
				.setPositiveButton(getString(R.string.OptionsMenuFMRen), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if( editRen.getText().toString() == name ){
							Toast.makeText(Photo_Preview.this, getString(R.string.PhotoRen), Toast.LENGTH_LONG).show();
						}else if ( CheckExtension(editRen.getText().toString()+Ext) ){
							File fd = new File(path);
							
						    if(fd.exists()){
						    	fd.renameTo(new File(Environment.getExternalStorageDirectory()
										+ "/ECG-Analyzer/"+editRen.getText().toString()));
						    }
							Toast.makeText(Photo_Preview.this, getString(R.string.PhotoRen), Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(Photo_Preview.this, getString(R.string.PhotoRenFail), Toast.LENGTH_LONG).show();
						}	
					}
				})
				.setNegativeButton(getString(R.string.OptionsMenuFMCancel), null)
				.show();
	}
	
	private enum Extensions {
		png, jpg, bmp, NOVALUE;
				
		public static Extensions toExt(String str)
	    {
	        try {
	            return valueOf(str);
	        }
	        catch (IllegalArgumentException ex) {
	            return NOVALUE;
	        }
	    } 
	}
	
	public boolean CheckExtension (String p){
		String [] s = p.split("\\.");
		switch(Extensions.toExt(s[s.length-1])){
			case png:
				return true;
			case jpg:
				return true;
			case bmp:
				return true;
	        default: 
	        	return false;
	    
		}
	}
	
	public void ActionAnaliz (View btAnalz){
		System.out.println("asasasasasas");
		//SIn Implementar
	}
	
	public void ActionDelete (View btDel){
		File fd = new File(path);
		if(fd.exists()){
	    	fd.delete();
			Toast.makeText(this, getString(R.string.PhotoDisc), Toast.LENGTH_LONG).show();
			finish();
	    }
	}

}
