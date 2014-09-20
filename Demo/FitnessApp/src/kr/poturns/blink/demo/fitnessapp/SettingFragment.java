package kr.poturns.blink.demo.fitnessapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeListener;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.ListView;
import android.widget.Toast;

public class SettingFragment extends PreferenceFragment implements
		SwipeListener {
	ActivityInterface mActivityInterface;
	public static final String KEY_ALERT_HEART_BEAT_IMPACT = "KEY_ALERT_HEART_BEAT_IMPACT";
	public static final String KEY_DELETE_TRAINING_DATA = "KEY_DELETE_TRAINING_DATA";
	public static final String KEY_INBODY_DATA = "KEY_INBODY_DATA";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivityInterface = (ActivityInterface) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		bindPreferenceSummaryToValue();
	}

	private void bindPreferenceSummaryToValue() {
		CheckBoxPreference preference = (CheckBoxPreference) findPreference(KEY_ALERT_HEART_BEAT_IMPACT);
		preference.setChecked(getPreferenceScreen().getSharedPreferences()
				.getBoolean(KEY_ALERT_HEART_BEAT_IMPACT, false));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getView().setBackgroundResource(R.drawable.image_balance);
		ListView listView = (ListView) getView()
				.findViewById(android.R.id.list);
		// TODO 리스트 뷰 구성
		//listView.getch
		listView.setDivider(getResources().getDrawable(android.R.color.transparent));
		listView.setPaddingRelative(10, 30, 10, 30);
		listView.setDividerHeight(5);
		listView.setBackgroundColor(Color.parseColor("#00000000"));
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		String key = preference.getKey();
		if (key.equals(KEY_DELETE_TRAINING_DATA)) {
			SQLiteHelper.getInstance(getActivity()).dropAllTable();
			Toast.makeText(getActivity(), "삭제했습니다.", 1000).show();
			return true;
		} else if (key.equals(KEY_INBODY_DATA)) {
			copy(new File(
					"/data/data/kr.poturns.blink.demo.fitnessapp/databases/fitness"),
					new File(Environment.getExternalStorageDirectory(),
							"fitness.db"));

			return true;
		}
		return false;
	}

	private void copy(File src, File dst) {
		InputStream in = null;
		OutputStream out = null;

		// Transfer bytes from in to out
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception e2) {
				}
			if (out != null)
				try {
					out.close();
				} catch (Exception e2) {
				}
		}
	}

	@Override
	public boolean onSwipe(Direction direction) {
		if (direction == Direction.LEFT_TO_RIGHT) {
			mActivityInterface.returnToMain();
			return true;
		}
		return false;
	}

}
