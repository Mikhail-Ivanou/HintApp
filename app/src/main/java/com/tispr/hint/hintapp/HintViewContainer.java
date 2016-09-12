package com.tispr.hint.hintapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.tispr.hint.hintapp.cardstack.cards.listeners.IDragListener;

public class HintViewContainer extends RelativeLayout implements IDragListener {
    public HintViewContainer(Context context) {
        super(context);
    }

    public HintViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HintViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HintViewContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).onTouchEvent(event);
        }
        return false;
    }


    @Override
    public void onDragStart(float x) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof IDragListener) {
                ((IDragListener) view).onDragStart(x);
            }
        }
    }

    @Override
    public void onDragContinue(float x) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof IDragListener) {
                ((IDragListener) view).onDragContinue(x);
            }
        }
    }

    @Override
    public void onDragEnd() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof IDragListener) {
                ((IDragListener) view).onDragEnd();
            }
        }
    }

    public void hideAllViews() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.setVisibility(View.INVISIBLE);
        }

    }
}
