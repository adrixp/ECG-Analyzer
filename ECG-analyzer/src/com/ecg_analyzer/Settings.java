package com.ecg_analyzer;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class Settings extends PreferenceActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
	}

	@Override
	public void onPause() {

		Toast.makeText(this, getString(R.string.SavedPref), Toast.LENGTH_LONG)
				.show();
		super.onPause();
	}

	public static class PrefsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.settings);
		}
	}
	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		addPreferencesFromResource(R.xml.settings);
//	}
//	
//	@Override
//	public void onPause() {
//
//		Toast.makeText(this, getString(R.string.SavedPref), Toast.LENGTH_LONG).show();
//		super.onPause();
//	}
//
	

}
