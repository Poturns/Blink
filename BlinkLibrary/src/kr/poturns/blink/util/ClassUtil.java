package kr.poturns.blink.util;

import java.lang.reflect.Field;

/**
 * DB검색에서 Reflection을 사용하기 위한 매소드와 Schema 명을 만들기 위한 매소드들을 정의하고 있다.
 * @author Jiwon
 *
 */
public class ClassUtil {
	/**
	 * 클래스명을 리턴한다.
	 * @param obj
	 * @return
	 */
	public static String obtainClassName(Class<?> obj){
		return obj.getSimpleName();
	}
	
	/**
	 * 부모클래스가 있는지 여부를 리턴한다. 부모클래스가 Object인 경우, 즉 아무것도 상속받지 않았을 경우 false를 리턴한다.
	 * @param obj
	 * @return
	 */
	public static boolean hasSuperClass(Class<?> obj){
		Class<?> SuperClass = obj.getSuperclass();
		if(SuperClass==null || obtainClassName(SuperClass).contentEquals("Object"))return false;
		return true;
	}
	
	/**
	 * 패키지명을 리턴한다.
	 * @param obj
	 * @return
	 */
	public static String obtainPackageName(Class<?> obj){
		return obj.getPackage().getName();
	}
	
	/**
	 * 클래스의 Blink 스키마 앞부분을 리턴한다.
	 * 스키마는 '패키지:클래스/부모클래스:필드명' 이다. 
	 * @param obj
	 * @return '패키지:클래스/부모클래스'
	 */
	public static String obtainClassSchema(Class<?> obj){
		String schema = obtainPackageName(obj)+":"+obtainClassName(obj);
		Class<?> SuperClass = obj;
		while(hasSuperClass(SuperClass)){
			SuperClass = SuperClass.getSuperclass();
			schema += "."+obtainClassName(SuperClass);
		}
		return schema;
	}
	
	/**
	 * 필드를 통해 스키마를 리턴한다.
	 * 스키마는 '패키지:클래스/부모클래스:필드명' 이다. 
	 * @param field
	 * @return '패키지:클래스/부모클래스:필드명'
	 */
	public static String obtainFieldSchema(Field field){
		return obtainClassSchema(field.getDeclaringClass())+"/"+field.getName();
	}
	
	/**
	 * 부모 클래스에 정의된 필드의 스키마를 얻어온다.
	 * @param field
	 * @return '패키지:부모클래스:필드명'
	 */
	public static String obtainParentSchema(Field field){
		String schema = "";
		Class<?> SuperClass = field.getDeclaringClass();
		if(hasSuperClass(SuperClass)){
			SuperClass = SuperClass.getSuperclass();
			schema += obtainClassName(SuperClass);
		}
		while(hasSuperClass(SuperClass)){
			SuperClass = SuperClass.getSuperclass();
			schema += "."+obtainClassName(SuperClass);
		}
		schema.substring(1);
		return schema+"/"+field.getName();
	}
	
}
