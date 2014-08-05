package kr.poturns.blink.external.tab.connectionview;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.poturns.blink.R;
import kr.poturns.blink.external.IServiceContolActivity;
import kr.poturns.blink.external.tab.connectionview.CircularViewHelper.OnDragAndDropListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public final class CircularConnectionFragment extends Fragment {
	/** 원형으로 배치되는 View들의 parent View */
	private ViewGroup mViewGroup;
	/** 가운데에 배치되는 View */
	private TextView mHostView;
	/** 각 Device의 간략한 정보를 나타내는 Dialog */
	private AlertDialog mSimpleInfoDialog;
	/** mSimpleInfoDialog의 구체적인 내용이 표현되는 View */
	private TextView mDialogContentTextView;
	/** layout 하단의 submenu 역할을 하는 SlidingDrawer */
	private SlidingDrawer mSlidingDrawer;
	private SeekBar mSeekBar;
	protected CircularViewHelper mCircularHelper;
	private int random;
	protected IServiceContolActivity mActivityInterface;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof IServiceContolActivity) {
			mActivityInterface = (IServiceContolActivity) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mDialogContentTextView = new TextView(getActivity());
		mDialogContentTextView.setGravity(Gravity.CENTER);
		mDialogContentTextView.setPadding(20, 20, 20, 20);
		mSimpleInfoDialog = new AlertDialog.Builder(getActivity())
				.setView(mDialogContentTextView)
				.setPositiveButton(android.R.string.ok, null).create();
		random = new Random(System.currentTimeMillis()).nextInt(5) + 1;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mViewGroup = (ViewGroup) View.inflate(getActivity(),
				R.layout.fragment_circular_connection, null);
		mCircularHelper = new CircularViewHelper(mViewGroup, android.R.id.text1);
		mHostView = (TextView) mCircularHelper.getCenterView();
		mHostView.setText("Host");
		mHostView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				mCircularHelper.addChildViews(generateViews());
				mCircularHelper.drawCircularView();
				Toast.makeText(getActivity(), "connection refresh!",
						Toast.LENGTH_SHORT).show();
				random = new Random(System.currentTimeMillis()).nextInt(5) + 1;
				mSeekBar.setProgress(0);
				return true;
			}
		});
		mCircularHelper.setOnDragAndDropListener(mDragAndDropListener);
		mCircularHelper.addChildViews(generateViews());
		mCircularHelper.drawCircularView();

		mSlidingDrawer = (SlidingDrawer) mViewGroup
				.findViewById(R.id.fragment_circular_sliding_drawer);
		mSeekBar = (SeekBar) mSlidingDrawer
				.findViewById(R.id.fragment_circular_seekbar);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				setFilteringViewAlpha(progress);
			}
		});

		return mViewGroup;
	}

	protected void setFilteringViewAlpha(int percent) {
		List<View> list = mCircularHelper.getChildViews();
		int size = list.size();

		for (int i = 0; i < size; i++) {
			View v = list.get(i);
			if (checkDeviceFilteringCondition(i))
				v.setAlpha(((float) (100 - percent) / 100f));
		}
	}

	private boolean checkDeviceFilteringCondition(int i) {
		return i % random == 0;
	}

	protected ArrayList<View> generateViews() {
		ArrayList<View> list = new ArrayList<View>();
		int size = new Random(System.currentTimeMillis()).nextInt(11) + 3;
		for (int i = 0; i < size; i++) {
			TextView view = (TextView) View.inflate(getActivity(),
					R.layout.view_textview, null);
			view.setText("Device " + i);
			view.setOnClickListener(mOnClickListener);
			list.add(view);
		}
		return list;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.action_connection_view_change){
			getFragmentManager()
					.beginTransaction()
					.replace(
							R.id.activity_main_fragment_content,
							Fragment.instantiate(getActivity(),
									ListConnectionFragment.class.getName()))
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
			return true;
		}else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_circular_connection, menu);
	}

	private OnDragAndDropListener mDragAndDropListener = new OnDragAndDropListener() {

		@Override
		public void onDrop(View view) {
			Toast.makeText(getActivity(),
					view.getTag().toString() + " connected successful!",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStartDrag(View view, View center) {
			view.setBackgroundResource(R.drawable.drawable_rounded_circle_gray);
			((TextView) center).setText("Drop here to connect");
			((TextView) center)
					.setBackgroundResource(R.drawable.drawable_rounded_circle_border);
		}

		@Override
		public void onDropEnd(View view, View center) {
			center.setBackgroundResource(R.drawable.drawable_rounded_circle);
			view.setBackgroundResource(R.drawable.drawable_rounded_circle);
			((TextView) center).setText("Host");
		}
	};
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(final View v) {
			final String info = v.getTag().toString();
			final String[] titles = getResources().getStringArray(
					R.array.activity_sercive_control_menu_array);
			final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_NEGATIVE:
						Toast.makeText(getActivity(),
								info + " 의 자세한 데이터를 보여 줄 예정",
								Toast.LENGTH_SHORT).show();
						v.postDelayed(new Runnable() {

							@Override
							public void run() {
								mActivityInterface.transitFragment(1, null);
							}
						}, 500);
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						Toast.makeText(getActivity(), info + " 의 로그를 보여 줄 예정",
								Toast.LENGTH_SHORT).show();
						v.postDelayed(new Runnable() {

							@Override
							public void run() {
								mActivityInterface.transitFragment(2, null);
							}
						}, 500);

						break;
					default:
						break;
					}
				}
			};
			mDialogContentTextView.setText(info);
			mSimpleInfoDialog.setTitle(info);
			mSimpleInfoDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					titles[1], onClickListener);
			mSimpleInfoDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
					titles[2], onClickListener);
			mSimpleInfoDialog.show();
		}
	};

}
