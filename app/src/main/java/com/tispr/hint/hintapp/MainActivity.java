package com.tispr.hint.hintapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.tispr.hint.hintapp.cardstack.CardStackLayout;
import com.tispr.hint.hintapp.cardstack.cards.CardStack;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardStackLayout mCardStack = (CardStackLayout) findViewById(R.id.container);
        mCardStack.init(this, R.layout.card_content);

        CardsDataAdapter<String> mCardAdapter = new CardsDataAdapter<String>(getApplicationContext(), R.layout.card_content);
        mCardAdapter.add("test1");
        mCardAdapter.add("test2");
        mCardAdapter.add("test3");
        mCardAdapter.add("test4");
        mCardAdapter.add("test5");
        mCardAdapter.add("test6");
        mCardAdapter.add("test7");
        mCardAdapter.add("test8");
        mCardAdapter.add("test9");

        mCardStack.setAdapter(mCardAdapter);

        mCardStack.setCardSwipeListener(new CardStackLayout.ICardSwipeListener<String>() {

            @Override
            public void onCardDeleted(String dataObject) {
                Toast.makeText(MainActivity.this, "deleted " + dataObject, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardReordered(String dataObject) {
                Toast.makeText(MainActivity.this, "reordered " + dataObject, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
