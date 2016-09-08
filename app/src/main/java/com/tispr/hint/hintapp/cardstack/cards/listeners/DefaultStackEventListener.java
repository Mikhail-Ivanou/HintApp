package com.tispr.hint.hintapp.cardstack.cards.listeners;

import com.tispr.hint.hintapp.cardstack.cards.CardStack;

public class DefaultStackEventListener implements CardEventListener {

    private float mThreshold;

    public DefaultStackEventListener(int i) {
        mThreshold = i;
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
