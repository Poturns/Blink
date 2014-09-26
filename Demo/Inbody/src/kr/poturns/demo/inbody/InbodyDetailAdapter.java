package kr.poturns.demo.inbody;

import kr.poturns.demo.inbody.schema.InbodyDomain;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InbodyDetailAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private InbodyDomain mInbodyDomain = null;
	private ViewHolder viewHolder = null;

	public InbodyDetailAdapter(Context c, InbodyDomain mInbodyDomain) {
		this.inflater = LayoutInflater.from(c);
		this.mInbodyDomain = mInbodyDomain;
	}

	// Adapter가 관리할 Data의 개수를 설정 합니다.
	@Override
	public int getCount() {
		return 11;
	}

	// Adapter가 관리하는 Data의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
	@Override
	public InbodyDomain getItem(int position) {
		return mInbodyDomain;
	}

	// Adapter가 관리하는 Data의 Item 의 position 값의 ID 를 얻어 옵니다.
	@Override
	public long getItemId(int position) {
		return position;
	}

	// ListView의 뿌려질 한줄의 Row를 설정 합니다.
	@Override
	public View getView(int position, View convertview, ViewGroup parent) {

		View v = inflater.inflate(R.layout.list_inbody_detail_row, null);
		View v2 = inflater.inflate(R.layout.list_inbody_detail_row2, null);
		View v3 = inflater.inflate(R.layout.list_inbody_detail_row3, null);
		// if(v == null){
		viewHolder = new ViewHolder();
		
		viewHolder.tv_domain = (TextView) v
				.findViewById(R.id.auctionItemName); 
		viewHolder.tv_data = (TextView) v
				.findViewById(R.id.auctionItemPrice); 
		viewHolder.iv_icon = (ImageView) v
				.findViewById(R.id.list_image); 
		if (position == 0) {
			viewHolder.iv_icon.setImageResource(R.drawable.personalinfo);
			viewHolder.tv_domain.setText("성별");
			viewHolder.tv_data.setText(mInbodyDomain.gender);
		} else if (position == 1) {
			viewHolder.iv_icon.setImageResource(R.drawable.personalinfo);
			viewHolder.tv_domain.setText("나이");
			viewHolder.tv_data.setText(""+mInbodyDomain.age);
		} else if (position == 2) {
			viewHolder.iv_icon.setImageResource(R.drawable.bodycm);
			viewHolder.tv_domain.setText("키");
			viewHolder.tv_data.setText(mInbodyDomain.height+"cm");
		}
		if (position == 3) {
			viewHolder.iv_icon.setImageResource(R.drawable.bodykg);
			viewHolder.tv_domain.setText("체중");
			viewHolder.tv_data.setText(mInbodyDomain.weight+"kg");
		} else if (position == 4) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("근육량");
			((TextView) v2.findViewById(R.id.auctionItemPrice)).setText(mInbodyDomain.muscle+"kg");
			((ImageView) v2.findViewById(R.id.list_image)).setImageResource(R.drawable.musclekg);
			v2.setPadding(0, 10, 0, 0);
			return v2;
		} else if (position == 5) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("지방량"); 
			((TextView) v2.findViewById(R.id.auctionItemPrice)).setText(mInbodyDomain.fat+"kg"); 
			((ImageView) v2.findViewById(R.id.list_image)).setImageResource(R.drawable.fatkg); 
			return v2;
		}
		if (position == 6) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("체중조절"); 
			((TextView) v2.findViewById(R.id.auctionItemPrice)).setText(mInbodyDomain.needweight+"kg"); // city temperature
			((ImageView) v2.findViewById(R.id.list_image)).setImageResource(R.drawable.bodycontrol); 
			return v2;
		} else if (position == 7) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("근육조절"); 
			((TextView) v2.findViewById(R.id.auctionItemPrice)).setText(mInbodyDomain.needmuscle+"kg"); // city temperature
			((ImageView) v2.findViewById(R.id.list_image)).setImageResource(R.drawable.musclecontrol);
			return v2;
		} else if (position == 8) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("지방조절");
			((TextView) v2.findViewById(R.id.auctionItemPrice))	.setText(mInbodyDomain.needfat+"kg"); // city temperature
			((ImageView) v2.findViewById(R.id.list_image)).setImageResource(R.drawable.fatcontrol); 
			return v2;
		} else if (position == 9) {
			((TextView) v3.findViewById(R.id.auctionItemName)).setText("기초대사량"); 
			((TextView) v3.findViewById(R.id.auctionItemPrice)).setText(mInbodyDomain.usecalorie+"kcal"); // city temperature
			((ImageView) v3.findViewById(R.id.list_image)).setImageResource(R.drawable.metabolismamount);
			v3.setPadding(0, 10, 0, 0);
			// thumb image
			return v3;
		} else if (position == 10) {
			((TextView) v3.findViewById(R.id.auctionItemName)).setText("필요 운동량");
			((TextView) v3.findViewById(R.id.auctionItemPrice)).setText(mInbodyDomain.needcalorie+"kcal"); // city temperature
			((ImageView) v3.findViewById(R.id.list_image)).setImageResource(R.drawable.neededexcerciseamount); // thumb image
			return v3;
		} 
		return v;

	}

	/*
	 * ViewHolder getView의 속도 향상을 위해 쓴다. 한번의 findViewByID 로 재사용 하기 위해
	 * viewHolder를 사용 한다.
	 */
	static class ViewHolder {

		TextView tv_domain;
		TextView tv_data;
		ImageView iv_icon;
	}

	@Override
	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}

	private void free() {
		inflater = null;
		mInbodyDomain = null;
		viewHolder = null;
	}

}