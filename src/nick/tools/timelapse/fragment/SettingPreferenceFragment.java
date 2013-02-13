package nick.tools.timelapse.fragment;

import nick.tools.timelapse.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class SettingPreferenceFragment extends PreferenceFragment implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String KEY_OUTPUT_FORMAT = "lp_CaptureOutput";
	public static final String KEY_CAPTURE_SCREENSIZE = "lp_CaptureSize";
	public static final String KEY_CAPTURE_FRAMRATE = "etp_FrameRate";

	private ListPreference lpOutput;
	private ListPreference lpCaptureSize;
	private EditTextPreference etpFramRate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		init();
	}

	private void init() {
		lpOutput = (ListPreference) findPreference(KEY_OUTPUT_FORMAT);
		lpCaptureSize = (ListPreference) findPreference(KEY_CAPTURE_SCREENSIZE);
		etpFramRate = (EditTextPreference) findPreference(KEY_CAPTURE_FRAMRATE);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if (key.equals(KEY_OUTPUT_FORMAT)) {
			lpOutput.setSummary(lpOutput.getEntry());
		} else if(key.equals(KEY_CAPTURE_FRAMRATE)){
			etpFramRate.setSummary(sharedPreferences.getString(key, "") + "s");
		} else if(key.equals(KEY_CAPTURE_SCREENSIZE)){
			lpCaptureSize.setSummary(lpCaptureSize.getEntry());
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

}
