package kr.poturns.blink.demo.fitnessapp;

import java.io.IOException;
import java.util.List;

import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnessapp.schema.InBodyData;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.schema.Inbody;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * fragment_inbody xml<br>
 * 1) inbody_date : inbody 정보 생성일 ( example : 2014-09-23 )<br>
 * 2) inbody_age_gender : inbody 데이터의 나이,성별 정보 ( example : 25 ( 남 ) )<br>
 * 3) inbody_weight : inbody 데이터의 체중, 표준몸무게 ( example : 73 ( 표준 : 63 ) )<br>
 * 4) inbody_progressbar : 현재 운동량 / 하루 필요 운동량<br>
 * 5) inbody_progressbar_summary : 현재 운동량 / 하루 소모 칼로리 ( example : 50 / 100 ( 현재
 * 운동량 / 필요 운동량 ) )<br>
 * 6) inbody_go_fitness : 운동하러가기버튼<br>
 * 7) inbody_update : inbody 데이터 업데이트 버튼<br>
 * 
 * @author Jiwon
 * @author Myungjin
 */
public class InBodyFragment extends SwipeEventFragment implements
		OnClickListener, IInternalEventCallback {
	public static final int CODE_INBODY = 0x01;
	Gson gson;

	TextView inbody_date;
	TextView inbody_age_gender;
	TextView inbody_weight;
	ProgressBar inbody_progressbar;
	TextView inbody_progressbar_summary;
	ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_inbody, container, false);
		v.findViewById(R.id.inbody_go_fitness).setOnClickListener(this);
		v.findViewById(R.id.inbody_update).setOnClickListener(this);
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("로딩중....");
		// find view
		inbody_date = (TextView) v.findViewById(R.id.inbody_date);
		inbody_age_gender = (TextView) v.findViewById(R.id.inbody_age_gender);
		inbody_weight = (TextView) v.findViewById(R.id.inbody_weight);
		inbody_progressbar = (ProgressBar) v
				.findViewById(R.id.inbody_progressbar);
		inbody_progressbar_summary = (TextView) v
				.findViewById(R.id.inbody_progressbar_summary);

		gson = new Gson();

		try {
			InBodyData mInbodyData = FitnessUtil
					.readInBodyFromFile(getActivity());
			updateView(mInbodyData);
		} catch (Exception e) {
			e.printStackTrace();
			updateView(null);
		}

		return v;
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			mActivityInterface.returnToMain();
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.inbody_go_fitness:
			// 운동하기 액티비티 열기
			mActivityInterface.attachFragment(new FitnessFragment(), null);
			break;
		case R.id.inbody_update:
			// 인바디 데이터 얻어오기
			if (mActivityInterface.getBlinkServiceInteraction() == null) {
				Toast.makeText(getActivity(), "서비스에 연결할 수 없습니다.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			mActivityInterface.getBlinkServiceInteraction().remote
					.obtainMeasurementData(Inbody.class, CODE_INBODY);
			progressDialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null && getArguments().getBoolean("hasNotInbody")) {
			mActivityInterface.getBlinkServiceInteraction().remote
					.obtainMeasurementData(
							kr.poturns.blink.schema.Inbody.class, CODE_INBODY);
			progressDialog.show();
		}
	}

	@Override
	public IBinder asBinder() {
		return new IInternalEventCallback.Stub() {

			@Override
			public void onReceiveData(int arg0, CallbackData arg1)
					throws RemoteException {
				InBodyFragment.this.onReceiveData(arg0, arg1);
			}
		};
	}

	@Override
	public void onReceiveData(int code, final CallbackData data)
			throws RemoteException {
		switch (code) {
		case CODE_INBODY:
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					progressDialog.dismiss();
					if (data.InDeviceData == null && data.OutDeviceData == null) {
						Toast.makeText(getActivity(), "인바디 데이터를 받을 수 없습니다.",
								Toast.LENGTH_SHORT).show();
						return;
					} else {
						List<InBodyData> mInbodyList = null;
						InBodyData mInbodyData = null;
						try {
							if (data.OutDeviceData != null) {
								mInbodyList = gson.fromJson(data.OutDeviceData,
										new TypeToken<List<InBodyData>>() {
										}.getType());
							} else if (data.InDeviceData != null) {
								mInbodyList = gson.fromJson(data.InDeviceData,
										new TypeToken<List<InBodyData>>() {
										}.getType());
							}
							mInbodyData = mInbodyList.get(mInbodyList.size() - 1);

						} catch (JsonParseException e) {
							e.printStackTrace();
							Toast.makeText(getActivity(),
									"받은 데이터가 없거나 잘못되었습니다.", Toast.LENGTH_SHORT)
									.show();
							return;
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(getActivity(),
									"인바디 데이터를 받을 수 없습니다.", Toast.LENGTH_SHORT)
									.show();
							return;
						}
						try {
							FitnessUtil.saveInBodyFile(getActivity(),
									mInbodyData);
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
						updateView(mInbodyData);
					}
				}
			});
			break;
		default:
			break;
		}
	}

	private void updateView(InBodyData inbodyData) {
		if (inbodyData == null) {
			inbody_date.setText("");
			inbody_age_gender.setText("정보가 없습니다.");
			inbody_weight.setText("업데이트를 해주세요.");
			inbody_progressbar.setProgress(0);
			double todayExersisedCalorie = FitnessUtil
					.getTodayBurnedCalorie(getActivity());
			inbody_progressbar_summary.setText(Double
					.toString(todayExersisedCalorie) + " KCal 소모");
		} else {
			inbody_date.setText("");
			inbody_age_gender.setText(inbodyData.age + " 세 ("
					+ inbodyData.gender + ")");
			inbody_weight.setText("몸무게 : "
					+ Integer.toString(inbodyData.weight) + " Kg");
			inbody_progressbar.setMax(inbodyData.needcalorie);
			int todayExersisedCalorie = (int) FitnessUtil
					.getTodayBurnedCalorie(getActivity());

			// 오늘 운동한 칼로리 / 총 소모해야할 칼로리
			inbody_progressbar_summary.setText(Integer
					.toString(todayExersisedCalorie)
					+ " / "
					+ inbodyData.needcalorie);
			if (inbodyData.needcalorie < todayExersisedCalorie)
				todayExersisedCalorie = inbodyData.needcalorie;
			// 오늘 운동한 만큼 프로그레스 설정
			inbody_progressbar.setProgress(todayExersisedCalorie);
		}
	}

}
