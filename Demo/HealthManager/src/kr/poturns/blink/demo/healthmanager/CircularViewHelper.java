package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import kr.poturns.blink.demo.healthmanager.R;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
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
 * List&lt;Integer&gt; list = new ArrayList&lt;Integer&gt;(size);
 * for (int i = 0; i &lt; size; i++) {
 * 	list.add(i);
 * }
 * TextView centerView = new TextView(someContext);
 * centerView.setText(&quot;center&quot;);
 * 
 * CircularViewHelper circularAdapter = new CircularViewHelper(someFrameLayout,
 * 		centerView) {
 * 	// childView의 형태를 정의
 * 	protected View getView(int position, Object object){
 * 		return View.inflate(....);
 * 	}
 * };
 * 
 * circularAdapter.drawChildViews(list);
 * 
 * </pre>
 * 
 * 
 * @see ViewGroup
 * @see FrameLayout
 * 
 * 
 */
class CircularViewHelper {
	private ViewGroup mViewGroup;
	private List<View> mChildViewList;
	private View mCenterView;
	private Context mContext;
	private int mCenterViewId;
	/** ChildView의 지름 */
	private int mViewSize;
	/** CenterView 와 ChildView와의 거리 */
	private int mChildViewDistance;
	/** ChildView 개수 */
	private int mObjectSize;
	private FrameLayout.LayoutParams mLayoutParams;
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

	/** 중앙에 배치된 View를 얻는다. */
	public final View getCenterView() {
		return mCenterView;
	}

