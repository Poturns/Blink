package kr.poturns.blink.demo.fitnessapp.view;

import kr.poturns.blink.demo.fitnesswear.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WearableListItemLayout extends LinearLayout implements
		WearableListView.Item {

	private final float mFadedTextAlpha;
	private final int mFadedCircleColor;
	private final int mChosenCircleColor;
	private float mMinProximityValue;
	private float mMaxProximityValue;
	private View mCircleView;
	private float mScale;
	private boolean mDrawableChange;
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
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.WearableListItemLayout, 0, 0);

		try {
			mFadedCircleColor = a.getColor(
					R.styleable.WearableListItemLayout_fadedCircleColor,
					R.color.light_grey);
			mChosenCircleColor = a.getColor(
					R.styleable.WearableListItemLayout_chosenCircleColor,
					R.color.orange);
			mDrawableChange = a.getBoolean(
					R.styleable.WearableListItemLayout_enableDrawableChange,
					true);
			mMinProximityValue = a.getFloat(
					R.styleable.WearableListItemLayout_minProximityValue, 1.0f);
			mMaxProximityValue = a.getFloat(
					R.styleable.WearableListItemLayout_maxProximityValue, 1.8f);
		} finally {
			a.recycle();
		}

		// mFadedTextAlpha = getResources().getInteger(
		// R.integer.action_text_faded_alpha) / 100f;
		mFadedTextAlpha = 0.3f;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCircleView = findViewById(R.id.circle);
		mTitleView = (TextView) findViewById(R.id.name);
	}

	@Override
	public float getProximityMinValue() {
		return mMinProximityValue;
	}

	@Override
	public float getProximityMaxValue() {
		return mMaxProximityValue;
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
		if (mDrawableChange)
			((GradientDrawable) mCircleView.getBackground())
					.setColor(mChosenCircleColor);
	}

	@Override
	public void onScaleDownStart() {
		mTitleView.setAlpha(mFadedTextAlpha);
		if (mDrawableChange)
			((GradientDrawable) mCircleView.getBackground())
					.setColor(mFadedCircleColor);
	}
}