package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnessapp.schema.InBodyData;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import android.os.Bundle;
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
 * 5) inbody_progressbar_summary : 현재 운동량 / 하루 소모 칼로리 ( example : 50 / 100 ( 현재 운동량 / 필요 운동량 ) )<br>
 * 6) inbody_go_fitness : 운동하러가기버튼<br>
 * 7) inbody_update : inbody 데이터 업데이트 버튼<br>
 * @author Jiwon
 *
 */
public class InBodyFragment extends SwipeEventFragment implements OnClickListener{
	public static int CODE_INBODY = 0x01;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_inbody, container, false);
		v.findViewById(R.id.inbody_go_fitness).setOnClickListener(this);
		v.findViewById(R.id.inbody_update).setOnClickListener(this);
		return v;
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			mActivityInterface.returnToMain();
			break;

		default:
			break;
		}
		return false;
	}

	@Override
    public void onClick(View v) {
	    // TODO Auto-generated method stub
	    switch (v.getId()) {
		case R.id.inbody_go_fitness:
			//운동하기 액티비티 열기
			mActivityInterface.attachFragment(
					new FitnessFragment(), null);
			break;
			
		case R.id.inbody_update:
			//운동하기 액티비티 열기
			BlinkServiceInteraction mInteraction = ((MainActivity)getActivity()).getBlinkServiceInteraction();
			if(mInteraction==null){
				Toast.makeText(getActivity(), "서비스에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
				return;
			}
			mInteraction.remote.obtainMeasurementData(InBodyData.class, CODE_INBODY);
			break;

		default:
			break;
		}
    }
	
	public void setInbodyData(InBodyData inbodydata){
		
	}

}
