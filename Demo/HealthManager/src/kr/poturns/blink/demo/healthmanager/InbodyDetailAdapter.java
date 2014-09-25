package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InbodyDetailAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private ArrayList<InbodyListDomain> InbodyDomainList = null;
	private ViewHolder viewHolder = null;
	private Context mContext = null;
	private String bodyType = null; // 체형 정보

	public InbodyDetailAdapter(Context c, ArrayList<InbodyListDomain> arrays) {
		this.mContext = c;
		this.inflater = LayoutInflater.from(c);
		this.InbodyDomainList = arrays;
	}

	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}

	// Adapter가 관리할 Data의 개수를 설정 합니다.
	@Override
	public int getCount() {
		return InbodyDomainList.size();
	}

	// Adapter가 관리하는 Data의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
	@Override
	public InbodyListDomain getItem(int position) {
		return InbodyDomainList.get(position);
	}

	// Adapter가 관리하는 Data의 Item 의 position 값의 ID 를 얻어 옵니다.
	@Override
	public long getItemId(int position) {
		return position;
	}

	// ListView의 뿌려질 한줄의 Row를 설정 합니다.
	@Override
	public View getView(int position, View convertview, ViewGroup parent) {

		View v = convertview;
		View v2 = inflater.inflate(R.layout.list_inbody_detail_row2, null);
		View v3 = inflater.inflate(R.layout.list_inbody_detail_row3, null);
		// if(v == null){
		viewHolder = new ViewHolder();
		v = inflater.inflate(R.layout.list_inbody_detail_row, null);

		viewHolder.auctionItemName = (TextView) v
				.findViewById(R.id.auctionItemName); // city weather overview
		viewHolder.auctionItemPrice = (TextView) v
				.findViewById(R.id.auctionItemPrice); // city temperature
		viewHolder.auctionItemImage = (ImageView) v
				.findViewById(R.id.list_image); // thumb image
		// viewHolder.auctionItemEndFlag =
		// (TextView)v.findViewById(R.id.auctionEndFlag);
		/*
		 * v.setTag(viewHolder);
		 * 
		 * }else { viewHolder = (ViewHolder)v.getTag(); }
		 */
		if (position == 0) {
			viewHolder.auctionItemImage
					.setImageResource(R.drawable.personalinfo);
			viewHolder.auctionItemName.setText("이름");
			if (bodyType.equals("fat")) {
				viewHolder.auctionItemPrice.setText("김지원");
			} else if (bodyType.equals("muscle")) {
				viewHolder.auctionItemPrice.setText("김명진");

			} else if (bodyType.equals("avg")) {
				viewHolder.auctionItemPrice.setText("김연호");
			}
		} else if (position == 1) {
			viewHolder.auctionItemImage
					.setImageResource(R.drawable.personalinfo);
			viewHolder.auctionItemName.setText("나이");
			if (bodyType.equals("fat")) {
				viewHolder.auctionItemPrice.setText("46세");
			} else if (bodyType.equals("muscle")) {
				viewHolder.auctionItemPrice.setText("25세");

			} else if (bodyType.equals("avg")) {
				viewHolder.auctionItemPrice.setText("32세");
			}
		} else if (position == 2) {
			viewHolder.auctionItemImage.setImageResource(R.drawable.bodycm);
			viewHolder.auctionItemName.setText("키");
			if (bodyType.equals("fat")) {
				viewHolder.auctionItemPrice.setText("167cm");
			} else if (bodyType.equals("muscle")) {
				viewHolder.auctionItemPrice.setText("175cm");

			} else if (bodyType.equals("avg")) {
				viewHolder.auctionItemPrice.setText("177cm");
			}
		}
		if (position == 3) {
			viewHolder.auctionItemImage.setImageResource(R.drawable.bodykg);
			viewHolder.auctionItemName.setText("체중");
			if (bodyType.equals("fat")) {
				viewHolder.auctionItemPrice.setText("100kg");
			} else if (bodyType.equals("muscle")) {
				viewHolder.auctionItemPrice.setText("83kg");

			} else if (bodyType.equals("avg")) {
				viewHolder.auctionItemPrice.setText("75kg");
			}
		} else if (position == 4) {
		//	viewHolder.auctionItemImage.setImageResource(R.drawable.musclekg);
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("근육량"); // city
			// weather
			// overview
			if (bodyType.equals("fat")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("13kg"); // city temperature
			} else if (bodyType.equals("muscle")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("25kg"); // city temperature

			} else if (bodyType.equals("avg")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("19kg"); // city temperature
			}

			((ImageView) v2.findViewById(R.id.list_image))
					.setImageResource(R.drawable.musclekg); // thumb image
			v2.setPadding(0, 10, 0, 0);
			return v2;
		} else if (position == 5) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("지방량"); // city
			// weather
			// overview
			if (bodyType.equals("fat")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("25kg"); // city temperature
			} else if (bodyType.equals("muscle")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("8kg"); // city temperature

			} else if (bodyType.equals("avg")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("13kg"); // city temperature
			}

			((ImageView) v2.findViewById(R.id.list_image))
					.setImageResource(R.drawable.fatkg); // thumb image
			return v2;
		}
		if (position == 6) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("체중조절"); // city
																				// weather
																				// overview
			if (bodyType.equals("fat")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("-20kg"); // city temperature
			} else if (bodyType.equals("muscle")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("+1kg"); // city temperature

			} else if (bodyType.equals("avg")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("-5kg"); // city temperature
			}

			((ImageView) v2.findViewById(R.id.list_image))
					.setImageResource(R.drawable.bodycontrol); // thumb image
			//v2.setPadding(0, 10, 0, 0);
			// viewHolder.auctionItemImage
			// viewHolder.auctionItemPrice
			// viewHolder.auctionItemName.setText("체중조절");
			return v2;
		} else if (position == 7) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("근육조절"); // city
																				// weather
			// overview
			if (bodyType.equals("fat")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("+10kg"); // city temperature
			} else if (bodyType.equals("muscle")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("-1.3kg"); // city temperature

			} else if (bodyType.equals("avg")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("+5kg"); // city temperature
			} // overview
			((ImageView) v2.findViewById(R.id.list_image))
					.setImageResource(R.drawable.musclecontrol);
			// thumb image
			/*
			 * viewHolder.auctionItemImage.setImageResource(R.drawable.musclecontrol
			 * ); viewHolder.auctionItemPrice.setText("+12kg");
			 * viewHolder.auctionItemName.setText("근육조절");
			 */
			return v2;
		} else if (position == 8) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("지방조절"); // city
																				// weather
																				// overview
			if (bodyType.equals("fat")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("-9kg"); // city temperature
			} else if (bodyType.equals("muscle")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("0kg"); // city temperature

			} else if (bodyType.equals("avg")) {
				((TextView) v2.findViewById(R.id.auctionItemPrice))
						.setText("-3kg"); // city temperature
			}// city
				// temperature
			((ImageView) v2.findViewById(R.id.list_image))
					.setImageResource(R.drawable.fatcontrol); // thumb image
			/*
			 * viewHolder.auctionItemImage.setImageResource(R.drawable.fatcontrol
			 * ); viewHolder.auctionItemPrice.setText("-7kg");
			 * viewHolder.auctionItemName.setText("지방조절");
			 */
			return v2;
		} else if (position == 9) {
			((TextView) v3.findViewById(R.id.auctionItemName)).setText("기초대사량"); // city
																					// weather
																					// overview
			if (bodyType.equals("fat")) {
				((TextView) v3.findViewById(R.id.auctionItemPrice))
						.setText("2100kcal"); // city temperature
			} else if (bodyType.equals("muscle")) {
				((TextView) v3.findViewById(R.id.auctionItemPrice))
						.setText("1800kcal"); // city temperature

			} else if (bodyType.equals("avg")) {
				((TextView) v3.findViewById(R.id.auctionItemPrice))
						.setText("1500kcal"); // city temperature
			}
			((ImageView) v3.findViewById(R.id.list_image))
					.setImageResource(R.drawable.metabolismamount);
			v3.setPadding(0, 10, 0, 0);
			// thumb image
			return v3;
		} else if (position == 10) {
			((TextView) v3.findViewById(R.id.auctionItemName))
					.setText("필요 운동량"); // city weather overview
			if (bodyType.equals("fat")) {
				((TextView) v3.findViewById(R.id.auctionItemPrice))
						.setText("600kcal"); // city temperature
			} else if (bodyType.equals("muscle")) {
				((TextView) v3.findViewById(R.id.auctionItemPrice))
						.setText("200kcal"); // city temperature

			} else if (bodyType.equals("avg")) {
				((TextView) v3.findViewById(R.id.auctionItemPrice))
						.setText("400kcal"); // city temperature
			}
			((ImageView) v3.findViewById(R.id.list_image))
					.setImageResource(R.drawable.neededexcerciseamount); // thumb image
			return v3;
		} else if (position == 11) {
			((TextView) v3.findViewById(R.id.auctionItemName))
					.setText("sample"); // city
										// weather
										// overview
			((TextView) v3.findViewById(R.id.auctionItemPrice))
					.setText("+12kg"); // city temperature
			((ImageView) v3.findViewById(R.id.list_image))
					.setImageResource(R.drawable.musclecontrol); // thumb image
			return v3;
		}
		// viewHolder.auctionItemImage.setImageResource(R.drawable.bodymusclepercent);
		// ImageView imageView = new ImageView(this.mContext);
		// imageView.setImageS(R.drawable.fatcontrol);

		// viewHolder.auctionItemEndFlag.setText("test1");
		// viewHolder.auctionItemName.setTag(position);
		// viewHolder.auctionItemPrice.setText("test1");
		// viewHolder.auctionItemEndFlag.setText("test");
		/*
		 * viewHolder.tv_title.setText(getItem(position).title);
		 * 
		 * // image 나 button 등에 Tag를 사용해서 position 을 부여해 준다. // Tag란 View를 식별할 수
		 * 있게 바코드 처럼 Tag를 달아 주는 View의 기능 // 이라고 생각 하시면 됩니다.
		 * viewHolder.iv_image.setTag(position);
		 * viewHolder.iv_image.setOnClickListener(buttonClickListener);
		 * 
		 * viewHolder.btn_button.setTag(position);
		 * viewHolder.btn_button.setText(getItem(position).button);
		 * viewHolder.btn_button.setOnClickListener(buttonClickListener);
		 * 
		 * viewHolder.cb_box.setTag(position);
		 * viewHolder.cb_box.setOnClickListener(buttonClickListener);
		 */

		return v;

	}

	// Adapter가 관리하는 Data List를 교체 한다.
	// 교체 후 Adapter.notifyDataSetChanged() 메서드로 변경 사실을
	// Adapter에 알려 주어 ListView에 적용 되도록 한다.
	public void setArrayList(ArrayList<InbodyListDomain> arrays) {
		this.InbodyDomainList = arrays;
	}

	public ArrayList<InbodyListDomain> getArrayList() {
		return InbodyDomainList;
	}

	public void addArrayList(InbodyListDomain arrays) {
		this.InbodyDomainList.add(arrays);
		// Triggers the list update
		notifyDataSetChanged();

	}


	/*
	 * ViewHolder getView의 속도 향상을 위해 쓴다. 한번의 findViewByID 로 재사용 하기 위해
	 * viewHolder를 사용 한다.
	 */
	static class ViewHolder {

		TextView auctionItemName;
		TextView auctionItemPrice;
		TextView auctionItemEndFlag;
		ImageView auctionItemImage;
	}

	@Override
	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}

	private void free() {
		inflater = null;
		InbodyDomainList = null;
		viewHolder = null;
		mContext = null;
	}

}