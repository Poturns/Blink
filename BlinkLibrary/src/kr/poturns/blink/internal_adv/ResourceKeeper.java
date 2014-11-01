package kr.poturns.blink.internal_adv;


/**
 * 
 * @author Yeonho.Kim
 * @since 2014.10.31
 *
 */
public class ResourceKeeper {
	
	/******************************************************************
 		FIELDS
	 ******************************************************************/
	
	/** 
	 * ResourceKeeper Singleton Instance 
	 */
	private static ResourceKeeper sInstance;
	
	
	
	
	/******************************************************************
		CONSTRUCTORS
	 ******************************************************************/
	/** */
	public static ResourceKeeper getInstance(BlinkEngine engine) {
		if (sInstance == null) {
			sInstance = new ResourceKeeper(engine);
		}
		
		return sInstance;
	}
	
	private ResourceKeeper(BlinkEngine engine) {
		
	}
	
	
	
}
