package kr.poturns.blink.external.tab.connectionview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 기존 존재하는 ViewGroup에 원의 형태로 ChildView를 추가해주고, <br>
 * 핵심 View와 기타 ChildView 와의 Drag and Drop event를 받기 쉽게 만들어주는 도우미 Class <br>
 * <br>
 * * 현재 지원하는 {@link ViewGroup}은 {@link FrameLayout}에 한정된다. <br>
 * <br>
 * <b><사용 예시></b><br>
 * 
 * <pre class="prettyprint">
 * FrameLayout someFrameLayout = (FrameLayout) someParentView
 * 		.findViewById(R.id.someId);
 * 
 * int size = 11;
 * List&lt;View&gt; viewList = new ArrayList(size);
 * for (int i = 0; i &lt; size; i++) {
 * 	TextView tv = new TextView(someContext);
 * 	tv.setText(&quot;View &quot; + i);
 * 	viewList.add(tv);
 * }
 * TextView centerView = new TextView(someContext);
 * centerView.setText(&quot;center&quot;);
 * 
 * CircularViewHelper circularAdapter = new CircularViewHelper(someFrameLayout,
 * 		centerView);
 * circularAdapter.addChildViews(viewList);
 * 
 * circularAdapter.drawChildViews();
 * 
 * </pre>
 * 
 * 
 * @see ViewGroup
 * @see FrameLayout
 * 
 * 
 */
public class CircularViewHelper {
	private ViewGroup mViewGroup;
	private List<View> mChildViewList;
	private View mCenterView;
	private Context mContext;
	private int mCenterViewId;
	private static final int CENTER_VIEW_POSITION = -2;

	/**
	 * CircularViewHelper instance를 생성한다.
	 * 
	 * @param viewGroup
	 *            원형 View가 추가될 ViewGroup
	 */
	public CircularViewHelper(ViewGroup viewGroup) {
		if (!checkVaildViewGroup(viewGroup))
			throw new RuntimeException(
					"CircularViewHelper only accept FrameLayout");
		this.mViewGroup = viewGroup;
		mContext = mViewGroup.getContext();
		mCenterView = (View) viewGroup.findViewById(android.R.id.text1);
		mChildViewList = new ArrayList<View>();
	}

	/**
	 * CircularViewHelper instance를 생성한다.
	 * 
	 * @param viewGroup
	 *            원형 View가 추가될 ViewGroup
	 * @param centerViewId
	 *            원 가운데 배치된 View의 id
	 */
	public CircularViewHelper(ViewGroup viewGroup, int centerViewId) {
		this(viewGroup);
		mCenterView = (View) viewGroup.findViewById(centerViewId);
		this.mCenterViewId = centerViewId;
	}

	/**
	 * CircularViewHelper instance를 생성한다.
	 * 
	 * @param viewGroup
	 *            원형 View가 추가될 ViewGroup
	 * @param centerView
	 *            원 가운데 배치된 View
	 */
	public CircularViewHelper(ViewGroup viewGroup, View centerView) {
		this(viewGroup);
		mCenterView = centerView;
		setCenterViewIdIfNotExist();
	}

	private boolean checkVaildViewGroup(ViewGroup viewGroup) {
		return viewGroup instanceof FrameLayout;
	}

	public View getCenterView() {
		return mCenterView;
	}

	public void setCenterView(View centerView) {
		if (mCenterView != null)
			mViewGroup.removeView(mCenterView);
		this.mCenterView = centerView;
		setCenterViewIdIfNotExist();
	}

	private void setCenterViewIdIfNotExist() {
		mCenterViewId = mCenterView.getId();
		if (mCenterViewId == View.NO_ID) {
			mCenterViewId = View.generateViewId();
			mCenterView.setId(mCenterViewId);
		}
	}

	private void removeAllChildView() {
		if (mChildViewList != null) {
			int size = mChildViewList.size();
			for (int i = 0; i < size; i++) {
				mViewGroup.removeView(mChildViewList.get(i));
			}
		}
	}

	/**
	 * 원형으로 배치될 View를 등록한다.
	 * 
	 * @param childViews
	 *            원형으로 배치될 View들
	 */
	public void addChildViews(Collection<? extends View> childViews) {
		removeAllChildView();
		mChildViewList.clear();
		mChildViewList.addAll(childViews);
	}

	public List<View> getChildViews() {
		return mChildViewList;
	}

