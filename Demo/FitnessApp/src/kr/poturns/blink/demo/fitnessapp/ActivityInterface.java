package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Fragment;
import android.os.Bundle;

/**
 * Activity가 제공하는 기능을 나타내는 인터페이스이다.<br>
 * <br>
 * Activity와의 결합성을 줄이기 위해 작성되었다.
 * 
 * @author Myungjin.Kim
 */
public interface ActivityInterface {
	/** {@link BlinkServiceInteraction}객체를 얻는다. */
	BlinkServiceInteraction getBlinkServiceInteraction();

	/** {@link IInternalOperationSupport}객체를 얻는다. */
	IInternalOperationSupport getBlinkServiceSupport();

	/**
	 * 현재 화면을 해당 Fragment로 바꾼다.<br>
	 * Fragment가 변경될 때, 그려질 Animation은 Right-In / Left-Out 이다.
	 * 
	 * @param fragment
	 *            변경될 UI가 존재하는 {@link Fragment}
	 * @param argument
	 *            fragment에 전달할 {@link Bundle}
	 */
	void attachFragment(Fragment fragment, Bundle argument);

	/**
	 * 현재 화면을 해당 Fragment로 바꾼다.
	 * 
	 * @param fragment
	 *            변경될 UI가 존재하는 {@link Fragment}
	 * @param argument
	 *            fragment에 전달할 {@link Bundle}
	 * @param animIn
	 *            fragment가 UI에 그려질 때, 발생할 animation resource id
	 * @param animOut
	 *            이전 fragment가 UI에서 삭제될 때, 발생할 animation resource id
	 * 
	 * @see R.animator
	 */
	void attachFragment(Fragment fragment, Bundle argument, int animIn,
			int animOut);

	/** 메인 화면으로 돌아간다. */
	void returnToMain();

	/** 심박수 측정 서비스를 시작/종료 한다. */
	void startOrStopService(boolean start);
}
