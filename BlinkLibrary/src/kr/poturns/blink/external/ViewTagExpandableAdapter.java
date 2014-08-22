package kr.poturns.blink.external;

import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

abstract class ViewTagExpandableAdapter<K, V> extends
		BaseExpandableListAdapter {
	private Map<K, ? extends List<V>> mDataMap;
	private Object[] mKeyArray = new Object[1];
	private Context mContext;
	private int mGroupResId, mChildResId;
	private LayoutInflater mInflater;

	public ViewTagExpandableAdapter(Context context, int groupResId,
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
		createGroupView(groupPosition, isExpanded, h);
		return convertView;
	}

	protected abstract void createGroupView(int groupPosition,
			boolean isExpanded, ViewHolder h);

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
		createChildView(groupPosition, childPosition, isLastChild, h);
		return convertView;
	}

	protected abstract ViewHolder getViewHolder(View v, boolean isGroup);

	protected abstract void createChildView(int groupPosition,
			int childPosition, boolean isLastChild, ViewHolder h);

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
