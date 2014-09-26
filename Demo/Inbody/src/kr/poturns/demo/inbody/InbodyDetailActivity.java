package kr.poturns.demo.inbody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.demo.inbody.schema.InbodyDomain;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author Ho.Kwon
 * @since 2014.09.23
 * 
 *
 */
public class InbodyDetailActivity extends ListActivity {

	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mIInternalOperationSupport;
	final String TAG = "Inbody";

	InbodyDomain mInbodyDomain = new InbodyDomain(); // InbodyList를 관리할
														// ArrayList.
	InbodyDetailAdapter adapter; // ListView 관리용 Adapter

	Button button_fat;
	Button button_avg;
	Button button_muscle; // 각 체형 별 데이터 발생 버튼
	Button button_transfer;

	ImageView bodytypeImage;
	TextView bodytypeText;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_inbody_detail_list);
		mInteraction = new BlinkServiceInteraction(this) {
			@Override
			public void onServiceConnected(
					IInternalOperationSupport iSupport) {
				InbodyDetailActivity.this.mIInternalOperationSupport = iSupport;
			}
		};
		mInteraction.startService();
		bodytypeImage = (ImageView) findViewById(R.id.bodytypeimage);
		bodytypeText = (TextView) findViewById(R.id.bodytypetext);
		/*
		 * if(mInbodyDomain.type.equals("비만형")){
		 * bodytypeImage.setImageResource(R.drawable.fatperson_white); }else
		 * if(mInbodyDomain.type.equals("평균형")){
		 * bodytypeImage.setImageResource(R.drawable.avgperson_white); }else
		 * if(mInbodyDomain.type.equals("근육형")){
		 * bodytypeImage.setImageResource(R.drawable.musclebodytype); }
		 */

		button_fat = (Button) findViewById(R.id.fatbodytypeBtn);
		button_muscle = (Button) findViewById(R.id.musclebodytypeBtn);
		button_avg = (Button) findViewById(R.id.avgbodytypeBtn);
		button_transfer = (Button) findViewById(R.id.transferButton);

		adapter = new InbodyDetailAdapter(this, mInbodyDomain); // 동적 리스트 관리
																// Adapter
		setListAdapter(adapter);
		button_fat.setOnClickListener(new Button.OnClickListener() { // fat
																		// person
																		// inbody
																		// 정보
																		// 발생.

					@Override
					public void onClick(View arg0) {
						Log.i("Inbody", "click!!");
						// TODO Auto-generated method stub
						bodytypeImage
								.setImageResource(R.drawable.fatperson_white);
						mInbodyDomain.setFatGuy();
						bodytypeText.setText("체형 : " + mInbodyDomain.type);
						adapter.notifyDataSetChanged();

					}

				});
		button_avg.setOnClickListener(new Button.OnClickListener() {// average
																	// person
																	// inbody 정보
																	// 발생.

					@Override
					public void onClick(View arg0) {
						Log.i("auction", "click!!");
						bodytypeImage
								.setImageResource(R.drawable.avgperson_white);

						mInbodyDomain.setAvgGuy();
						bodytypeText.setText("체형 : " + mInbodyDomain.type);
						adapter.notifyDataSetChanged();
					}

				});
		button_muscle.setOnClickListener(new Button.OnClickListener() {// muscle
																		// person
																		// inbody
																		// 정보
																		// 발생.

					@Override
					public void onClick(View arg0) {
						Log.i("auction", "click!!");
						// adapter.setBodyType("muscle");
						bodytypeImage
								.setImageResource(R.drawable.musclebodytype);

						mInbodyDomain.setMuscleGuy();
						bodytypeText.setText("체형 : " + mInbodyDomain.type);
						adapter.notifyDataSetChanged();
					}

				});
		button_transfer.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.i("Inbody", "전송!");
				// TODO Auto-generated method stub
				sendInbodyRemote(mInbodyDomain);
			}
			
		});
	}

	public void sendInbodyRemote(InbodyDomain inbody) {

		final int REQUEST_CODE = 0;
		final String REMOTE_APP_PACKAGE_NAME = "kr.poturns.blink.demo.healthmanager";
		Gson mGson = new GsonBuilder().setPrettyPrinting().create();

		if (mIInternalOperationSupport != null) {
			for (BlinkAppInfo info : mInteraction.local.obtainBlinkAppAll()) {
				if (info.mApp.PackageName.equals(REMOTE_APP_PACKAGE_NAME)) {
					mInteraction.remote.sendMeasurementData(info,
							mGson.toJson(inbody), REQUEST_CODE);
					Log.d(TAG, "send Inbody : " + inbody.type + " // to "
							+ REMOTE_APP_PACKAGE_NAME);
					return;
				}
			}
			Log.e(TAG, "Cannot reach remote device : "
					+ REMOTE_APP_PACKAGE_NAME);
		} else {
			Log.e(TAG, "Blink Service Support == null");
		}
	}

}