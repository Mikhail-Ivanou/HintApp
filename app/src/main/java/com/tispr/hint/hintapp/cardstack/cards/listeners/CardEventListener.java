package com.tispr.hint.hintapp.cardstack.cards.listeners;

public interface CardEventListener {
    boolean swipeEnd(int section, float distance);

    boolean swipeStart(int section, float distance);

    boolean swipeContinue(int section, float distanceX, float distanceY);

    void discarded(int mIndex, int direction);

    void topCardTapped();
}
