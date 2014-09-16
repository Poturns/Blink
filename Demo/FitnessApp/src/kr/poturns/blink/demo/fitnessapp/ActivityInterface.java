package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Fragment;
import android.os.Bundle;

public interface ActivityInterface {
	public BlinkServiceInteraction getBlinkServiceInteraction();

	public IInternalOperationSupport getBlinkServiceSupport();

	/** 현재 화면을 해당 Fragment로 바꾼다. */
	public void attachFragment(Fragment fragment, Bundle argument);
}
