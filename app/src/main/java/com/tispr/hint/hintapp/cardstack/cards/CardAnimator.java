package com.tispr.hint.hintapp.cardstack.cards;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.tispr.hint.hintapp.HintViewContainer;
import com.tispr.hint.hintapp.cardstack.cards.utils.CardUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class CardAnimator {

    //TODO we need to calculate screen width to animate more definitely
    private static final int REMOTE_DISTANCE = 1000;

    public static final int SWIPE_OUT_DURATION = 400;
    public static final int RETURN_BACK_DURATION = 400;
    public static final int RESTACK_DURATION = 400;

    public ArrayList<View> mCardCollection;

    private HashMap<View, LayoutParams> mLayoutsMap;
    private RelativeLayout.LayoutParams[] mRemoteLayouts = new RelativeLayout.LayoutParams[2];
    private RelativeLayout.LayoutParams[] mRestackLayouts = new RelativeLayout.LayoutParams[4];
    private RelativeLayout.LayoutParams baseLayout;

    private int mStackMargin = (int) CardStack.DEFAULT_STACK_MARGIN;
    private int mSwipeOutDuration = (int) SWIPE_OUT_DURATION;
    private int mReturnBackDuration = (int) RETURN_BACK_DURATION;
    private int mRestackDuration = (int) RESTACK_DURATION;



    public CardAnimator(ArrayList<View> viewCollection, int stackMargin) {
        mCardCollection = viewCollection;
        mStackMargin = stackMargin;
        setup();

    }

    private void setup() {
        mLayoutsMap = new HashMap<View, LayoutParams>();

        for (View v : mCardCollection) {
            //setup basic layout
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.width = LayoutParams.MATCH_PARENT;
            params.height = LayoutParams.MATCH_PARENT;

            v.setLayoutParams(params);
        }

        baseLayout = (RelativeLayout.LayoutParams) mCardCollection.get(0).getLayoutParams();
        baseLayout = CardUtils.cloneParams(baseLayout);

        initLayout();

        for (int i = 0; i < mCardCollection.size(); i++) {
            View v = mCardCollection.get(i);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            RelativeLayout.LayoutParams paramsCopy = CardUtils.cloneParams(params);
            mLayoutsMap.put(v, paramsCopy);
            mRestackLayouts[i] = CardUtils.getMoveParams(v, REMOTE_DISTANCE, 0);
        }

        setupRemotes();

    }

    public void initLayout() {
        int size = mCardCollection.size();
        for (View v : mCardCollection) {
            int index = mCardCollection.indexOf(v);
            if (index != 0) {
                index -= 1;
            }
            LayoutParams params = CardUtils.cloneParams(baseLayout);
            v.setLayoutParams(params);

            CardUtils.scale(v, -(size - index - 1) * 5);
            CardUtils.move(v, index * mStackMargin, 0);
            v.setRotation(0);
        }
    }

    private void setupRemotes() {
        View topView = getTopView();
        mRemoteLayouts[0] = CardUtils.getMoveParams(topView, 0, -REMOTE_DISTANCE);
        mRemoteLayouts[1] = CardUtils.getMoveParams(topView, 0, REMOTE_DISTANCE);
    }

    private View getTopView() {
        return mCardCollection.get(mCardCollection.size() - 1);
    }

    private void moveToBack(View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    private void reorder() {
        View temp = getTopView();
        moveToBack(temp);
        for (int i = (mCardCollection.size() - 1); i > 0; i--) {
            View current = mCardCollection.get(i - 1);
            mCardCollection.set(i, current);

        }
        mCardCollection.set(0, temp);
    }

    public void discard(final int direction, final float xPosition, final HintViewContainer hintViewContainer, final boolean ignoreDragEventForHint, final AnimatorListener al) {
        AnimatorSet as = new AnimatorSet();
        ArrayList<Animator> aCollection = new ArrayList<Animator>();


        final View topView = getTopView();
        RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) topView.getLayoutParams();
        RelativeLayout.LayoutParams layout = CardUtils.cloneParams(topParams);
        ValueAnimator discardAnim = ValueAnimator.ofObject(new RelativeLayoutParamsEvaluator(), layout, mRemoteLayouts[direction]);

        discardAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                if (!ignoreDragEventForHint) {
                    int directionValue = CardUtils.DIRECTION_LEFT == direction ? -1 : 1;
                    float animatedFraction = value.getAnimatedFraction();
                    float calculatedPosition = xPosition + animatedFraction * REMOTE_DISTANCE * directionValue;
                    hintViewContainer.onDragContinue(calculatedPosition);
                }
                topView.setLayoutParams((LayoutParams) value.getAnimatedValue());


            }
        });

        discardAnim.setDuration(mSwipeOutDuration);
        aCollection.add(discardAnim);

        for (int i = 0; i < mCardCollection.size(); i++) {
            final View v = mCardCollection.get(i);

            if (v == topView) continue;
            final View nv = mCardCollection.get(i + 1);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
            RelativeLayout.LayoutParams endLayout = CardUtils.cloneParams(layoutParams);
            ValueAnimator layoutAnim = ValueAnimator.ofObject(new RelativeLayoutParamsEvaluator(), endLayout, mLayoutsMap.get(nv));
            layoutAnim.setDuration(mSwipeOutDuration);
            layoutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator value) {
                    v.setLayoutParams((LayoutParams) value.getAnimatedValue());
                }
            });
            aCollection.add(layoutAnim);
        }

        as.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                reorder();
                if (al != null) {
                    al.onAnimationEnd(animation);
                }
                mLayoutsMap = new HashMap<>();
                for (View v : mCardCollection) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    RelativeLayout.LayoutParams paramsCopy = CardUtils.cloneParams(params);
                    mLayoutsMap.put(v, paramsCopy);
                }
            }

        });

        as.playTogether(aCollection);
        as.start();
    }

    public void reverse(final int direction, final float xPosition, final HintViewContainer hintViewContainer, final boolean ignoreDragEventForHint) {
        final View topView = getTopView();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) topView.getLayoutParams();
        RelativeLayout.LayoutParams endLayout = CardUtils.cloneParams(layoutParams);
        ValueAnimator layoutAnim = ValueAnimator.ofObject(new RelativeLayoutParamsEvaluator(), endLayout, mLayoutsMap.get(topView));
        layoutAnim.setDuration(mReturnBackDuration);
        layoutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                if (!ignoreDragEventForHint) {
                    //reverse direction
                    int directionValue = CardUtils.DIRECTION_LEFT == direction ? 1 : -1;
                    float animatedFraction = value.getAnimatedFraction();
                    float calculatedPosition = xPosition + animatedFraction * REMOTE_DISTANCE * directionValue;
                    hintViewContainer.onDragContinue(calculatedPosition);
                }
                topView.setLayoutParams((LayoutParams) value.getAnimatedValue());
            }
        });
        layoutAnim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                hintViewContainer.hideAllViews();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        layoutAnim.start();

    }

    public void drag(MotionEvent e1, MotionEvent e2, float distanceX,
                     float distanceY) {

        View topView = getTopView();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) topView.getLayoutParams();
        RelativeLayout.LayoutParams topViewLayouts = mLayoutsMap.get(topView);
        int x_diff = (int) ((e2.getRawX() - e1.getRawX()));

        layoutParams.leftMargin = topViewLayouts.leftMargin + x_diff;
        layoutParams.rightMargin = topViewLayouts.rightMargin - x_diff;

        topView.setLayoutParams(layoutParams);
    }

    public void setStackMargin(int margin) {
        mStackMargin = margin;
        initLayout();
    }

    public void restack() {
        AnimatorSet as = new AnimatorSet();
        ArrayList<Animator> aCollection = new ArrayList<Animator>();
        final View lastView = mCardCollection.get(0);
        lastView.setVisibility(View.GONE);
        for (int i = 1; i < 4; i++) {
            final View v = mCardCollection.get(i);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
            RelativeLayout.LayoutParams endLayout = CardUtils.cloneParams(layoutParams);
            CardUtils.move(v, REMOTE_DISTANCE, 0);
            ValueAnimator layoutAnim = ValueAnimator.ofObject(new RelativeLayoutParamsEvaluator(), mRestackLayouts[i], endLayout);
            layoutAnim.setDuration(mRestackDuration);
            layoutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator value) {
                    v.setLayoutParams((LayoutParams) value.getAnimatedValue());

                }
            });
            if (i == 1) {
                layoutAnim.addListener(new AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        lastView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
            aCollection.add(layoutAnim);


        }

        as.playSequentially(aCollection);
        as.start();

    }

    public void setSwipeOutDuration(int pSwipeOutDuration) {
        this.mSwipeOutDuration = pSwipeOutDuration;
    }

    public void setReturnBackDuration(int pReturnBackDuration) {
        this.mReturnBackDuration = pReturnBackDuration;
    }

    public void setRestackDuration(int pRestackDuration) {
        this.mRestackDuration = pRestackDuration;
    }
}
