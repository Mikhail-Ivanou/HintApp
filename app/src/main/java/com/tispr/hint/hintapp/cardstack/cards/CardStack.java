package com.tispr.hint.hintapp.cardstack.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tispr.hint.hintapp.HintViewContainer;
import com.tispr.hint.hintapp.R;
import com.tispr.hint.hintapp.cardstack.cards.adapter.AbstractAdapterWithViewHolder;
import com.tispr.hint.hintapp.cardstack.cards.listeners.CardEventListener;
import com.tispr.hint.hintapp.cardstack.cards.listeners.DefaultStackEventListener;
import com.tispr.hint.hintapp.cardstack.cards.listeners.ICardSwipeListener;
import com.tispr.hint.hintapp.cardstack.cards.utils.CardUtils;

import java.util.ArrayList;


public class CardStack extends RelativeLayout {

    public static final float DEFAULT_STACK_MARGIN = 20;
    public static final int DEFAULT_CARDS_COUNT = 3;

    private float mStackMargin = DEFAULT_STACK_MARGIN;
    //we need to have one extra card to animate to last one when top is discarded
    private int mNumVisible = DEFAULT_CARDS_COUNT +1;
    private AbstractAdapterWithViewHolder mAdapter;
    private OnTouchListener mOnTouchListener;
    private CardAnimator mCardAnimator;

    private CardEventListener mEventListener;
    private ICardSwipeListener mCardSwipeListener;


    private HintViewContainer mHintViewContainer;

    private float mDiscardOffset = 0;

    public void setCardSwipeListener(ICardSwipeListener pCardSwipeListener) {
        this.mCardSwipeListener = pCardSwipeListener;
    }

