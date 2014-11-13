package kr.poturns.blink.demo.healthmanager;

import java.util.Comparator;

import kr.poturns.blink.schema.Inbody;

public class DateCompare implements Comparator<Inbody> {
	public int compare(Inbody arg0, Inbody arg1) {
		// TODO Auto-generated method stub
		return arg1.DateTime.compareTo(arg0.DateTime);
	}

}