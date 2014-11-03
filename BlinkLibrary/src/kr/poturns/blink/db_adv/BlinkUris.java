package kr.poturns.blink.db_adv;

import android.net.Uri;

/**
 * BlinkUris - content://authority/path/id
 * @author Jiwon
 *
 */
public class BlinkUris {
	private String content;
	private String authority;
	private String path;
	
	public static final String CONTENT = "blink";
	public static final String AUTHORITY = "kr.poturns.blink";
	public static final String PATH_DATABASE = "database";
	
	
	enum Uris {
		
	}
	
	public final static Uri URI_MEASUREMENTDATA = Uri.parse("blink://kr.poturns.blink/database/measurementdata");
	
	
	class Builder {
		private String content = BlinkUris.CONTENT;
		private String authority = BlinkUris.AUTHORITY;
		private String path;
		
		public Builder(){
			
		}
		
		public Builder setPath(String path){
			this.path = path;
			return this;
		}
		
		public Uri build(){
			return Uri.parse(content+"://"+authority+path);
		}
	}
}
