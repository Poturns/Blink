package kr.poturns.blink.external;

import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

/**
 * ViewHolder patten을 사용하여 View를 구성하는 {@link BaseExpandableListAdapter}<br>
 * <br>
 * 
 * ExpandableListView의 처리속도가 개선되는 효과가 있지만, ViewHolder를 통해 View가 재활용 되기 때문에 그에 따른
 * 문제점이 존재한다.
 * 
 * <br>
 * <br>
 * 
 * 만약 각 List Item, 상태 등등에 맞게 View의 UI를 다양하게 주고 싶다면 이 클래스를 사용하는 것을 권장하지 않는다.
 */
abstract class ViewHolderExpandableAdapter<K, V> extends
		BaseExpandableListAdapter {
	private Map<K, ? extends List<V>> mDataMap;
	private Object[] mKeyArray = new Object[1];
	private Context mContext;
	private int mGroupResId, mChildResId;
	private LayoutInflater mInflater;

	public ViewHolderExpandableAdapter(Context context, int groupResId,
			int childResId, Map<K, ? extends List<V>> map) {
		this.mDataMap = map;
		Set<K> keySet = map.keySet();
		mKeyArray = keySet.toArray(mKeyArray);
		this.mChildResId = childResId;
		this.mGroupResId = groupResId;
		this.mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getGroupCount() {
		return mDataMap.size();
	}

	protected Context getContext() {
		return mContext;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mDataMap.get(mKeyArray[groupPosition]).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mKeyArray[groupPosition];
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mDataMap.get(mKeyArray[groupPosition]).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition * 1000 + childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ViewHolder h;
		if (convertView == null) {
			convertView = mInflater.inflate(mGroupResId, parent, false);
			h = getViewHolder(convertView, true);
			convertView.setTag(h);
		} else {
			h = (ViewHolder) convertView.getTag();
		}
		createGroupView(groupPosition, isExpanded, convertView, h);
		return convertView;
	}

	/**
	 * ViewTag / ViewHolder patten을 통해 얻어진 GroupView의 내부 View를 구성한다.
	 * 
	 * @param groupPosition
	 *            View가 생성되는 group의 position
	 * @param isExpanded
	 *            View가 펼쳐지는지 / 축소되는지의 여부
	 * @param convertView
	 *            리스트를 구성할 GroupView
	 * @param h
	 *            convertView 의 ViewHolder
	 */
	protected abstract void createGroupView(int groupPosition,
			boolean isExpanded, View convertView, ViewHolder h);

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder h;
		if (convertView == null) {
			convertView = mInflater.inflate(mChildResId, parent, false);
			h = getViewHolder(convertView, false);
			convertView.setTag(h);
		} else {
			h = (ViewHolder) convertView.getTag();
		}
		createChildView(groupPosition, childPosition, isLastChild, convertView,
				h);
		return convertView;
	}

	/**
	 * ViewHolder patten을 사용하는데 필요한 Holder 객체를 얻는다.
	 * 
	 * @param v
	 *            ViewHolder의 내부 View들을 지니고 있는 parent View
	 * @param isGroup
	 *            group / child view의 여부
	 */
	protected abstract ViewHolder getViewHolder(View v, boolean isGroup);

	/**
	 * ViewTag / ViewHolder patten을 통해 얻어진 ChildView의 내부 View를 구성한다.
	 * 
	 * @param groupPosition
	 *            View가 생성되는 group의 position
	 * @param childPosition
	 *            View가 생성되는 child의 position
	 * @param isLastChild
	 *            구성되려는 View가 마지막 child인지 여부
	 * @param convertView
	 *            리스트를 구성할 ChildView
	 * @param h
	 *            convertView 의 ViewHolder
	 */
	protected abstract void createChildView(int groupPosition,
			int childPosition, boolean isLastChild, View convertView,
			ViewHolder h);

	/** ViewHolder patten을 사용하는데 필요한 Holder */
	protected static interface ViewHolder {
	};

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public void notifyDataSetChanged() {
		mKeyArray = mDataMap.keySet().toArray(mKeyArray);
		super.notifyDataSetChanged();
	}

}
