package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnessapp.schema.InBodyData;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

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
 * 
 */
public class InBodyFragment extends SwipeEventFragment implements
		OnClickListener, IInternalEventCallback {
	public static int CODE_INBODY = 0x01;
	BlinkServiceInteraction mInteraction;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_inbody, container, false);
		v.findViewById(R.id.inbody_go_fitness).setOnClickListener(this);
		v.findViewById(R.id.inbody_update).setOnClickListener(this);
		mInteraction = mActivityInterface.getBlinkServiceInteraction();
		mInteraction.setIInternalEventCallback(this);
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
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.inbody_go_fitness:
			// 운동하기 액티비티 열기
			mActivityInterface.attachFragment(new FitnessFragment(), null);
			break;

		case R.id.inbody_update:
			// 운동하기 액티비티 열기
			if (mInteraction == null) {
				Toast.makeText(getActivity(), "서비스에 연결할 수 없습니다.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			mInteraction.remote.obtainMeasurementData(InBodyData.class,
					CODE_INBODY);
			break;

		default:
			break;
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
	public void onReceiveData(int code, CallbackData data)
			throws RemoteException {
		// TODO Auto-generated method stub
		if(code==CODE_INBODY){
			if(data.Result==false){
				Toast.makeText(getActivity(), "인바디 데이터를 받을 수 없습니다.", Toast.LENGTH_SHORT);
				return;
			}
			data
		}
	}

}
