package kr.poturns.blink.external_adv;

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.10.26
 *
 */
public class InitialTuningActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	/*
	 *  >> 분기... Launch Activity에서 등록된 Account가 존재하지 않을 경우, 본 Activity를 실행.
	 *  			등록된 Account가 존재하지 않을 경우, 정상적으로 본 Orchestra 서비스를 사용할 수 없다. 
	 * 
	 *  >> SmartThings 참조... 
	 *  	각 단계는 CardFragment 사용. (Mobile에서도 가능하겠지?)
	 *  	현재 단계를 제대로 통과하지 못할 경우, 다음 단계로 넘어갈 수 없음.
	 *  	마지막 단계에서 [시작하기] 버튼을 누르기 전까지, 미리 설정 적용하지 않기.
	 *  	즉, 중간 단계에서 종료할 경우, 저장되거나 설정되는 데이터 없이 처음부터 다시 수행해야함.
	 *  
	 *  1. 사용자 등록을 할 것인가? YES, 등록절차 / No, 둘러보기 
	 *  
	 *  (YES)
	 *  2. 사용자 계정 등록 (oAuth... Google )
	 *  
	 *  3. 개인정보 등록 및 보안등급 설정 (BlinkAccount)
	 *  
	 *  4. Orchestra 전역적인 환경설정
	 *  
	 *  
	 *  
	 *  (No)
	 *  1-1. 제한적인 기능, UI 둘러보기 및 제공되는 기능 살펴보기. 
	 *  
	 */
}