    public void discardTop(final int direction) {
        mCardAnimator.discard(direction, 0, mHintViewContainer, needIgnoreDragEventForHint(direction), new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator arg0) {
                mCardAnimator.initLayout();
                loadLast();

                viewCollection.get(0).setOnTouchListener(null);
                viewCollection.get(viewCollection.size() - 1).setOnTouchListener(mOnTouchListener);
                mEventListener.discarded(0, direction);
            }
        });
    }


    public CardStack(Context context, AttributeSet attrs) {
        super(context, attrs);
        int restackDuration = CardAnimator.RESTACK_DURATION;
        int swipeOutDuration = CardAnimator.SWIPE_OUT_DURATION;
        int returnBackDuration = CardAnimator.RETURN_BACK_DURATION;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CardStack);
            mStackMargin = array.getDimension(R.styleable.CardStack_stackMargin, mStackMargin);

            //we need to have one extra card to animate to last one when top is discarded
            mNumVisible = array.getInteger(R.styleable.CardStack_visibleCards, DEFAULT_CARDS_COUNT) +1;

            restackDuration = array.getInteger(R.styleable.CardStack_restackAnimationDuration, restackDuration);
            swipeOutDuration = array.getInteger(R.styleable.CardStack_swipeOutAnimationDuration, swipeOutDuration);
            returnBackDuration = array.getInteger(R.styleable.CardStack_returnBackAnimationDuration, returnBackDuration);

            int hintLayoutId = array.getResourceId(R.styleable.CardStack_hintLayout, 0);
            mHintViewContainer = (HintViewContainer) LayoutInflater.from(context).inflate(hintLayoutId, null);
            mDiscardOffset = array.getDimension(R.styleable.CardStack_discardOffset, mDiscardOffset);

            array.recycle();
        }

        setThreshold(mDiscardOffset);

        //get attrs assign minVisiableNum
        for (int i = 0; i < mNumVisible; i++) {
            if (i + 1 == mNumVisible) {
                addView(mHintViewContainer);
            }
            addContainerViews();
        }
        setupAnimation();
        mCardAnimator.setRestackDuration(restackDuration);
        mCardAnimator.setReturnBackDuration(returnBackDuration);
        mCardAnimator.setSwipeOutDuration(swipeOutDuration);
    }

    private void addContainerViews() {
        FrameLayout v = new FrameLayout(getContext());
        viewCollection.add(v);
        addView(v);
    }

    public void setStackMargin(int margin) {
        mStackMargin = margin;
        mCardAnimator.setStackMargin((int) mStackMargin);
        mCardAnimator.initLayout();
    }

    public void reset() {
        removeAllViews();
        viewCollection.clear();
        for (int i = 0; i < mNumVisible; i++) {
            if (i + 1 == mNumVisible) {
                addView(mHintViewContainer);
            }
            addContainerViews();
        }
        setupAnimation();
        loadData();
    }

    public void setVisibleCardNum(int visiableNum) {
        mNumVisible = visiableNum;
        reset();
    }

    public void setThreshold(float discardOffset) {
        mEventListener = new DefaultStackEventListener(discardOffset);
    }

    public void setListener(CardEventListener cel) {
        mEventListener = cel;
    }

    private void setupAnimation() {
        final View cardView = viewCollection.get(viewCollection.size() - 1);
        mCardAnimator = new CardAnimator(viewCollection, (int) mStackMargin);
        mCardAnimator.initLayout();

        final DragGestureDetector dd = new DragGestureDetector(CardStack.this.getContext(), new DragGestureDetector.DragListener() {

            @Override
            public boolean onDragStart(MotionEvent e1, MotionEvent e2,
                                       float distanceX, float distanceY) {
                mCardAnimator.drag(e1, e2, distanceX, distanceY);
                float x1 = e1.getRawX();
                float y1 = e1.getRawY();
                float x2 = e2.getRawX();
                float y2 = e2.getRawY();
                Log.e("test", "onDragStart x2=" + x2);
                final int direction = CardUtils.direction(x1, x2);
                float distance = CardUtils.distance(x1, y1, x2, y2);
                mEventListener.swipeStart(direction, distance);
                if (!needIgnoreDragEventForHint(direction)) {
                    mHintViewContainer.onDragStart(x2);
                }
                mCardSwipeListener.onSwipeStarted();
                return true;
            }

            @Override
            public boolean onDragContinue(MotionEvent e1, MotionEvent e2,
                                          float distanceX, float distanceY) {
                Log.e("test", "onDragContinue");
                float x1 = e1.getRawX();
                float y1 = e1.getRawY();
                float x2 = e2.getRawX();
                float y2 = e2.getRawY();
                Log.e("test", "onDragContinue x2=" + x2);
                final int direction = CardUtils.direction(x1, x2);
                mCardAnimator.drag(e1, e2, distanceX, distanceY);
                mEventListener.swipeContinue(direction, Math.abs(x2 - x1), Math.abs(y2 - y1));
                if (!needIgnoreDragEventForHint(direction)) {
                    mHintViewContainer.onDragContinue(x2);
                }
                return true;
            }

            @Override
            public boolean onDragEnd(MotionEvent e1, MotionEvent e2) {
                //reverse(e1,e2);
                Log.e("test", "onDragEnd");
                float x1 = e1.getRawX();
                float y1 = e1.getRawY();
                float x2 = e2.getRawX();
                float y2 = e2.getRawY();
                float distance = CardUtils.distance(x1, y1, x2, y2);
                final int direction = CardUtils.direction(x1, x2);
                boolean ignoreDragEventForHint = needIgnoreDragEventForHint(direction);
                Log.e("test", "onDragEnd x2=" + x2 + " direction = " +direction);
                mCardSwipeListener.onSwipeFinished();
                boolean discard = mEventListener.swipeEnd(direction, distance);
                if (discard) {
                    Log.e("test", "startDiscard x1= " + x1 + " x2=" + x2);
                    mCardAnimator.discard(direction, x2, mHintViewContainer, ignoreDragEventForHint, new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator arg0) {
                            if (!needIgnoreDragEventForHint(direction)) {
                                mHintViewContainer.onDragEnd();
                            }
                            mCardAnimator.initLayout();
                            mEventListener.discarded(0, direction);
                            Object item = mAdapter.getItem(0);
                            mAdapter.remove(item);
                            if (direction == CardUtils.DIRECTION_RIGHT) {
                                mAdapter.add(item);
                                mCardSwipeListener.onCardReordered(item);
                            } else {
                                mCardSwipeListener.onCardDeleted(item);
                            }
                            mAdapter.notifyDataSetChanged();
                            //mIndex = mIndex%mAdapter.getCount();
                            loadLast();

                            viewCollection.get(0).setOnTouchListener(null);
                            viewCollection.get(viewCollection.size() - 1)
                                    .setOnTouchListener(mOnTouchListener);
                        }

                    });
                } else {
                    mCardAnimator.reverse(direction, x2, mHintViewContainer, ignoreDragEventForHint);
                }
                return true;
            }

            @Override
            public boolean onTapUp() {
                Log.e("test", "onTapUp");
                mEventListener.topCardTapped();
                return true;
            }
        }
        );

        mOnTouchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                dd.onTouchEvent(event);
                return true;
            }
        };
        cardView.setOnTouchListener(mOnTouchListener);
    }

    private boolean needIgnoreDragEventForHint(int direction) {
        return CardUtils.DIRECTION_RIGHT == direction && mAdapter.getCount() == 1;
    }

    private DataSetObserver mOb = new DataSetObserver() {
        @Override
        public void onChanged() {
            reset();
        }
    };


    //ArrayList

    ArrayList<View> viewCollection = new ArrayList<View>();

    public CardStack(Context context) {
        super(context);
    }

    public void setAdapter(final AbstractAdapterWithViewHolder adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mOb);
        }
        mAdapter = adapter;
        adapter.registerDataSetObserver(mOb);

        loadData();
    }

    public AbstractAdapterWithViewHolder getAdapter() {
        return mAdapter;
    }

    public View getTopView() {
        return ((ViewGroup) viewCollection.get(viewCollection.size() - 1)).getChildAt(0);
    }

    private void loadData() {
        for (int i = mNumVisible - 1; i >= 0; i--) {
            ViewGroup parent = (ViewGroup) viewCollection.get(i);
            int index = ( mNumVisible - 1) - i;
            if (index > mAdapter.getCount() - 1) {
                parent.setVisibility(View.GONE);
            } else {
                View child = mAdapter.getView(index, null, this);
                parent.addView(child);
                parent.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadLast() {
        ViewGroup parent = (ViewGroup) viewCollection.get(0);

        int lastIndex = mNumVisible - 1;
        if (lastIndex > mAdapter.getCount() - 1) {
            parent.setVisibility(View.GONE);
            return;
        }

        View child = mAdapter.getView(lastIndex, null, parent);
        parent.removeAllViews();
        parent.addView(child);
    }

    public int getStackSize() {
        return mNumVisible;
    }

    public void restackOnRefresh() {
        mCardAnimator.restack();
    }
}
