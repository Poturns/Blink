package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
/** ServiceControlActivity가 관리하는 자원을 다른 Fragment 클래스가 참조할 수 있게 하는 Interface 클래스. */
interface IServiceContolActivity {
	/**
	 * Fragment를 전환한다.
	 * 
	 * @param position
	 *            현재 Activity에서의 List의 번호
	 * @param arguments
	 *            추가적으로 전달할 arguments
	 * 
	 */
	public void transitFragment(int position, Bundle arguments);

	public BlinkServiceInteraction getServiceInteration();

	public void setServiceInteration(BlinkServiceInteraction interaction);

	public void setInternalOperationSupport(
			IInternalOperationSupport blinkOperation);

	public IInternalOperationSupport getInternalOperationSupport();

	public SqliteManagerExtended getDatabaseHandler();
}
/**
 * {@code ServiceControlActivity}의 기능을 대리 수행하는 클래스<br>
 * <br>
 */
abstract class ServiceControlActivityDelegate implements IServiceContolActivity {
	static ServiceControlActivityDelegate createDelegate(
			ServiceControlActivity activity) {

		switch (PrivateUtil.checkDeviceType(activity)) {
		default:
		case HANDHELD_PHONE:
		case HANDHELD_TABLET:
			return new ServiceControlActivityDelegateHandheld(activity);
		case WAREABLE_WATCH:
			return new ServiceControlActivityDelegateWatch(activity);
		}
	}

	ServiceControlActivity mActivity;
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mBlinkOperation;
	SqliteManagerExtended mSqliteManagerExtended;

	ServiceControlActivityDelegate(ServiceControlActivity activity) {
		this.mActivity = activity;
		mSqliteManagerExtended = new SqliteManagerExtended(mActivity);
	}

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

	abstract void onCreate(Bundle savedInstanceState);

	void onPostCreate(Bundle savedInstanceState) {
	}

	void onConfigurationChanged(Configuration newConfig) {
	}

	boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	void onBackPressed() {
	}

