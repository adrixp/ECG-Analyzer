package com.ecg_analyzer;

import java.util.ArrayList;
import java.util.List;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class Settings extends PreferenceActivity {
	
	private Camera camera = null;
	Camera.Parameters parameters;
	static List<String> listItemsEntries;
	static List<String> listItemsEntriesVal;
	static int counter =0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		camera = Camera.open();
		parameters = camera.getParameters();
		
		listItemsEntries = new ArrayList<String>();
		listItemsEntriesVal = new ArrayList<String>();
		counter =0;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
        	listItemsEntries.add(size.width+"x"+size.height);
			listItemsEntriesVal.add(Integer.toString(counter));
			counter = counter +1;
		}
		
		
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
	}

	@Override
	public void onPause() {
		counter =0;
		camera.release();
		camera = null;
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
			
			final ListPreference listPreference = (ListPreference) findPreference("typeCal");
			
			setListPreferenceData(listPreference);		
		}
		
		protected static void setListPreferenceData(ListPreference lp) {
			
            final CharSequence[] entries = listItemsEntries.toArray(new CharSequence[listItemsEntries.size()]);
            final CharSequence[] entryValues = listItemsEntriesVal.toArray(new CharSequence[listItemsEntriesVal.size()]);
            lp.setEntries(entries);
            lp.setDefaultValue(3);
            lp.setEntryValues(entryValues);
		}
	}

}
