package com.amin.keyboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

public class AminKeyboardView extends LinearLayout {
    private final AminImeService service;
    private boolean english = false;

    private final String[][] fa = {
            {"ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج","چ"},
            {"ش","س","ی","ب","ل","ا","ت","ن","م","ک","گ"},
            {"ظ","ط","ز","ر","ذ","د","پ","و","ژ"}
    };
    private final String[][] en = {
            {"q","w","e","r","t","y","u","i","o","p"},
            {"a","s","d","f","g","h","j","k","l"},
            {"z","x","c","v","b","n","m"}
    };

    public AminKeyboardView(Context context, AminImeService service) {
        super(context);
        this.service = service;
        setOrientation(VERTICAL);
        setPadding(dp(4), dp(8), dp(4), dp(8));
        setBackgroundColor(Color.rgb(225, 229, 235));
        render();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int keyboardHeightDp() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float screenHeightDp = metrics.heightPixels / metrics.density;
        return Math.round(Math.max(260f, Math.min(290f, screenHeightDp * 0.34f)));
    }

    private void render() {
        removeAllViews();
        String[][] rows = english ? en : fa;

        int rowHeight = dp((keyboardHeightDp() - 16) / 4f);
        for (String[] row : rows) {
            LinearLayout line = new LinearLayout(getContext());
            line.setGravity(Gravity.CENTER);
            line.setOrientation(HORIZONTAL);
            for (String key : row) addKey(line, key, 1);
            addView(line, new LayoutParams(-1, rowHeight));
        }

        LinearLayout bottom = new LinearLayout(getContext());
        bottom.setGravity(Gravity.CENTER);
        addKey(bottom, "?123", 1, () -> {});
        addKey(bottom, english ? "فا" : "EN", 1, () -> {
            english = !english;
            service.setEnglish(english);
            render();
        });
        addKey(bottom, "،", 1, () -> service.typeText("،"));
        addKey(bottom, "فاصله", 4, () -> service.typeText(" "));
        addKey(bottom, "⌫", 1, () -> service.backspace());
        addKey(bottom, "↵", 1, () -> service.typeText("\n"));
        addView(bottom, new LayoutParams(-1, rowHeight));
    }

    private void addKey(LinearLayout row, String label, int weight) {
        addKey(row, label, weight, () -> service.typeText(label));
    }

    private void addKey(LinearLayout row, String label, int weight, Runnable action) {
        Button b = new Button(getContext());
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(label.length() > 2 ? 12 : 18);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setPadding(0, 0, 0, 0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        LayoutParams lp = new LayoutParams(0, -1, weight);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(b, lp);
        b.setOnClickListener(v -> action.run());
    }
}