	void onResume() {
		if (mInteraction != null) {
			try {
				mInteraction.startBroadcastReceiver();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void onPause() {
		if (mInteraction != null) {
			try {
				mInteraction.stopBroadcastReceiver();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void onDestroy() {
		if (mInteraction != null) {
			try {
				mInteraction.stopService();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (mSqliteManagerExtended != null)
			mSqliteManagerExtended.close();
	};

	boolean onTouchEvent(MotionEvent ev) {
		return false;
	}

	boolean dispatchTouchEvent(MotionEvent ev) {
		return false;
	}
}

/**
 * 
 * 이 {@link ServiceControlActivityDelegate}를 통해 다음과 같은 작업을 수행 할 수 있다. <br>
 * <li>{@link BlinkDevice}의 연결 상태 표시 및 관리</li><br>
 * <li>BlinkDatabase 의 내용 표시</li><br>
 * <li>BlinkService Log 조회</li> <br>
 * <li>Service 설정 값 변경</li>
 */
class ServiceControlActivityDelegateHandheld extends
		ServiceControlActivityDelegate {
	/** ActionBar 좌 상단의 Toggle Button */
	ActionBarDrawerToggle mActionBarToggle;
	DrawerLayout mDrawerLayout;
	/** 왼쪽에 위치한 메뉴 리스트 */
	AbsListView mListView;
	/** 현재 선택된 메뉴(페이지) 번호 */
	int mCurrentPageSelection = 0;
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

	ServiceControlActivityDelegateHandheld(ServiceControlActivity activity) {
		super(activity);
	}

	@Override
	void onCreate(Bundle savedInstanceState) {
		// 기본 화면 설정
		mActivity.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mActivity.requestWindowFeature(Window.FEATURE_ACTION_BAR);
		mActivity.setTheme(android.R.style.Theme_Holo_Light);
		mActivity
				.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
		mActivity.setTitle(R.string.res_blink_app_name);
		mActivity.setContentView(R.layout.res_blink_activity_service_control);
		mActivity.getActionBar().setIcon(R.drawable.res_blink_ic_launcher);

		// 변수 초기화, 뷰 설정

		mDrawerLayout = (DrawerLayout) mActivity
				.findViewById(R.id.res_blink_activity_drawer_layout);
		mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {

			@Override
			public void onDrawerStateChanged(int newState) {
				mActionBarToggle.onDrawerStateChanged(newState);
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				mActionBarToggle.onDrawerSlide(drawerView, slideOffset);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				mActionBarToggle.onDrawerOpened(drawerView);
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				mActionBarToggle.onDrawerClosed(drawerView);
			}
		});
		mListView = (ListView) mActivity
				.findViewById(R.id.res_blink_activity_main_left_drawer);
		mActionBarToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout,
				R.drawable.res_blink_ic_navigation_drawer,
				R.string.res_blink_app_name, R.string.res_blink_app_name);

		mListView.setAdapter(ArrayAdapter.createFromResource(mActivity,
				R.array.res_blink_activity_sercive_control_menu_array,
				android.R.layout.simple_list_item_1));
		mListView.setOnItemClickListener(mLeftListViewOnItemClickListener);
		mConnectionFragment = ConnectionFragment.getFragment();
		mConnectionFragment.setArguments(new Bundle());

		int[] paddingArray = new int[] {
				android.R.attr.listPreferredItemPaddingStart,
				android.R.attr.listPreferredItemPaddingEnd };
		TypedValue typedValue = new TypedValue();
		TypedArray a = mActivity.obtainStyledAttributes(typedValue.data,
				paddingArray);
		mListViewChildPaddingStart = a.getDimensionPixelSize(0, 20);
		mListViewChildPaddingEnd = a.getDimensionPixelSize(1, 20);
		a.recycle();

		// '연결화면' 설정
		mActivity
				.getFragmentManager()
				.beginTransaction()
				.add(R.id.res_blink_activity_main_fragment_content,
						mConnectionFragment, "0").hide(mConnectionFragment)
				.commit();
		transitFragment(0, null);
	}

	/**
	 * <hr>
	 * <b>1.</b> DrawerLayout 이 먼저 이벤트를 받는다.<br>
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
	void onBackPressed() {
		// 1.화면이 작다.
		// 2. 화면 방향이 '세로 (portrait)' 이다.
		// 3. 왼쪽 메뉴가 열려 있다.
		// 위 세 조건을 모두 만족할 때, 왼쪽메뉴는 닫는다.
		// 위 조건을 하나라도 만족하지 않는 경우는 대개 왼쪽 메뉴가 항시 열려있는 상태이므로
		// 왼쪽 메뉴를 닫을 필요가 없다.
		if (PrivateUtil.isScreenSizeSmall(mActivity)
				&& mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				&& mDrawerLayout.isDrawerOpen(mListView))
			mDrawerLayout.closeDrawer(mListView);
		else {
			// backstack에 저장되어 있는 Fragment 복귀
			if (!mActivity.getFragmentManager().popBackStackImmediate()) {
				// 복귀할 것이 없을 때,
				// 현재 Fragment가 ConnectionFragment가 아닌 경우
				// ConnectionFragment로 이동한다.
				if (mCurrentPageSelection != 0) {
					transitFragment(0, null);
				} else {
					mActivity.finish();
				}
			}
		}
	}

	@Override
	boolean onOptionsItemSelected(MenuItem item) {
		if (mActionBarToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		default:
			return false;
		}
	}

	@Override
	void onConfigurationChanged(Configuration newConfig) {
		mActionBarToggle.onConfigurationChanged(newConfig);
		mActivity.getActionBar().setDisplayHomeAsUpEnabled(
				mActionBarToggle.isDrawerIndicatorEnabled());
	}

	@Override
	void onPostCreate(Bundle savedInstanceState) {
		mActionBarToggle.syncState();
		mActivity.getActionBar().setDisplayHomeAsUpEnabled(
				mActionBarToggle.isDrawerIndicatorEnabled());
	}

	@Override
	public void transitFragment(int position, Bundle arguments) {
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
			mActivity.getFragmentManager().popBackStackImmediate(null,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}

		FragmentTransaction transaction = mActivity.getFragmentManager()
				.beginTransaction();
		// Fragment가 바뀌기 전 activity에 표시되고 있던 Fragment
		Fragment prevFragment = mActivity.getFragmentManager()
				.findFragmentByTag(String.valueOf(mCurrentPageSelection));

		// 바뀌는 Fragment가 ConnectionFragment가 아닌 경우
		if (position != 0) {
			mConnectionFragment.setUserVisibleHint(false);
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
			f.setUserVisibleHint(true);
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
		mActivity.getActionBar().setTitle(
				mListView.getItemAtPosition(position).toString());
	}

	/**
	 * {@link ServiceControlActivity#mListView}의 리스너, 메뉴를 터치하였을 때, 화면 전환이 되도록
	 * 한다.
	 */
	private AdapterView.OnItemClickListener mLeftListViewOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (!mDrawerLayout.isDrawerOpen(mListView))
				return;
			if (mCurrentPageSelection == position) {
				mDrawerLayout.closeDrawer(mListView);
				return;
			}
			// connection / data / log / setting fragment
			transitFragment(position, null);
			mDrawerLayout.closeDrawer(mListView);
		}
	};
}
