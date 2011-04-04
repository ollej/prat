package com.jxdevelopment.droidprat;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SetPrefs extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
