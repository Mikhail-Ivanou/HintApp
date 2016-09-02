package com.tispr.hint.hintapp.cardstack.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tispr.hint.hintapp.R;
import com.tispr.hint.hintapp.cardstack.CardStackLayout;

import java.util.ArrayList;


public class CardStack extends RelativeLayout {

    public static final float DEFAULT_STACK_MARGIN = 20;
    public static final int DEFAULT_CARDS_COUNT = 3;

    private float mStackMargin = DEFAULT_STACK_MARGIN;
    //we need to have one extra card to animate to last one when top is discarded
    private int mNumVisible = DEFAULT_CARDS_COUNT +1;
    private ArrayAdapter<Object> mAdapter;
    private OnTouchListener mOnTouchListener;
    private CardAnimator mCardAnimator;

    //TODO discard offset to params
    private CardEventListener mEventListener = new DefaultStackEventListener(200);
    private int mContentResource = 0;
    private CardStackLayout.ICardSwipeListener mCardSwipeListener;

    public void setCardSwipeListener(CardStackLayout.ICardSwipeListener pCardSwipeListener) {
        this.mCardSwipeListener = pCardSwipeListener;
    }


    public interface CardEventListener {

        boolean swipeEnd(int section, float distance);

        boolean swipeStart(int section, float distance);

        boolean swipeContinue(int section, float distanceX, float distanceY);

        void discarded(int mIndex, int direction);

        void topCardTapped();
    }

    public void discardTop(final int direction) {
        mCardAnimator.discard(direction, new AnimatorListenerAdapter() {
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


    //only necessary when I need the attrs from xml, this will be used when inflating layout
    public CardStack(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CardStack);
            mStackMargin = array.getDimension(R.styleable.CardStack_stackMargin, mStackMargin);

            //we need to have one extra card to animate to last one when top is discarded
            mNumVisible = array.getInteger(R.styleable.CardStack_visibleCards, DEFAULT_CARDS_COUNT) +1;
            array.recycle();
        }

        //get attrs assign minVisiableNum
        for (int i = 0; i < mNumVisible; i++) {
            addContainerViews();
        }
        setupAnimation();
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

    public void setContentResource(int res) {
        mContentResource = res;
    }

    public void reset(boolean resetIndex) {
        removeAllViews();
        viewCollection.clear();
        for (int i = 0; i < mNumVisible; i++) {
            addContainerViews();
        }
        setupAnimation();
        loadData();
    }

    public void setVisibleCardNum(int visiableNum) {
        mNumVisible = visiableNum;
        reset(false);
    }

    public void setThreshold(int t) {
        mEventListener = new DefaultStackEventListener(t);
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
                final int direction = CardUtils.direction(x1, x2);
                float distance = CardUtils.distance(x1, y1, x2, y2);
                mEventListener.swipeStart(direction, distance);
                return true;
            }

            @Override
            public boolean onDragContinue(MotionEvent e1, MotionEvent e2,
                                          float distanceX, float distanceY) {
                float x1 = e1.getRawX();
                float y1 = e1.getRawY();
                float x2 = e2.getRawX();
                float y2 = e2.getRawY();
                final int direction = CardUtils.direction(x1, x2);
                mCardAnimator.drag(e1, e2, distanceX, distanceY);
                mEventListener.swipeContinue(direction, Math.abs(x2 - x1), Math.abs(y2 - y1));
                return true;
            }

            @Override
            public boolean onDragEnd(MotionEvent e1, MotionEvent e2) {
                //reverse(e1,e2);
                float x1 = e1.getRawX();
                float y1 = e1.getRawY();
                float x2 = e2.getRawX();
                float y2 = e2.getRawY();
                float distance = CardUtils.distance(x1, y1, x2, y2);
                final int direction = CardUtils.direction(x1, x2);

                boolean discard = mEventListener.swipeEnd(direction, distance);
                if (discard) {
                    mCardAnimator.discard(direction, new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator arg0) {
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
                    mCardAnimator.reverse(e1, e2);
                }
                return true;
            }

            @Override
            public boolean onTapUp() {
                mEventListener.topCardTapped();
                return true;
            }
        }
        );

        mOnTouchListener = new OnTouchListener() {
            private static final String DEBUG_TAG = "MotionEvents";

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                dd.onTouchEvent(event);
                return true;
            }
        };
        cardView.setOnTouchListener(mOnTouchListener);
    }

    private DataSetObserver mOb = new DataSetObserver() {
        @Override
        public void onChanged() {
            reset(false);
        }
    };


    //ArrayList

    ArrayList<View> viewCollection = new ArrayList<View>();

    public CardStack(Context context) {
        super(context);
    }

    public void setAdapter(final ArrayAdapter<Object> adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mOb);
        }
        mAdapter = adapter;
        adapter.registerDataSetObserver(mOb);

        loadData();
    }

    public ArrayAdapter getAdapter() {
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
                View child = mAdapter.getView(index, getContentView(), this);
                if (i == 1) {
                    //TODO initHints();
                }
                parent.addView(child);
                parent.setVisibility(View.VISIBLE);
            }
        }
    }

    private View getContentView() {
        View contentView = null;
        if (mContentResource != 0) {
            LayoutInflater lf = LayoutInflater.from(getContext());
            contentView = lf.inflate(mContentResource, null);
        }
        return contentView;

    }

    private void loadLast() {
        ViewGroup parent = (ViewGroup) viewCollection.get(0);

        int lastIndex = mNumVisible - 1;
        if (lastIndex > mAdapter.getCount() - 1) {
            parent.setVisibility(View.GONE);
            return;
        }

        View child = mAdapter.getView(lastIndex, getContentView(), parent);
        parent.removeAllViews();
        parent.addView(child);
    }

    public int getStackSize() {
        return mNumVisible;
    }
}
