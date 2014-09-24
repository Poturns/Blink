package com.example.auctionrealtimetest;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class AuctionAddActivty extends Activity implements OnClickListener {
 	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	    //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		 
		
	//	WindowManager.LayoutParams layoutParams= new WindowManager.LayoutParams();
		//  layoutParams.flags= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		//  layoutParams.dimAmount= 0.7f;
		
			// getWindow().setAttributes(layoutParams);
			//  getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_customized);
		setContentView(R.layout.activity_auction_add_activty);
		
		 
		//WebView webView = (WebView)findViewById(R.id.webPopup);
		//webView.setWebViewClient(new myWebViewClient());
		//WebSettings webSettings = webView.getSettings();
	//	webSettings.setJavaScriptEnabled(true);
	//	webSettings.setBuiltInZoomControls(true);
		//  webView.loadUrl("http://www.google.com");

	}/*
	class myWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			view.loadUrl(url);
			return true;
		}

	}
	public void onClick(View arg0) {
	
	}*/

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
} 