package kr.poturns.blink.external;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.db.SqliteManagerExtended;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.util.FileUtil;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dev.dworks.libs.actionbartoggle.ActionBarToggle;

public final class ServiceControlActivity extends Activity implements
		IServiceContolActivity {
	// private static final String TAG = ServiceControlActivity.class
	// .getSimpleName();
	ActionBarToggle mActionBarToggle;
	SlidingPaneLayout mSlidingPaneLayout;
	ListView mLeftListView;
	int mCurrentPageSelection = 0;
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mBlinkOperation;
	SqliteManagerExtended mSqliteManagerExtended;
	List<Fragment> mFragmentList = new ArrayList<Fragment>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		FileUtil.createExternalDirectory();
		setContentView(R.layout.activity_service_control);
		mSqliteManagerExtended = new SqliteManagerExtended(this);
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
		mFragmentList.add(new ConnectionFragment());
		mFragmentList.add(new DataSelectFragment());
		mFragmentList.add(new LogViewFragment());
		mFragmentList.add(new PreferenceExternalFragment());
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		int i = 0;
		for (Fragment f : mFragmentList) {
			f.setArguments(new Bundle());
			transaction.add(R.id.activity_main_fragment_content, f,
					String.valueOf(i++)).hide(f);
		}
		transaction.commit();
		transitFragment(0, null);
	}

	@Override
	public void transitFragment(int position, Bundle arguments) {
		Fragment currentFragment = mFragmentList.get(mCurrentPageSelection);
		Fragment shownFragment = mFragmentList.get(position);
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		if (currentFragment != null) {
			transaction.hide(currentFragment);
		}
		if (shownFragment != null) {
			Bundle fragmentArg = shownFragment.getArguments();
			if (fragmentArg != null && arguments != null) {
				fragmentArg.clear();
				fragmentArg.putAll(arguments);
			}
			transaction.show(shownFragment);
		}
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
		mCurrentPageSelection = position;
		getActionBar().setTitle(
				mLeftListView.getItemAtPosition(position).toString());
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
		if (PrivateUtil.isScreenSizeSmall(this) && mSlidingPaneLayout.isOpen())
			mSlidingPaneLayout.closePane();
		else
			super.onBackPressed();
	}

	@Override
	public void finish() {
		if (mInteraction != null) {
			try {
				mInteraction.stopService();
				mInteraction.stopBroadcastReceiver();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (mSqliteManagerExtended != null)
			mSqliteManagerExtended.close();
		super.finish();
	}

	private AdapterView.OnItemClickListener mLeftListViewOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (!mSlidingPaneLayout.isOpen())
				return;
			if (mCurrentPageSelection == position) {
				mSlidingPaneLayout.closePane();
				return;
			}

			// connection / data / log / setting fragment
			transitFragment(position, null);
			mSlidingPaneLayout.closePane();
		}
	};

	@Override
	public BlinkServiceInteraction getServiceInteration() {
		return mInteraction;
	}

	@Override
	public void setServiceInteration(BlinkServiceInteraction interaction) {
		mInteraction = interaction;
	}

	@Override
	public SqliteManagerExtended getDatabaseHandler() {
		return mSqliteManagerExtended;
	}

	@Override
	public void setInternalOperationSupport(
			IInternalOperationSupport blinkOperation) {
		mBlinkOperation = blinkOperation;
	}

	@Override
	public IInternalOperationSupport getInternalOperationSupport() {
		return mBlinkOperation;
	}

}
