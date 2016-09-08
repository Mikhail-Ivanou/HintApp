package com.tispr.hint.hintapp;

import android.content.Context;
import android.widget.TextView;

import com.tispr.hint.hintapp.cardstack.cards.adapter.AbstractAdapterWithViewHolder;

import java.util.List;

public class CardsDataAdapter extends AbstractAdapterWithViewHolder<String> {

    public CardsDataAdapter(Context context, int pLayoutId, List<String> pList) {
        super(context, pLayoutId, pList);
    }

    @Override
    public void init(ViewHolder view, String item) {
        TextView v = (TextView) (view.findViewById(R.id.content));
        v.setText(item);
    }
}

