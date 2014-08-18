package kr.poturns.blink.external.preference;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.R;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabWidget;

public class PreferenceActivity extends Activity {
	private ViewPager mViewPager;
	private TabHost mTabHost;
	private List<Integer> mFragmentIdList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_preference);

		mFragmentIdList = new ArrayList<Integer>();
		mFragmentIdList.add(R.string.title_preference_app);
		mFragmentIdList.add(R.string.title_preference_global);

		initTabHost();
		initViewPager();
	}

	/**
	 * TabHost를 초기화 하고, 관련 변수들을 설정한다.
	 */
	private void initTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		TabHost.TabContentFactory factory = new TabHost.TabContentFactory() {

			@Override
			public View createTabContent(String tag) {
				View v = new View(getApplicationContext());
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				v.setTag(tag);
				return v;
			}
		};
		int i = 0;
		for (int id : mFragmentIdList) {
			mTabHost.addTab(mTabHost.newTabSpec(String.valueOf(i++))
					.setIndicator(getString(id)).setContent(factory));
		}

		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				navigateTab(Integer.valueOf(tabId), false);
			}
		});
	}

	/**
	 * ViewPager를 초기화 하고, 관련 변수들을 설정한다.
	 */
	private void initViewPager() {
		PagerAdapter mPagerAdapter = new PreferencePagerAdapter(
				getFragmentManager(), this, mFragmentIdList);
		mViewPager = (ViewPager) findViewById(R.id.activity_preference_viewpager);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						navigateTab(position, true);
					}
				});
		mViewPager.setAdapter(mPagerAdapter);
	}

	/**
	 * ViewPager 또는 TabHost의 tab(page)를 이동한다.
	 * 
	 * @param position
	 *            이동할 tab(page)의 위치
	 * @param isFromPager
	 *            tab(page)이동 event가 ViewPager에서 일어났는지 여부 <br>
	 *            * 무한 루프를 방지하기 위해 사용된다.
	 */
	protected void navigateTab(int position, boolean isFromPager) {
		if (isFromPager) {
			mTabHost.setCurrentTab(position);
		} else {
			mViewPager.setCurrentItem(position);
		}
		centerTabItem(position);
	}

	/**
	 * 현재 선택된 ViewPager의 page에 대응하는 TapHost의 Tab을 화면 가운데로 위치시킨다.
	 * 
	 * @param position
	 *            현재 선택된 Tab / ViewPager 의 위치
	 */
	private void centerTabItem(int position) {
		final TabWidget tabWidget = mTabHost.getTabWidget();

		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		final int screenWidth = size.x;

		final int leftX = tabWidget.getChildAt(position).getLeft();
		int newX = leftX + (tabWidget.getChildAt(position).getWidth() / 2)
				- (screenWidth / 2);
		if (newX < 0) {
			newX = 0;
		}
		View horizontalScrollView;
		if ((horizontalScrollView = (View) mTabHost.getTag()) == null) {
			horizontalScrollView = mTabHost.getChildAt(0).findViewById(
					R.id.activity_preference_horizontalscrollview);
			mTabHost.setTag(horizontalScrollView);
		}
		horizontalScrollView.scrollTo(newX, 0);
	}
}
