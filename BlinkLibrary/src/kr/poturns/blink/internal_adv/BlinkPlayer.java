package kr.poturns.blink.internal_adv;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.10.26
 *
 */
public final class BlinkPlayer implements IBlinkSynchronizable {

	/******************************************************************
	 	FIELDS
	 ******************************************************************/
	/** */
	private final BlinkDevice Device;
	private final BlinkAccount Account;
	private final ConcurrentHashMap<String, BlinkChannel> ChannelGroup;
	

	
	
	/******************************************************************
    	CONSTRUCTORS
	 ******************************************************************/
	/** */
	BlinkPlayer(BlinkDevice device, BlinkAccount account) {
		Device = device;
		Account = account;
		ChannelGroup = new ConcurrentHashMap<String, BlinkChannel>();
	}

	

	/******************************************************************
    	METHODS
	 ******************************************************************/
	/**
	 * 
	 * @return
	 */
	public Set<BlinkChannel> collectConnectedChannels() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Set<BlinkDevice> collectConnectedDevices() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/******************************************************************
    	OVERRIDES
	 ******************************************************************/
	/** */
	@Override
	public String obtainJsonSyncContents() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append((Device == null)? null : Device.toString());
		builder.append("_on_");
		builder.append((Account == null)? null : Account.toString());

		return builder.toString();
	}





	/******************************************************************
    	GETTER & SETTER
	 ******************************************************************/	
	/** */
	public BlinkDevice getDevice() {
		return Device;
	}

	public BlinkAccount getAccount() {
		return Account;
	}
}
