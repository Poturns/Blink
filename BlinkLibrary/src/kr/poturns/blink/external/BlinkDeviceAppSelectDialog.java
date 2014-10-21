package kr.poturns.blink.external;

import java.util.List;
import java.util.Map;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.IDatabaseObject;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

/** 현재 Device에 존재하는 Device와 그 Device에 속한 App의 목록을 보여주는 Dialog */
public class BlinkDeviceAppSelectDialog {
	private Dialog mDialog;
	private OnItemSelectedListener l = null;
	private Map<Device, List<App>> mDeviceMap;

	public BlinkDeviceAppSelectDialog(Context context) {
		mDeviceMap = new SqliteManagerExtended(context).obtainDeviceMap();
		View contentView = View.inflate(context,
				R.layout.res_blink_view_device_app, null);
		contentView.setPadding(20, 20, 20, 20);
		ExpandableListView listView = (ExpandableListView) contentView
				.findViewById(android.R.id.list);
		listView.setEmptyView(contentView.findViewById(android.R.id.empty));
		listView.setAdapter(new ContentAdapter(context, mDeviceMap));

		mDialog = new AlertDialog.Builder(context).setCancelable(true)
				.setView(contentView).create();
	}

	public final Dialog getDialog() {
		return mDialog;
	}

	public final void show() {
		mDialog.show();
	}

	public final void dismiss() {
		mDialog.dismiss();
	}

	public final void setOnItemSelectedListener(OnItemSelectedListener l) {
		this.l = l;
	}

	/** {@link Device} 또는 {@link App}이 선택되면 호출될 리스너 */
	public static interface OnItemSelectedListener {
		/**
		 * {@code BlinkDeviceAppSelectDialog}에서 {@link Device} 또는 {@link App}이
		 * 선택되면 호출된다.
		 * 
		 * @param object
		 *            선택된 {@link Device} 또는 {@link App} 객체
		 */
		public void onItemSelected(IDatabaseObject object);
	}

	public class ContentAdapter extends
			ViewHolderExpandableAdapter<Device, App> {

		public ContentAdapter(Context context,
				Map<Device, ? extends List<App>> map) {
			super(context, R.layout.res_blink_view_select_device_app_parent,
					R.layout.res_blink_view_select_device_app_child, map);
		}

		@Override
		protected void createGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewHolder h) {
			final Device device = (Device) getGroup(groupPosition);
			Holder holder = (Holder) h;
			holder.tv.setText(device.Device);
			holder.tv.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.res_blink_ic_action_hardware_phone, 0, 0, 0);
			holder.btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					if (l != null)
						l.onItemSelected(device);
				}
			});
		}

		@Override
		protected ViewHolder getViewHolder(View v, boolean isGroup) {
			return new Holder(v);
		}

		@Override
		protected void createChildView(final int groupPosition,
				int childPosition, boolean isLastChild, View convertView,
				ViewHolder h) {
			Holder holder = (Holder) h;
			final App item = (App) getChild(groupPosition, childPosition);
			holder.tv.setText(item.AppName);
			holder.tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
					PrivateUtil
							.obtainAppIcon(item, getContext().getResources()),
					null, null, null);
			holder.btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					if (l != null)
						l.onItemSelected(item);
				}
			});

		}

		private class Holder implements ViewHolder {
			TextView tv;
			Button btn;

			public Holder(View v) {
				tv = (TextView) v.findViewById(android.R.id.text1);
				btn = (Button) v.findViewById(android.R.id.button1);
			}
		}
	}
}