	public final void setCenterView(View centerView) {
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
	 * 원형 View를 구성하는 Data가 변경되었을때,<br>
	 * 원형으로 배치할 View의 크기 등등의 변수(Spec)를 다시 측정한다.
	 */
	protected void measuringSpec(int newSize) {
		final int screenSize = Math.min(mContext.getResources()
				.getDisplayMetrics().widthPixels, mContext.getResources()
				.getDisplayMetrics().heightPixels);
		int actionBarSize;
		if (PrivateUtil.isScreenSizeSmall(mContext)) {
			actionBarSize = 20;
		} else {
			final TypedArray styledAttributes = mContext.getTheme()
					.obtainStyledAttributes(
							new int[] { android.R.attr.actionBarSize });
			actionBarSize = (int) styledAttributes.getDimension(0, 0);
			styledAttributes.recycle();
		}

		mViewSize = (screenSize - actionBarSize) / Math.max(newSize, 8) * 2;
		mChildViewDistance = (screenSize - mViewSize - actionBarSize * 2) / 2;
		mLayoutParams = new FrameLayout.LayoutParams(mViewSize, mViewSize);
		mLayoutParams.gravity = Gravity.CENTER;
	}

	/** 중앙의 View를 현재 Spec에 맞게 다시 그리고, 필요하다면 주어진 ViewGroup에 추가한다 */
	private void setCenterViewSpec() {
		ViewInfoTag tag = new ViewInfoTag();
		tag.mIsDrag = false;
		tag.mViewPosition = CENTER_VIEW_POSITION;
		tag.mViewId = mCenterViewId;
		mCenterView.setTag(tag);
		mCenterView.setOnDragListener(mOnDragListener);
		mCenterView.setLayoutParams(mLayoutParams);

		if (!mViewGroup.equals(mCenterView.getParent()))
			mViewGroup.addView(mCenterView);
	}

	/** ViewGroup에 ChildView를 배치한다. */
	public final void drawCircularView(Collection<? extends Object> datas) {
		removeAllChildView();
		mChildViewList.clear();
		if (datas == null) {
			mObjectSize = 0;
			measuringSpec(mObjectSize);
			setCenterViewSpec();
			return;
		}
		mObjectSize = datas.size();
		measuringSpec(mObjectSize);
		setCenterViewSpec();
		View child;
		int i = 0;
		for (Object obj : datas) {
			child = getView(mContext, i, obj);
			child.setTag(obj);
			setChildViewInfo(child, i, mObjectSize);
			mViewGroup.addView(child);
			mChildViewList.add(child);
			i++;
		}
	}

	/**
	 * 원형으로 배치할 childView를 생성한다. <br>
	 * ChildView를 임의의 형태로 inflate하려면 재정의 할 수 있다.
	 * 
	 * @param context
	 *            - Circular View를 구성하는 ViewGroup의 Context
	 * @param position
	 *            - {@link CircularViewHelper#drawCircularView(Collection)}에서
	 *            인자로 넘겨준 {@link Collection}의 {@link Iterator}를 통해 얻어낸, <br>
	 *            구성되려는 ChildView의 위치
	 * @param object
	 *            - {@link Collection}의 data
	 */
	protected View getView(Context context, int position, Object object) {
		return View.inflate(context, R.layout.view_textview, null);
	}

	public int getSize() {
		return mObjectSize;
	}

	private void setChildViewInfo(View childView, int postion, int size) {
		ViewInfoTag tag = new ViewInfoTag();
		tag.mIsDrag = false;
		tag.mViewId = postion;
		tag.mViewPosition = postion;
		tag.mTag = childView.getTag();
		childView.setTag(tag);
		childView.setLayoutParams(mLayoutParams);
		float angleDeg = postion * 360.0f / size - 90.0f;
		float angleRad = (float) (angleDeg * Math.PI / 180.0f);
		childView.setTranslationX(mChildViewDistance
				* (float) Math.cos(angleRad));
		childView.setTranslationY(mChildViewDistance
				* (float) Math.sin(angleRad));
		// tv.setRotation(angleDeg + 90.0f);
		childView.setOnTouchListener(mOnTouchListener);
		childView.setOnDragListener(mOnDragListener);
	}

	public final List<View> getChildViews() {
		return mChildViewList;
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

	private static final Handler sHandler = new Handler() {
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
	private final View.OnDragListener mOnDragListener = new View.OnDragListener() {

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
				sHandler.removeMessages(ACTION_CLICK);
				return true;
			case DragEvent.ACTION_DROP:
				if (tag.mIsDrag && !sHandler.hasMessages(ACTION_CLICK)) {
					Message m = Message.obtain();
					m.what = ACTION_CLICK;
					m.obj = v;
					sHandler.sendMessageDelayed(m, 100);
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
						sHandler.removeMessages(ACTION_CLICK);
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

	public final Object getViewTag(int position) {
		return ((ViewInfoTag) mChildViewList.get(position).getTag()).mTag;
	}

	/**
	 * CircularView에 속한 View의 Tag를 얻는다.
	 * 
	 * @param Tag
	 *            얻어오려는 View
	 * @throws ClassCastException
	 * @see View#getTag()
	 */
	public final Object getViewTag(View childView) {
		return ((ViewInfoTag) childView.getTag()).mTag;
	}

	private static class ViewInfoTag {
		public boolean mIsDrag;
		public int mViewId;
		public int mDropViewId = View.NO_ID;
		public int mViewPosition;
		public Object mTag;
	}

	private OnDragAndDropListener mDragAndDropListener;

	public final void setOnDragAndDropListener(OnDragAndDropListener l) {
		mDragAndDropListener = l;
	}

	public static interface OnDragAndDropListener {
		/**
		 * 터치한 View의 Drag가 시작되었을 때, 호출된다.
		 * 
		 * @param view
		 *            Drag를 시작한 View
		 * @param center
		 *            중앙의 View
		 */
		public void onStartDrag(View view, View center);

		/**
		 * Drag하던 View가 중앙의 View에 Drop되었을 때, 호출 됨
		 * 
		 * @param view
		 *            Drag하던 뷰
		 */
		public void onDrop(View view);

		/**
		 * 터치한 View의 Drag가 종료되었을 때, 호출된다.
		 * 
		 * @param view
		 *            Drag를 시작한 View
		 * @param center
		 *            중앙의 View
		 */
		public void onDropEnd(View view, View center);
	}
}