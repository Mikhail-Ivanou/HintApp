package com.tispr.hint.hintapp.cardstack.cards.listeners;

public class DefaultStackEventListener implements CardEventListener {

    private float mThreshold;

    public DefaultStackEventListener(float discardOffset) {
        mThreshold = discardOffset;
    }

    @Override
    public boolean swipeEnd(int section, float distance) {
        return distance > mThreshold;
    }

    @Override
    public boolean swipeStart(int section, float distance) {
        return false;
    }

    @Override
    public boolean swipeContinue(int section, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void discarded(int mIndex,int direction) {

    }

    @Override
    public void topCardTapped() {

    }


}
