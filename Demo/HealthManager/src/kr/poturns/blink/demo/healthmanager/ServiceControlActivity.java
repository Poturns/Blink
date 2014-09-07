package kr.poturns.blink.demo.healthmanager;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.util.FileUtil;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dev.dworks.libs.actionbartoggle.ActionBarToggle;

/**
 * Service에서 실행 되어, Blink Service와 일부 상호작용하는 {@link Activity}<br>
 * <br>
 * 이 {@link Activity}를 통해 다음과 같은 작업을 수행 할 수 있다. <br>
 * <li>{@link BlinkDevice}의 연결 상태 표시 및 관리</li><br>
 * <li>BlinkDatabase 의 내용 표시</li><br>
 * <li>BlinkService Log 조회</li> <br>
 * <li>
 * Service 설정 값 변경</li>
 */
public final class ServiceControlActivity extends Activity implements
		IServiceContolActivity {
	/** ActionBar 좌 상단의 Toggle Button */
	ActionBarToggle mActionBarToggle;
	SlidingPaneLayout mSlidingPaneLayout;
	/** 왼쪽에 위치한 메뉴 리스트 */
	ListView mLeftListView;
	/** 현재 선택된 메뉴(페이지) 번호 */
	int mCurrentPageSelection = 0;
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mBlinkOperation;
	SqliteManagerExtended mSqliteManagerExtended;
	/** Activity가 유지될 동안 유지할 Fragment, ConnectionFragment 객체이다. */
	Fragment mConnectionFragment;
	/** {@link android.R.attr}에 정의되어있는 ListView의 ChildView의 padding */
	int mListViewChildPaddingStart, mListViewChildPaddingEnd;

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
		mConnectionFragment = new ConnectionFragment();
		mConnectionFragment.setArguments(new Bundle());

		int[] paddingArray = new int[] {
				android.R.attr.listPreferredItemPaddingStart,
				android.R.attr.listPreferredItemPaddingEnd };
		TypedValue typedValue = new TypedValue();
		TypedArray a = obtainStyledAttributes(typedValue.data, paddingArray);
		mListViewChildPaddingStart = a.getDimensionPixelSize(0, 20);
		mListViewChildPaddingEnd = a.getDimensionPixelSize(1, 20);
		a.recycle();
		getFragmentManager()
				.beginTransaction()
				.add(R.id.activity_main_fragment_content, mConnectionFragment,
						"0").hide(mConnectionFragment).commit();
		transitFragment(0, null);
	}

	@Override
	public void transitFragment(final int position, Bundle arguments) {
		Fragment f;
		switch (position) {
		case 1:
			f = new DataSelectFragment();
			break;
		case 2:
			f = new LogViewFragment();
			break;
		case 3:
			f = new PreferenceExternalFragment();
			break;
		case 0:
		default:
			f = mConnectionFragment;
			break;
		}
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		// Fragment가 바뀌기 전 activity에 표시되고 있던 Fragment
		Fragment prevFragment = getFragmentManager().findFragmentByTag(
				String.valueOf(mCurrentPageSelection));

		// 바뀌는 Fragment가 ConnectionFragment가 아닌 경우
		if (position != 0) {
			// 현재 Fragment가 ConnectionFragment가 아닌 경우,
			// FragmentTransaction에서 prevFragment를 삭제한다.
			if (mCurrentPageSelection != 0 && prevFragment != null)
				transaction.remove(prevFragment);
			f.setArguments(arguments);
			f.setHasOptionsMenu(true);
			transaction
					.hide(mConnectionFragment)
					.add(R.id.activity_main_fragment_content, f,
							String.valueOf(position)).show(f)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.commit();
		} else {
			f.getArguments().clear();
			if (arguments != null)
				f.getArguments().putAll(arguments);
			if (prevFragment != null)
				transaction.remove(prevFragment);
			transaction.show(f)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.commit();
		}
		// 리스트 뷰의 선택된 뷰와 이전 뷰의 배경을 바꿔준다.
		View prevSelection = mLeftListView.getChildAt(mCurrentPageSelection);

		if (prevSelection != null) {
			prevSelection.setBackgroundColor(Color.WHITE);
			prevSelection.setPaddingRelative(mListViewChildPaddingStart, 0,
					mListViewChildPaddingEnd, 0);
		}
		View presentSelection = mLeftListView.getChildAt(position);
		if (presentSelection != null) {
			presentSelection
					.setBackgroundResource(R.drawable.drawable_left_list_selected);
			presentSelection.setPaddingRelative(mListViewChildPaddingStart / 2,
					0, mListViewChildPaddingEnd, 0);
		}

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
		// 화면이 작고 / 화면 방향이 '세로 (portrait)' 이며 / 왼쪽 메뉴가 열려 있을 때, 메뉴를 닫는다.
		if (PrivateUtil.isScreenSizeSmall(this)
				&& getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				&& mSlidingPaneLayout.isOpen())
			mSlidingPaneLayout.closePane();
		else
			super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
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
		super.onDestroy();
	}

	/**
	 * {@link ServiceControlActivity#mLeftListView}의 리스너, 메뉴를 터치하였을 때, 화면 전환이
	 * 되도록 한다.
	 */
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