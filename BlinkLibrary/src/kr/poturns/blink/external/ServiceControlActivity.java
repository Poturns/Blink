package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.external.preference.PreferenceActivity;
import kr.poturns.blink.external.tab.connectionview.CircularConnectionFragment;
import kr.poturns.blink.external.tab.dataview.DataViewFragment;
import kr.poturns.blink.external.tab.logview.LogViewFragment;
import kr.poturns.blink.util.FileUtil;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dev.dworks.libs.actionbartoggle.ActionBarToggle;

public final class ServiceControlActivity extends Activity implements
		IServiceContolActivity {
	ActionBarToggle mActionBarToggle;
	SlidingPaneLayout mSlidingPaneLayout;
	ListView mLeftListView;
	int mCurrentPageSelection = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FileUtil.createExternalDirectory();
		setContentView(R.layout.activity_service_control);
		mSlidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.activity_sliding_layout);
		mSlidingPaneLayout.setSliderFadeColor(Color.TRANSPARENT);
		mLeftListView = (ListView) findViewById(R.id.activity_main_left_drawer);
		mActionBarToggle = new ActionBarToggle(this, mSlidingPaneLayout,
				R.drawable.ic_navigation_drawer, R.string.app_name,
				R.string.app_name);

		mLeftListView.setAdapter(ArrayAdapter.createFromResource(this,
				R.array.activity_sercive_control_menu_array,
				android.R.layout.simple_list_item_1));
		mLeftListView.setOnItemClickListener(mLeftListViewOnItemClickListener);
		transitFragment(0, null);
	}

	@Override
	public void transitFragment(int position, Bundle arguments) {
		String fname;
		switch (position) {
		default:
			fname = CircularConnectionFragment.class.getName();
			break;
		case 1:
			fname = DataViewFragment.class.getName();
			break;
		case 2:
			fname = LogViewFragment.class.getName();
			break;
		}
		Fragment f;
		try {
			f = Fragment.instantiate(this, fname, arguments);
		} catch (Exception e) {
			return;
		}

		getFragmentManager().beginTransaction()
				.replace(R.id.activity_main_fragment_content, f)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();

		mCurrentPageSelection = position;
		getActionBar().setTitle(
				mLeftListView.getItemAtPosition(position).toString());
	}

	protected void closePane() {
		mSlidingPaneLayout.closePane();
	}

	protected static void startPreferenceActivity(Context context) {
		context.startActivity(new Intent(context, PreferenceActivity.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mActionBarToggle.syncState();
		getActionBar().setDisplayHomeAsUpEnabled(
				mActionBarToggle.isDrawerIndicatorEnabled());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mActionBarToggle.onConfigurationChanged(newConfig);
		getActionBar().setDisplayHomeAsUpEnabled(
				mActionBarToggle.isDrawerIndicatorEnabled());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mSlidingPaneLayout.isOpen())
				mSlidingPaneLayout.closePane();
			else
				mSlidingPaneLayout.openPane();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (mSlidingPaneLayout.isOpen())
			mSlidingPaneLayout.closePane();
		else
			super.onBackPressed();
	}

	private AdapterView.OnItemClickListener mLeftListViewOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (!mSlidingPaneLayout.isOpen())
				return;
			if (mCurrentPageSelection == position) {
				closePane();
				return;
			}

			switch (position) {
			default: // connection / data / log
				transitFragment(position, null);
				break;
			case 3: // setting
				startPreferenceActivity(ServiceControlActivity.this);
				break;
			}
			closePane();
		}
	};

}
