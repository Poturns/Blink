package com.example.auctionrealtimetest;

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

public class CustomBaseAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private ArrayList<InbodyListDomain> infoList = null;
	private ViewHolder viewHolder = null;
	private Context mContext = null;
	private String bodyType = null;

	public CustomBaseAdapter(Context c, ArrayList<InbodyListDomain> arrays) {
		this.mContext = c;
		this.inflater = LayoutInflater.from(c);
		this.infoList = arrays;
	}

	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}

	// Adapter�� ������ Data�� ������ ���� �մϴ�.
	@Override
	public int getCount() {
		return infoList.size();
	}

	// Adapter�� �����ϴ� Data�� Item �� Position�� <��ü> ���·� ��� �ɴϴ�.
	@Override
	public InbodyListDomain getItem(int position) {
		return infoList.get(position);
	}

	// Adapter�� �����ϴ� Data�� Item �� position ���� ID �� ��� �ɴϴ�.
	@Override
	public long getItemId(int position) {
		return position;
	}

	// ListView�� �ѷ��� ������ Row�� ���� �մϴ�.
	@Override
	public View getView(int position, View convertview, ViewGroup parent) {

		View v = convertview;
		View v2 = inflater.inflate(R.layout.list_auction_row2, null);
		View v3 = inflater.inflate(R.layout.list_auction_row3, null);
		// if(v == null){
		viewHolder = new ViewHolder();
		v = inflater.inflate(R.layout.list_auction_row, null);

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
			viewHolder.auctionItemName.setText("�̸�");
			if (bodyType.equals("fat")) {
				viewHolder.auctionItemPrice.setText("������");
			} else if (bodyType.equals("muscle")) {
				viewHolder.auctionItemPrice.setText("�����");

			} else if (bodyType.equals("avg")) {
				viewHolder.auctionItemPrice.setText("�迬ȣ");
			}
		} else if (position == 1) {
			viewHolder.auctionItemImage
					.setImageResource(R.drawable.personalinfo);
			viewHolder.auctionItemName.setText("����");
			if (bodyType.equals("fat")) {
				viewHolder.auctionItemPrice.setText("46��");
			} else if (bodyType.equals("muscle")) {
				viewHolder.auctionItemPrice.setText("25��");

			} else if (bodyType.equals("avg")) {
				viewHolder.auctionItemPrice.setText("32��");
			}
		} else if (position == 2) {
			viewHolder.auctionItemImage.setImageResource(R.drawable.bodycm);
			viewHolder.auctionItemName.setText("Ű");
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
			viewHolder.auctionItemName.setText("ü��");
			if (bodyType.equals("fat")) {
				viewHolder.auctionItemPrice.setText("100kg");
			} else if (bodyType.equals("muscle")) {
				viewHolder.auctionItemPrice.setText("83kg");

			} else if (bodyType.equals("avg")) {
				viewHolder.auctionItemPrice.setText("75kg");
			}
		} else if (position == 4) {
		//	viewHolder.auctionItemImage.setImageResource(R.drawable.musclekg);
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("������"); // city
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
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("���淮"); // city
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
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("ü������"); // city
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
			// viewHolder.auctionItemName.setText("ü������");
			return v2;
		} else if (position == 7) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("��������"); // city
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
			 * viewHolder.auctionItemName.setText("��������");
			 */
			return v2;
		} else if (position == 8) {
			((TextView) v2.findViewById(R.id.auctionItemName)).setText("��������"); // city
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
			 * viewHolder.auctionItemName.setText("��������");
			 */
			return v2;
		} else if (position == 9) {
			((TextView) v3.findViewById(R.id.auctionItemName)).setText("���ʴ�緮"); // city
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
					.setText("�ʿ� ���"); // city weather overview
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
		 * // image �� button � Tag�� ����ؼ� position �� �ο��� �ش�. // Tag�� View�� �ĺ��� ��
		 * �ְ� ���ڵ� ó�� Tag�� �޾� �ִ� View�� ��� // �̶�� ���� �Ͻø� �˴ϴ�.
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

	// Adapter�� �����ϴ� Data List�� ��ü �Ѵ�.
	// ��ü �� Adapter.notifyDataSetChanged() �޼���� ���� �����
	// Adapter�� �˷� �־� ListView�� ���� �ǵ��� �Ѵ�.
	public void setArrayList(ArrayList<InbodyListDomain> arrays) {
		this.infoList = arrays;
	}

	public ArrayList<InbodyListDomain> getArrayList() {
		return infoList;
	}

	public void addArrayList(InbodyListDomain arrays) {
		this.infoList.add(arrays);
		// Triggers the list update
		notifyDataSetChanged();

	}

	private View.OnClickListener buttonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			/*
			 * switch (v.getId()) {
			 * 
			 * // �̹��� Ŭ�� case R.id.iv_image: Toast.makeText( mContext,
			 * "�̹��� Tag = " + v.getTag(), Toast.LENGTH_SHORT ).show(); break;
			 * 
			 * // ��ư Ŭ�� case R.id.btn_button: Toast.makeText( mContext,
			 * "��ư Tag = " + v.getTag(), Toast.LENGTH_SHORT ).show(); break;
			 * 
			 * // CheckBox case R.id.cb_box: Toast.makeText( mContext,
			 * "üũ�ڽ� Tag = " + v.getTag(), Toast.LENGTH_SHORT ).show(); break;
			 * 
			 * default: break; }
			 */
		}
	};

	/*
	 * ViewHolder getView�� �ӵ� ����� ���� ����. �ѹ��� findViewByID �� ���� �ϱ� ����
	 * viewHolder�� ��� �Ѵ�.
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
		infoList = null;
		viewHolder = null;
		mContext = null;
	}

}