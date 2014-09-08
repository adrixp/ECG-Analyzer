package com.ecg_analyzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Cover extends Activity {
	
	//Time for the splash
	private final int SPLASH_DISPLAY_LENGTH = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_cover);
		
		/* New Handler to start the Menu-Activity 
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(Cover.this,Menu.class);
                Cover.this.startActivity(mainIntent);
                Cover.this.finish();
                
            // transition from splash to main menu.
            //spashfadeout = max splash_dispaly_length
            //activityfadein = max splash_dispaly_length / 2
            overridePendingTransition(R.layout.activityfadein,
                    R.layout.splashfadeout);
            }
        }, SPLASH_DISPLAY_LENGTH);
		
	}

}
