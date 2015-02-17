package com.ecg_analyzer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowLog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log);

		TextView txtView = (TextView) findViewById(R.id.logView);
		
		String log;
		String aux = "";
		try {
			BufferedReader ficheroRead = new BufferedReader(new InputStreamReader(
					openFileInput("Log.txt")));					
			
			while ((log = ficheroRead.readLine()) != null) {
				
				aux = aux + log;
				
			}
			ficheroRead.close();
			txtView.setText(aux);
		} catch (Exception ex) {
			txtView.setText("error en ShowLog.java a la hora de leer");
		}

		

	}
}
