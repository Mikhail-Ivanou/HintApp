package com.tispr.hint.hintapp.cardstack.cards.listeners;

public interface IDragListener {
    void onDragStart(float x);

    void onDragContinue(float x);

    void onDragEnd();
}
