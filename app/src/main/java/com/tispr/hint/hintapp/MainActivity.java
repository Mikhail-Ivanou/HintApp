package com.tispr.hint.hintapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.tispr.hint.hintapp.cardstack.cards.CardStack;
import com.tispr.hint.hintapp.cardstack.cards.listeners.ICardSwipeListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardStack mCardStack = (CardStack) findViewById(R.id.cardStack);

        List<String> items = new ArrayList<>();

        items.add("test1");
        items.add("test2");
        items.add("test3");
        items.add("test4");
        items.add("test5");
        items.add("test6");
        items.add("test7");
        items.add("test8");
        items.add("test9");

        CardsDataAdapter adapter = new CardsDataAdapter(getApplicationContext(), R.layout.card_content, items);

        mCardStack.setAdapter(adapter);

        mCardStack.setCardSwipeListener(new ICardSwipeListener<String>() {

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
