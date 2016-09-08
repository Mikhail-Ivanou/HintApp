package com.tispr.hint.hintapp;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class HintView extends AppCompatImageView {

    private final int mScreenWidth = getResources().getDisplayMetrics().widthPixels;

    private float mMinSize = dpToPx(32);

    private float mMaxSize = dpToPx(150);

    private float mShowViewOffset = 0.2f;

    private float mMaxViewOffset = 0.6f;

    private float mChangeImageOffset = 0.5f;

    private Drawable mSrcDrawable;

    private Drawable mSecondSrcDrawable;

    private float mSecondSrcOffset = 0.45f;

    //-1 - left, 1 - right
    private int mDirection = 1;

    private float mPercentPoint;

    private int mCurrentDrawable = DRAWABLE_SRC;

    private static final int DRAWABLE_SRC = 1;

    private static final int DRAWABLE_SECOND_SRC = 2;

    public HintView(Context context) {
        super(context);
        init(context, null);
    }

    public HintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HintView);
        mMinSize = a.getDimension(R.styleable.HintView_minSize, mMinSize);
        mMaxSize = a.getDimension(R.styleable.HintView_maxSize, mMaxSize);
        mDirection = a.getInt(R.styleable.HintView_direction, mDirection);
        mShowViewOffset = a.getFloat(R.styleable.HintView_showViewOffset, mShowViewOffset);
        mMaxViewOffset = a.getFloat(R.styleable.HintView_maxViewOffset, mMaxViewOffset);
        mChangeImageOffset = a.getFloat(R.styleable.HintView_changeImageOffset, mChangeImageOffset);
        mSecondSrcDrawable = a.getDrawable(R.styleable.HintView_secondSrc);
        mSecondSrcOffset = a.getFloat(R.styleable.HintView_secondSrcOffset, mSecondSrcOffset);
        mSrcDrawable = getDrawable();
        mPercentPoint = (mMaxSize - mMinSize) / ((mMaxViewOffset - mShowViewOffset) * 100);
        if (!isInEditMode()) {
            setVisibility(View.INVISIBLE);
        }

        a.recycle();
    }

    private float mStartPosition;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e("test", "actionDown");
                proceedActionDown(event.getRawX());
                break;
            case MotionEvent.ACTION_MOVE:
                proceedActionMove(event.getRawX());
                break;
            case MotionEvent.ACTION_UP:
                Log.e("test", "actionUp");
                proceedActionUp(event.getRawX());
                break;
        }
        return true;
    }

    private void proceedActionDown(float pXPosition) {
        mStartPosition = pXPosition;
    }

    private void proceedActionMove(float pXPosition) {
        float position = swipeInPercent(pXPosition);
        Log.e("test", "position = " +position);
        if (position > mShowViewOffset) {
            Log.e("test", "setVisible");
            setVisibility(View.VISIBLE);
            if (position < mSecondSrcOffset && mCurrentDrawable != DRAWABLE_SRC) {
                setImageDrawable(mSrcDrawable);
                mCurrentDrawable = DRAWABLE_SRC;
            }
            if (position >= mSecondSrcOffset && mCurrentDrawable != DRAWABLE_SECOND_SRC) {
                setImageDrawable(mSecondSrcDrawable);
                mCurrentDrawable = DRAWABLE_SECOND_SRC;
            }
            if (position <= mMaxViewOffset) {
                setViewSize((position - mShowViewOffset) * 100 * mPercentPoint + mMinSize);
            }

        } else {
            Log.e("test", "setGone");
            setVisibility(View.INVISIBLE);
        }
    }

    private void proceedActionUp(float pXPosition) {
        mStartPosition = 0;
        setVisibility(View.INVISIBLE);
    }

    private float dpToPx(int value) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, r.getDisplayMetrics());
    }

    private void setViewSize(float size) {
        ViewGroup.LayoutParams p = getLayoutParams();
        if (p != null) {
            p.height = (int) size;
            p.width = (int) size;
            requestLayout();
        }
    }

    private float swipeInPercent(float position) {
        return (position - mStartPosition) / mScreenWidth * mDirection;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }
}
