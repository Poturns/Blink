package kr.poturns.blink.demo.fitnessapp.schema;

import java.io.Serializable;

/** @author Myungjin.Kim */
public class InBodyData implements Serializable {
	private static final long serialVersionUID = -7103393293897543627L;
	/** 체형 */
	public String bodyShape = "";
	/** 체중 */
	public int weight;
	/** 골격근 */
	public int skeletalStriatedMuscle;
	/** 체지방률 */
	public int bodyFat;
	/** 기초대사량 */
	public int bmr;
	/** 복부지방률 */
	public int bellyFat;
	/** 혈압 */
	public int bloodPressure;
}