	/** ViewGroup에 ChildView를 배치한다. */
	public void drawCircularView() {
		final int size = mChildViewList.size();
		final int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		final int viewSize = screenWidth / (size < 8 ? 8 : size) * 2;
		final int distance = (screenWidth - viewSize - 20) / 2;
		ViewInfoTag tag = new ViewInfoTag();
		tag.mIsDrag = false;
		tag.mViewPosition = CENTER_VIEW_POSITION;
		tag.mViewId = mCenterViewId;
		mCenterView.setTag(tag);
		mCenterView.setOnDragListener(mOnDragListener);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(viewSize,
				viewSize);
		lp.gravity = Gravity.CENTER;
		mCenterView.setLayoutParams(lp);

		if (!mViewGroup.equals(mCenterView.getParent()))
			mViewGroup.addView(mCenterView);

		for (int i = 0; i < size; i++) {
			View child = mChildViewList.get(i);
			tag = new ViewInfoTag();
			tag.mIsDrag = false;
			tag.mViewId = i;
			tag.mViewPosition = i;
			tag.mTag = child.getTag();
			child.setTag(tag);
			child.setLayoutParams(lp);
			float angleDeg = i * 360.0f / size - 90.0f;
			float angleRad = (float) (angleDeg * Math.PI / 180.0f);
			child.setTranslationX(distance * (float) Math.cos(angleRad));
			child.setTranslationY(distance * (float) Math.sin(angleRad));
			// tv.setRotation(angleDeg + 90.0f);
			child.setOnTouchListener(mOnTouchListener);
			child.setOnDragListener(mOnDragListener);
			mViewGroup.addView(child);
		}
	}

	private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(final View v, MotionEvent event) {
			if (v.getAlpha() == 1.0f) {
				ViewInfoTag tag = (ViewInfoTag) v.getTag();
				tag.mIsDrag = true;
				ClipData data = ClipData.newPlainText("", tag.toString());
				v.startDrag(data, new View.DragShadowBuilder(v), v, 0);
			}
			return true;
		}
	};

	private static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ACTION_CLICK:
				View v = (View) msg.obj;
				v.performClick();
				break;
			default:
				break;
			}
		}
	};
	private static final int ACTION_CLICK = 888;
	private View.OnDragListener mOnDragListener = new View.OnDragListener() {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			final int action = event.getAction();
			ViewInfoTag tag = (ViewInfoTag) v.getTag();

			switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:
				return true;
			case DragEvent.ACTION_DRAG_ENTERED:
				if (tag.mIsDrag && mDragAndDropListener != null)
					mDragAndDropListener.onStartDrag(v, mCenterView);
				return true;
			case DragEvent.ACTION_DRAG_LOCATION:
				return true;
			case DragEvent.ACTION_DRAG_EXITED:
				mHandler.removeMessages(ACTION_CLICK);
				return true;
			case DragEvent.ACTION_DROP:
				if (tag.mIsDrag && !mHandler.hasMessages(ACTION_CLICK)) {
					Message m = Message.obtain();
					m.what = ACTION_CLICK;
					m.obj = v;
					mHandler.sendMessageDelayed(m, 100);
					return false;
				}
				if (tag.mViewPosition == CENTER_VIEW_POSITION) {
					ViewInfoTag draggerTag = (ViewInfoTag) ((View) event
							.getLocalState()).getTag();
					draggerTag.mDropViewId = tag.mViewId;
					return true;
				} else
					return false;
			case DragEvent.ACTION_DRAG_ENDED:
				if (tag.mIsDrag) {
					if (event.getResult() && tag.mDropViewId == mCenterViewId
							&& mDragAndDropListener != null) {
						mHandler.removeMessages(ACTION_CLICK);
						mDragAndDropListener.onDrop(v);
					}
					tag.mDropViewId = View.NO_ID;
					tag.mIsDrag = false;
					if (mDragAndDropListener != null)
						mDragAndDropListener.onDropEnd(v, mCenterView);
				}
				return true;
			default:
				return false;
			}
		}
	};

	public static class ViewInfoTag {
		public boolean mIsDrag;
		public int mViewId;
		public int mDropViewId = View.NO_ID;
		public int mViewPosition;
		public Object mTag;
	}

	private OnDragAndDropListener mDragAndDropListener;

	public void setOnDragAndDropListener(OnDragAndDropListener l) {
		mDragAndDropListener = l;
	}

	public static interface OnDragAndDropListener {
		public void onStartDrag(View view, View center);

		/**
		 * Drag하던 View가 중앙의 View에 Drop되었을 때, 호출 됨
		 * 
		 * @param view
		 *            Drag하던 뷰
		 */
		public void onDrop(View view);

		public void onDropEnd(View view, View center);
	}
}
