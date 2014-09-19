package kr.poturns.blink.demo.healthmanager;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class UserInfoDialog extends Dialog implements OnClickListener {
	Button btn_yes;
	Button btn_no;
	String[] AgeList;
	String[] GenderList;

	public UserInfoDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_info);

		btn_yes = (Button) findViewById(R.id.button_yes);
		btn_no = (Button) findViewById(R.id.button_no);
		btn_yes.setOnClickListener(this);
		btn_no.setOnClickListener(this);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText("사용자 정보");
		TextView message = (TextView) findViewById(R.id.message);
		message.setText("나이와 성별을 선택해주세요.");

		AgeList = new String[100];
		for (int i = 0; i < 100; i++) {
			AgeList[i] = String.valueOf(i);
		}
		GenderList = new String[2];
		GenderList[0] = "남자";
		GenderList[1] = "여자";

		Spinner AgeSpinner = (Spinner) findViewById(R.id.spinner_age);
		Spinner GenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
		AgeSpinner.setAdapter(new UserInfoAdapter(getContext(), R.layout.spinner_default, AgeList));
		AgeSpinner.setSelection(25);
		GenderSpinner.setAdapter(new UserInfoAdapter(getContext(), R.layout.spinner_default, GenderList));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_yes:
			dismiss();
			break;
		case R.id.button_no:
			dismiss();
			break;
		default:
			break;
		}
	}

	public class UserInfoAdapter extends ArrayAdapter<String> {
		String[] data;
		public UserInfoAdapter(Context context, int textViewResourceId,
		        String[] objects) {
			super(context, textViewResourceId, objects);
			data = objects;
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getDropDownView(int position, View convertView,
		        ViewGroup parent) {
			// TODO Auto-generated method stub
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return getCustomView(position, convertView, parent);
		}

		public View getCustomView(int position, View convertView,
		        ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.spinner_default , parent, false);
			TextView label = (TextView) row.findViewById(R.id.tv_spinner);
			label.setText(data[position]);
			return row;
		}
	}
}
