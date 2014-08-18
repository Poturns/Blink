package kr.poturns.blink.external.preference;

import java.lang.ref.WeakReference;
import java.util.List;

import kr.poturns.blink.R;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

public class PreferencePagerAdapter extends FragmentPagerAdapter {
	private List<Integer> mFragmentIdList;
	private WeakReference<Context> mContextRef;

	public PreferencePagerAdapter(FragmentManager supportManager,
			Context context, List<Integer> fragmentIdList) {
		super(supportManager);
		mFragmentIdList = fragmentIdList;
		mContextRef = new WeakReference<Context>(context);
	}

	@Override
	public Fragment getItem(int position) {
		return Fragment
				.instantiate(mContextRef.get(),
						retrieveFragmentFromId(mFragmentIdList.get(position))
								.getName());
	}

	@Override
	public int getCount() {
		return mFragmentIdList.size();
	}

	private static Class<? extends Fragment> retrieveFragmentFromId(
			Integer fragmentId) {
		final int id = fragmentId.intValue();
		if (id == R.string.title_preference_global)
			return GlobalPreferenceFragment.class;
		else if (id == R.string.title_preference_app) {
			return AppPreferenceFragment.class;
		} else {
			return AppPreferenceFragment.class;
		}
	}

}
