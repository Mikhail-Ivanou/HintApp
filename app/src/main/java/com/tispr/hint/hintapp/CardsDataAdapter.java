package com.tispr.hint.hintapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CardsDataAdapter<T> extends ArrayAdapter<T> {

    private int mLayout;
    private LayoutInflater mLayoutInflator;

    public CardsDataAdapter(Context context, int pLayoutId) {
        super(context, pLayoutId);
        mLayout = pLayoutId;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent) {
        //we need logic to reuse views

        TextView v = (TextView) (contentView.findViewById(R.id.content));
        v.setText((String) getItem(position));
        return contentView;
    }

}

