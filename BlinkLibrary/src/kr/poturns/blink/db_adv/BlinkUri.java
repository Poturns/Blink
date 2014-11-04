package kr.poturns.blink.db_adv;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

/**
 * BlinkUris - content://authority/path/id
 * @author Jiwon
 *
 */
public class BlinkUri {
	private String content = CONTENT;
	private String authority = AUTHORITY;
	private ArrayList<String> path = new ArrayList<String>();
	
	public static final String CONTENT = "blink";
	public static final String AUTHORITY = "kr.poturns.blink";
	
	public static final String PATH_DATABASE = "database";
	public static final String PATH_MEASUREMENTDATA = "measurementdata";
	public static final String PATH_BLINKAPPINFO = "blinkappinfo";
	
	public String getContent(){
		return content;
	}
	
	public String getAuthority(){
		return authority;
	}
	
	public BlinkUri addPath(String uri){
		path.add(uri);
		return this;
	}
	
	public List<String> getPath(){
		return path;
	}
	
	public String toString(){
		return getUri().toString();
	}
	
	public Uri getUri(){
		String uri = content+"://"+authority;
		for(int i=0;i<path.size();i++){
			uri += "/"+path.get(i);
		}
		return Uri.parse(uri);
	}
	
	public static boolean isBlinkUri(BlinkUri uri){
		return false;
	}
	
	public static final String URI_MEASUREMENTDATA = new BlinkUri().addPath(PATH_DATABASE).addPath(PATH_MEASUREMENTDATA).toString();
	
}
