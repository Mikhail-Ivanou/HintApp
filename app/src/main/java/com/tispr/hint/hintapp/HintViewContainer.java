package com.tispr.hint.hintapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class HintViewContainer extends RelativeLayout {
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
}
