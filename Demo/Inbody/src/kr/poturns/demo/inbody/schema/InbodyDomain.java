package kr.poturns.demo.inbody.schema;

/**
 * type 종류
 * 1. 비만형
 * 2. 평균형
 * 3. 근육형
 * 
 * @author mementohora
 *
 */
public class InbodyDomain {
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
	
	public void setFatGuy(){
		type="비만형";
		gender="남자";
		age=46;
		height=167;
		weight=100;
		muscle=13;
		fat=25;
		needweight=-20;
		needmuscle=+10;
		needfat=-9;
		usecalorie=2100;
		needcalorie=600;
	}
	public void setAvgGuy(){
		type="평균형";
		gender="남자";
		age=25;
		height=174;
		weight=73;
		muscle=19;
		fat=13;
		needweight=-5;
		needmuscle=+5;
		needfat=-3;
		usecalorie=1500;
		needcalorie=400;
	}
	public void setMuscleGuy(){
		type="근육형";
		gender="남자";
		age=25;
		height=175;
		weight=83;
		muscle=25;
		fat=8;
		needweight=+1;
		needmuscle=-1;
		needfat=0;
		usecalorie=1800;
		needcalorie=200;
	}
}
