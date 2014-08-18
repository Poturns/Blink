package com.example.servicetestapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class TestActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Toast.makeText(this, "테스트 액티비티 실행", Toast.LENGTH_SHORT).show();
	}
}
