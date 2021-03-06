package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.external.PrivateUtil.DeviceType;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.util.FileUtil;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dev.dworks.libs.actionbartoggle.ActionBarToggle;

/**
 * Service에서 실행 되어, Blink Service와 일부 상호작용하는 {@link android.app.Activity}<br>
 * <br>
 * 이 {@link android.app.Activity}를 통해 다음과 같은 작업을 수행 할 수 있다. <br>
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
	AbsListView mListView;
	/** 현재 선택된 메뉴(페이지) 번호 */
	int mCurrentPageSelection = 0;
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mBlinkOperation;
	SqliteManagerExtended mSqliteManagerExtended;
	/** Activity가 유지될 동안 유지할 Fragment, ConnectionFragment 객체이다. */
	Fragment mConnectionFragment;
	/** {@link android.R.attr}에 정의되어있는 ListView의 ChildView의 padding */
	int mListViewChildPaddingStart, mListViewChildPaddingEnd;
	/**
	 * {@link android.app.FragmentManager}의 BackStack을 삭제하기 위한 boolean 값 <br>
	 * <br>
	 * {@link android.app.Activity}의 처음 실행시에는(
	 * {@link ServiceControlActivity#transitFragment(int, Bundle)}가 최초로 호출됨)
	 * BackStack을 삭제하지 않고, <br>
	 * 나중에 {@link ServiceControlActivity#transitFragment(int, Bundle)}가 호출 될 때
	 * BackStack을 삭제한다.
	 */
	boolean mStartActivity = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FileUtil.createExternalDirectory();
		PrivateUtil.checkDeviceType(this);
		if (PrivateUtil.DEVICE_TYPE == DeviceType.WAREABLE_WATCH) {
			// TODO watch code here
			startActivity(new Intent(this, ServiceControlWatchActivity.class));
			finish();
			return;
		}
		// 기본 화면 설정
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setTheme(android.R.style.Theme_Holo_Light);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
		setTitle(R.string.res_blink_app_name);
		setContentView(R.layout.res_blink_activity_service_control);
		getActionBar().setIcon(R.drawable.res_blink_ic_launcher);

		// 변수 초기화, 뷰 설정
		mSqliteManagerExtended = new SqliteManagerExtended(this);
		mSlidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.res_blink_activity_sliding_layout);
		mSlidingPaneLayout.setSliderFadeColor(Color.TRANSPARENT);
		mListView = (ListView) findViewById(R.id.res_blink_activity_main_left_drawer);
		mActionBarToggle = new ActionBarToggle(this, mSlidingPaneLayout,
				R.drawable.res_blink_ic_navigation_drawer,
				R.string.res_blink_app_name, R.string.res_blink_app_name);

		mListView.setAdapter(ArrayAdapter.createFromResource(this,
				R.array.res_blink_activity_sercive_control_menu_array,
				android.R.layout.simple_list_item_1));
		mListView.setOnItemClickListener(mLeftListViewOnItemClickListener);
		mConnectionFragment = ConnectionFragment.getFragment();
		mConnectionFragment.setArguments(new Bundle());

		int[] paddingArray = new int[] {
				android.R.attr.listPreferredItemPaddingStart,
				android.R.attr.listPreferredItemPaddingEnd };
		TypedValue typedValue = new TypedValue();
		TypedArray a = obtainStyledAttributes(typedValue.data, paddingArray);
		mListViewChildPaddingStart = a.getDimensionPixelSize(0, 20);
		mListViewChildPaddingEnd = a.getDimensionPixelSize(1, 20);
		a.recycle();

		// '연결화면' 설정
		getFragmentManager()
				.beginTransaction()
				.add(R.id.res_blink_activity_main_fragment_content,
						mConnectionFragment, "0").hide(mConnectionFragment)
				.commit();
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
			f = PreferenceExternalFragment.getFragment();
			break;
		case 0:
		default:
			f = mConnectionFragment;
			break;
		}

		if (mStartActivity)
			mStartActivity = !mStartActivity;
		else {
			getFragmentManager().popBackStackImmediate(null,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
					.add(R.id.res_blink_activity_main_fragment_content, f,
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
		View prevSelection = mListView.getChildAt(mCurrentPageSelection);

		if (prevSelection != null) {
			prevSelection.setBackgroundColor(Color.WHITE);
			prevSelection.setPaddingRelative(mListViewChildPaddingStart, 0,
					mListViewChildPaddingEnd, 0);
		}
		View presentSelection = mListView.getChildAt(position);
		if (presentSelection != null) {
			presentSelection
					.setBackgroundResource(R.drawable.res_blink_drawable_left_list_selected);
			presentSelection.setPaddingRelative(mListViewChildPaddingStart / 2,
					0, mListViewChildPaddingEnd, 0);
		}

		mCurrentPageSelection = position;
		getActionBar().setTitle(
				mListView.getItemAtPosition(position).toString());
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

	/**
	 * <hr>
	 * <b>1.</b> SlidingPaneLayout 이 먼저 이벤트를 받는다.<br>
	 * 
	 * <br>
	 * <b>2.</b>위에서 처리가 되지 않은 경우 FragmentManager의 BackStack을 pop한다.
	 * 
	 * <br>
	 * <br>
	 * <b>3.</b>BackStack에서 pop할 것이 없었다면 현재 보여지는 Fragment를 검사한다.
	 * 
	 * <br>
	 * <br>
	 * <b>4.</b>현재 보여지는 Fragment가 ConnectionFragment이면 종료하고,<br>
	 * 아니면 ConnectionFragment 화면으로 이동한다.<br>
	 * */
	@Override
	public void onBackPressed() {
		// 1.화면이 작다.
		// 2. 화면 방향이 '세로 (portrait)' 이다.
		// 3. 왼쪽 메뉴가 열려 있다.
		// 위 세 조건을 모두 만족할 때, 왼쪽메뉴는 닫는다.
		// 위 조건을 하나라도 만족하지 않는 경우는 대개 왼쪽 메뉴가 항시 열려있는 상태이므로
		// 왼쪽 메뉴를 닫을 필요가 없다.
		if (PrivateUtil.isScreenSizeSmall(this)
				&& getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				&& mSlidingPaneLayout.isOpen())
			mSlidingPaneLayout.closePane();
		else {
			// backstack에 저장되어 있는 Fragment 복귀
			if (!getFragmentManager().popBackStackImmediate()) {
				// 복귀할 것이 없을 때,
				// 현재 Fragment가 ConnectionFragment가 아닌 경우
				// ConnectionFragment로 이동한다.
				if (mCurrentPageSelection != 0) {
					transitFragment(0, null);
				} else {
					finish();
				}
			}
		}

	}

	@Override
	protected void onResume() {
		// if (mInteraction != null)
		// mInteraction.startBroadcastReceiver();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mInteraction != null) {
			try {
				mInteraction.stopService();
				mInteraction.stopBroadcastReceiver();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mSqliteManagerExtended != null)
			mSqliteManagerExtended.close();
		super.onDestroy();
	}

	/**
	 * {@link ServiceControlActivity#mListView}의 리스너, 메뉴를 터치하였을 때, 화면 전환이 되도록
	 * 한다.
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