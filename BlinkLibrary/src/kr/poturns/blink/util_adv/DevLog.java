package kr.poturns.blink.util_adv;

import java.lang.reflect.Field;

import android.util.Log;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public class DevLog {
	/**
	 * 
	 */
	public static boolean ForcePrint = false;
	
	/**
	 * 
	 */
	protected static boolean DefaultPrint = true;
	
	/**
	 * 
	 * @param from
	 * @param object
	 */
	public static void debug(Object from, Object object) {
		debug(from, object, null);
	}
	
	public static void debug(Object from, Object object, String keyword) {
		if (!(DefaultPrint || ForcePrint))
			return;
		
		String result = (keyword == null)? "" : "["+keyword+"]";
		if (object instanceof String)
			result += (String) object;
		
		else {
			StringBuilder builder = new StringBuilder();
			builder.append(object.getClass().getCanonicalName() + "\n");
			
			for(Field field : object.getClass().getDeclaredFields()) {
				builder.append(field.getName());
				builder.append(" : ");
				
				try {
					Object value = field.get(field.getName());
					builder.append(value.toString());
				
				} catch (Exception e) {
					builder.append("...");
				}
	
				builder.append("\n");
			}
			builder.append("\n");
			
			result += builder.toString();
		}
		
		String fromString = from.toString();
		int index = fromString.indexOf('@');

		Log.d(	fromString.substring(0, 
				(index == -1)? fromString.length(): index), 
				result);
	}
	
	/**
	 * 
	 * @param from
	 * @param object
	 */
	public static void error(Object from, Object object) {
		debug(from, object, null);
	}
	
	public static void error(Object from, Object object, String keyword) {
		if (!(DefaultPrint || ForcePrint))
			return;
		
		String result = (keyword == null)? "" : "["+keyword+"]";
		if (object instanceof String)
			result += (String) object;
		
		else {
			StringBuilder builder = new StringBuilder();
			builder.append(object.getClass().getCanonicalName() + "\n");
			
			for(Field field : object.getClass().getDeclaredFields()) {
				builder.append(field.getName());
				builder.append(" : ");
				
				try {
					Object value = field.get(field.getName());
					builder.append(value.toString());
				
				} catch (Exception e) {
					builder.append("...");
				}
	
				builder.append("\n");
			}
			builder.append("\n");
			
			result += builder.toString();
		}
		
		String fromString = from.toString();
		int index = fromString.indexOf('@');

		Log.e(	fromString.substring(0, 
				(index == -1)? fromString.length(): index), 
				result);
	}
}
