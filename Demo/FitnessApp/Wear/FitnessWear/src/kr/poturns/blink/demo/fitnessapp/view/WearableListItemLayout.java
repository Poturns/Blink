package kr.poturns.blink.demo.fitnessapp.view;

import kr.poturns.blink.demo.fitnesswear.R;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WearableListItemLayout extends LinearLayout implements
		WearableListView.Item {

	private final float mFadedTextAlpha;
	private final int mFadedCircleColor;
	private final int mChosenCircleColor;
	private ImageView mCircleView;
	private float mScale;
	private TextView mTitleView;

	public WearableListItemLayout(Context context) {
		this(context, null);
	}

	public WearableListItemLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WearableListItemLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// mFadedTextAlpha = getResources().getInteger(
		// R.integer.action_text_faded_alpha) / 100f;
		mFadedTextAlpha = 0.3f;
		mFadedCircleColor = getResources().getColor(R.color.light_grey);
		mChosenCircleColor = getResources().getColor(R.color.orange);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCircleView = (ImageView) findViewById(R.id.circle);
		mTitleView = (TextView) findViewById(R.id.name);
	}

	@Override
	public float getProximityMinValue() {
		return 1f;
	}

	@Override
	public float getProximityMaxValue() {
		return 1.6f;
	}

	@Override
	public float getCurrentProximityValue() {
		return mScale;
	}

	@Override
	public void setScalingAnimatorValue(float scale) {
		mScale = scale;
		mCircleView.setScaleX(scale);
		mCircleView.setScaleY(scale);
	}

	@Override
	public void onScaleUpStart() {
		mTitleView.setAlpha(1f);
		((GradientDrawable) mCircleView.getBackground()).setColor(mChosenCircleColor);
	}

	@Override
	public void onScaleDownStart() {
		((GradientDrawable) mCircleView.getBackground()).setColor(mFadedCircleColor);
		mTitleView.setAlpha(mFadedTextAlpha);
	}
}