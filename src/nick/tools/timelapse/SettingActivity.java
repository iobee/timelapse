package nick.tools.timelapse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Spinner;

public class SettingActivity extends Activity {
	private static final String TAG = SettingActivity.class.getName();

//	private Spinner spinnerChoose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

//		spinnerChoose = (Spinner) findViewById(R.id.Spinner_Choose);
//		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//				this, R.array.menuitems, android.R.layout.simple_spinner_item);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		spinnerChoose.setAdapter(adapter);

		// getFragmentManager().beginTransaction()
		// .replace(R.id.content, new SettingPreferenceFragment())
		// .commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add("去录制")
			.setIcon(android.R.drawable.stat_sys_upload)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Intent i = new Intent(this, CameraActivity.class);
		startActivity(i);
		return true;
	}
	
	public void showMenu(View v){
		PopupMenu popup = new PopupMenu(this, v);
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		popup.inflate(R.menu.menu_choose);
		popup.show();
	}
}
