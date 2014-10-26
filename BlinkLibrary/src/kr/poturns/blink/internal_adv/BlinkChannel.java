package kr.poturns.blink.internal_adv;

import java.util.concurrent.ConcurrentHashMap;

import kr.poturns.blink.internal_adv.BlinkResources.BlinkResourceException;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.10.26
 *
 */
public final class BlinkChannel implements Comparable<BlinkChannel> {

	/******************************************************************
	 	CONSTANTS
	 ******************************************************************/
	/** */
	public final static int OPENMODE_TEMPORARY = 0x0;
	public final static int OPENMODE_PUBLIC = 0x10;
	public final static int OPENMODE_STEALTH = 0x20;
	
	
	
	/******************************************************************
	 	FIELDS
	 ******************************************************************/
	/** */
	private final String ChannelID;
	private final ConcurrentHashMap<BlinkAccount, Member> MemberNavigator;
	
	private String Title;
	private int OpenMode;
	private int SecureAccessLevel;
	
	private int RolePoint;
	
	private long Timestamp;
	
	
	/******************************************************************
    	CONSTRUCTORS
	 ******************************************************************/
	/** */
	private BlinkChannel(String channelID) {
		ChannelID = channelID;
		MemberNavigator = new ConcurrentHashMap<BlinkAccount, Member>();

		OpenMode = OPENMODE_TEMPORARY;
		SecureAccessLevel = BlinkAccount.SecurePolicy.ACCESS_LEVEL_NORMAL;
		
		Timestamp = System.currentTimeMillis();
	}

	

	/******************************************************************
    	METHODS
	 ******************************************************************/
	/**
	 * 본 Channel에 해당 {@link BlinkPlayer}를 초대하도록 요청한다.
	 * Channel에서 새로운 멤버를 추가할 수 있는 권한소지자에게 요청을 보낸다.
	 *  
	 * @param player
	 */
	public void requestInvitation(BlinkPlayer player) {
		
	}
	
	/**
	 * 본 Channel에 해당 {@link BlinkPlayer}를 초대한다.
	 * Channel에서 새로운 멤버를 추가할 수 있는 권한소지자만 이 메소드를 정상적으로 수행할 수 있다.
	 * 
	 * @param player
	 */
	public void invite(BlinkPlayer player) {
		
	}
	
	
	
	/******************************************************************
    	OVERRIDES
	 ******************************************************************/
	/** */
	@Override
	public int hashCode() {
		return ChannelID.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		
		return hashCode() == o.hashCode();
	}

	@Override
	public int compareTo(BlinkChannel another) {
		return RolePoint - another.RolePoint;
	}
	
	
	
	/******************************************************************
    	GETTER & SETTER
	 ******************************************************************/
	/** */
	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public int getOpenMode() {
		return OpenMode;
	}

	public void setOpenMode(int openMode) {
		OpenMode = openMode;
	}

	public int getSecureAccessLevel() {
		return SecureAccessLevel;
	}

	public void setSecureAccessLevel(int secureAccessLevel) {
		SecureAccessLevel = secureAccessLevel;
	}
	
	

	/******************************************************************
    	INNER CLASSES
	 ******************************************************************/
	/**
	 * 
	 * @author Yeonho.Kim
	 * @since 2014.10.26
	 *
	 */
	public static class Builder {
		/*-----------------------------------------------------------------
	    	FIELDS
		 -----------------------------------------------------------------*/
		BlinkChannel mChannel;

		/*-----------------------------------------------------------------
	    	CONSTRUCTORS
		 -----------------------------------------------------------------*/
		public Builder() {
			
			mChannel = new BlinkChannel(null);
		}

		/*-----------------------------------------------------------------
	    	METHODS
		 -----------------------------------------------------------------*/
		public Builder addMember(BlinkPlayer player) {
			BlinkDevice device = player.getDevice();
			
			int rolePoint = 0;
			Member member = new Member(rolePoint, device);
			
			mChannel.RolePoint = rolePoint; 
			mChannel.MemberNavigator.put(player.getAccount(), member);
			return this;
		}
		
		public BlinkChannel build() {
			return mChannel;
		}
	}
	
	
	/**
	 * 
	 * @author Yeonho.Kim
	 * @since 2014.10.26
	 *
	 */
	private static class Member {
		/*-----------------------------------------------------------------
	    	FIELDS
		 -----------------------------------------------------------------*/
		int RolePoint;
		BlinkDevice Device;
		
		/*-----------------------------------------------------------------
	    	CONSTRUCTORS
		 -----------------------------------------------------------------*/
		Member(int rolePoint, BlinkDevice device) {
			RolePoint = rolePoint;
			Device = device;
		}
	}
	
	
	/**
	 * {@link BlinkChannel}에서 발생할 수 있는 예외 클래스.
	 * 
	 * @author Yeonho.Kim
	 * @since 2014.10.26
	 *
	 */
	public static class BlinkChannelException extends BlinkResourceException {
		/*-----------------------------------------------------------------
	    	CONSTANTS
		 -----------------------------------------------------------------*/
		private static final long serialVersionUID = -8953214138457454949L;

		/*-----------------------------------------------------------------
	    	CONSTRUCTORS
		 -----------------------------------------------------------------*/
		public BlinkChannelException(int code) {
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
