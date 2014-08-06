package kr.poturns.blink.util;

import java.lang.reflect.Field;

public class ClassUtil {
	public static String obtainClassName(Class<?> obj){
		return obj.getSimpleName();
	}
	public static boolean hasSuperClass(Class<?> obj){
		Class<?> SuperClass = obj.getSuperclass();
		if(SuperClass==null || obtainClassName(SuperClass).contentEquals("Object"))return false;
		return true;
	}
	public static String obtainPackageName(Class<?> obj){
		return obj.getPackage().getName();
	}
	public static String obtainClassSchema(Class<?> obj){
		String schema = obtainPackageName(obj)+":"+obtainClassName(obj);
		Class<?> SuperClass = obj;
		while(hasSuperClass(SuperClass)){
			SuperClass = SuperClass.getSuperclass();
			schema += "."+obtainClassName(SuperClass);
		}
		return schema;
	}
	public static String obtainFieldSchema(Field field){
		return obtainClassSchema(field.getDeclaringClass())+"/"+field.getName();
	}
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
