package kr.poturns.blink.internal_adv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.poturns.blink.internal_adv.BlinkResources.BlinkResourceException;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.10.26
 *
 */
public final class BlinkAccount {

	/******************************************************************
	 	FIELDS
	 ******************************************************************/
	/** */
	private String Authority;
	private String AuthKey;
	
	private int SecureAccessLevel;
	private HashMap<SecurePolicy, Object> PlayerInfo;
	
	private int BlinkFeaturePoint;
	
	
	/******************************************************************
    	CONSTRUCTORS
	 ******************************************************************/
	/** */
	BlinkAccount() {
		SecureAccessLevel = SecurePolicy.ACCESS_LEVEL_NORMAL;
		PlayerInfo = new HashMap<SecurePolicy, Object>();
		
	}
	
	

	/******************************************************************
    	GETTER & SETTER
	 ******************************************************************/
	/** */
	public int getSecureAccessLevel() {
		return SecureAccessLevel;
	}

	public void setSecureAccessLevel(int secureAccessLevel) {
		SecureAccessLevel = secureAccessLevel;
	}

	public String getAuthority() {
		return Authority;
	}
	
	
	
	/******************************************************************
    	INNER CLASSES
	 ******************************************************************/
	/**
	 * 
	 * @author YeonhoKim
	 * @since 2014.10.26
	 *
	 */
	public enum SecurePolicy {
		/*-----------------------------------------------------------------
	    	ENTRIES
		 -----------------------------------------------------------------*/
		
		//▲ ACCESS_LEVEL_EMPTY 		▽ ACCESS_LEVEL_LOW
		NICKNAME(0x100),
		
		//▲ ACCESS_LEVEL_LOW 		▽  ACCESS_LEVEL_NORMAL
		CHANNEL(0x1000),
		APPLICATION(0x2000),
		
		//▲ ACCESS_LEVEL_NORMAL		▽  ACCESS_LEVEL_HIGH
		EMAIL(0x4100),
		
		//▲ ACCESS_LEVEL_HIGH 		▽  ACCESS_LEVEL_CLASSIFIED
		NAME(0x8100),
		
		//▲ ACCESS_LEVEL_CLASSIFIED	▽  ACCESS_LEVEL_SECRET
		AUTHENTICATION(0xFFA678)
		;

		/*-----------------------------------------------------------------
	    	CONSTANTS
		 -----------------------------------------------------------------*/
		/** 특수 접근인가자만 열람가능한 보안 등급. (비공유정보) */
		public static final int ACCESS_LEVEL_SECRET = 0xF00000;
		
		/** 수집된 모든 개인정보에 접근할 수 있는 보안등급. */
		public static final int ACCESS_LEVEL_CLASSIFIED = 0xA00000;
		/** 서비스에서 운용되는 모든 정보와 일부 개인정보를 확인할 수 있는 보안등급. */
		public static final int ACCESS_LEVEL_HIGH = 0x80000;
		/** 서비스의 전반적인 정보를 확인할 수 있는 보안등급. */
		public static final int ACCESS_LEVEL_NORMAL = 0x4000;
		/** 서비스의 기본적인 정보를 확인할 수 있는 보안 등급. */
		public static final int ACCESS_LEVEL_LOW = 0x200;
		/** 서비스 운용에 필수적인 정보만 확인할 수 있는 보안 등급. */
		public static final int ACCESS_LEVEL_EMPTY = 0x10;

		/*-----------------------------------------------------------------
	    	FIELDS
		 -----------------------------------------------------------------*/
		int AccessLevel;

		/*-----------------------------------------------------------------
	    	CONSTRUCTORS
		 -----------------------------------------------------------------*/
		private SecurePolicy(int access) {
			AccessLevel = access;
		}

		/*-----------------------------------------------------------------
	    	METHODS
		 -----------------------------------------------------------------*/
		public static List<SecurePolicy> availables(int level) {
			ArrayList<SecurePolicy> policyList = new ArrayList<SecurePolicy>();
			for (SecurePolicy policy : SecurePolicy.values()) { 
				if (policy.AccessLevel <= level) 
					policyList.add(policy);
				else
					break;
			}
			
			return policyList;
		}
	}
	
	/**
	 * {@link BlinkAccount}에서 발생할 수 있는 보안정책 예외 클래스.
	 * 
	 * @author Yeonho.Kim
	 * @since 2014.10.26
	 *
	 */
	public static class SecurePolicyException extends BlinkResourceException {
		/*-----------------------------------------------------------------
	    	CONSTANTS
		 -----------------------------------------------------------------*/
		private static final long serialVersionUID = -713011994686119843L;


		/*-----------------------------------------------------------------
	    	CONSTRUCTORS
		 -----------------------------------------------------------------*/
		public SecurePolicyException(int code) {
			super(code);
		}
		

		/*-----------------------------------------------------------------
	    	OVERRIDES
		 -----------------------------------------------------------------*/
		@Override
		public String getMessage() {
			String errMsg = null;
			
			switch (ExceptionCode) {
			}
			
			return errMsg;
		}
	}
}
