package com.tispr.hint.hintapp.cardstack;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.tispr.hint.hintapp.CardsDataAdapter;
import com.tispr.hint.hintapp.R;
import com.tispr.hint.hintapp.cardstack.cards.CardStack;

/**
 * Created by mikhail.ivanou on 01.09.16.
 */
public class CardStackLayout extends RelativeLayout {


    private CardStack mCardStack;

    public CardStackLayout(Context context) {
        super(context);
    }

    public CardStackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardStackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardStackLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(Context context, int pContentResource) {
        mCardStack = (CardStack) findViewById(R.id.cardStack);
        mCardStack.setContentResource(pContentResource);
        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mCardStack.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    public void setAdapter(CardsDataAdapter pCardAdapter) {
        mCardStack.setAdapter(pCardAdapter);
    }

    public void setCardSwipeListener(ICardSwipeListener listener) {
        mCardStack.setCardSwipeListener(listener);
    }


    public interface ICardSwipeListener<T> {

        void onCardDeleted(T dataObject);

        void onCardReordered(T dataObject);


    }
}
