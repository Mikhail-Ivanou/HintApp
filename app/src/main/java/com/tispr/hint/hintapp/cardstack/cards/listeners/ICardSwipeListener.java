package com.tispr.hint.hintapp.cardstack.cards.listeners;

public interface ICardSwipeListener<T> {

    void onCardDeleted(T dataObject);

    void onCardReordered(T dataObject);

}
