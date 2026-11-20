package com.bitlifebr.game;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitlifebr.game.character.CharacterState;
import com.bitlifebr.game.core.CharacterFactory;
import com.bitlifebr.game.database.GameDatabase;

import java.util.Random;

public final class MainActivity extends Activity {
    private GameDatabase database;
    private CharacterState character;
    private TextView nameView;
    private TextView ageView;
    private TextView statsView;
    private TextView statusView;
    private Button advanceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new GameDatabase(this);
        character = database.load();
        buildScreen();
        render();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(24));
        root.setGravity(Gravity.TOP);
        root.setBackgroundColor(Color.rgb(247, 248, 247));

        TextView title = text(getString(R.string.title), 27, Color.rgb(30, 40, 32));
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        TextView subtitle = text(getString(R.string.subtitle), 15, Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, matchWrap());

        nameView = text("", 23, Color.rgb(35, 105, 61));
        nameView.setGravity(Gravity.CENTER);
        nameView.setPadding(0, dp(36), 0, dp(8));
        root.addView(nameView, matchWrap());

        ageView = text("", 18, Color.DKGRAY);
        ageView.setGravity(Gravity.CENTER);
        root.addView(ageView, matchWrap());

        statsView = text("", 14, Color.DKGRAY);
        statsView.setGravity(Gravity.CENTER);
        statsView.setPadding(0, dp(14), 0, dp(24));
        root.addView(statsView, matchWrap());

        Button newLife = new Button(this);
        newLife.setText(getString(R.string.new_life));
        newLife.setOnClickListener(v -> {
            character = CharacterFactory.create(new Random());
            database.save(character);
            statusView.setText(getString(R.string.status_created));
            render();
        });
        root.addView(newLife, matchWrap());

        advanceButton = new Button(this);
        advanceButton.setText(getString(R.string.advance_year));
        advanceButton.setOnClickListener(v -> {
            if (character == null) return;
            character = character.nextYear();
            database.save(character);
            statusView.setText(getString(R.string.status_advanced));
            render();
        });
        LinearLayout.LayoutParams buttonParams = matchWrap();
        buttonParams.topMargin = dp(8);
        root.addView(advanceButton, buttonParams);

        statusView = text(getString(R.string.no_character), 14, Color.DKGRAY);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(24), 0, dp(8));
        root.addView(statusView, matchWrap());

        TextView offline = text(getString(R.string.offline), 12, Color.GRAY);
        offline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footer = matchWrap();
        footer.topMargin = dp(24);
        root.addView(offline, footer);

        setContentView(root);
    }

    private void render() {
        boolean exists = character != null;
        nameView.setText(exists ? character.name : getString(R.string.no_character));
        ageView.setText(exists ? getString(R.string.age, character.age) : "");
        statsView.setText(exists ? getString(R.string.stats, character.happiness,
                character.health, character.intelligence) : "");
        advanceButton.setEnabled(exists);
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }
}
