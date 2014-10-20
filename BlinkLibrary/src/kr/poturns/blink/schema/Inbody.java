package kr.poturns.blink.schema;


/**
 * type 종류
 * 1. 비만형
 * 2. 평균형
 * 3. 근육형
 * 
 * @author mementohora
 *
 */
public class Inbody extends DefaultSchema.Base{
	public String type;
	public String gender;
	public int age;
	public double height;
	public int weight;
	public int muscle;
	public int fat;
	public int needweight;
	public int needmuscle;
	public int needfat;
	public int usecalorie;
	public int needcalorie;
	
}
