package com.amin.keyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Locale;

public class AminKeyboardView extends LinearLayout {
    private final AminImeService service;
    private boolean english = false;
    private boolean shiftEnabled = false;

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
        setPadding(4, 8, 4, 8);
        setBackgroundColor(Color.rgb(225, 229, 235));
        render();
    }

    private void render() {
        removeAllViews();

        String[][] rows = english ? en : fa;
        for (String[] row : rows) {
            LinearLayout line = new LinearLayout(getContext());
            line.setGravity(Gravity.CENTER);
            line.setOrientation(HORIZONTAL);

            for (String key : row) {
                String displayedKey = english && shiftEnabled
                    ? key.toUpperCase(Locale.ROOT)
                    : key;
                addKey(line, displayedKey, 1, () -> service.typeText(displayedKey));
            }

            addView(line, new LayoutParams(-1, 52));
        }

        LinearLayout bottom = new LinearLayout(getContext());
        bottom.setGravity(Gravity.CENTER);

        addKey(bottom, "?123", 1, () -> {});

        // Shift is available in both layouts. Persian letters have no uppercase form,
        // so in Persian this key only exposes the shift state for visual testing.
        addKey(bottom, shiftEnabled ? "⇧" : "⇧", 1, () -> {
            shiftEnabled = !shiftEnabled;
            render();
        });

        addKey(bottom, english ? "فا" : "EN", 1, () -> {
            english = !english;
            service.setEnglish(english);
            render();
        });

        addKey(bottom, "،", 1, () -> service.typeText("،"));
        addKey(bottom, "فاصله", 4, () -> service.typeText(" "));
        addKey(bottom, "⌫", 1, () -> service.backspace());
        addKey(bottom, "↵", 1, () -> service.typeText("\n"));

        addView(bottom, new LayoutParams(-1, 54));
    }

    private void addKey(LinearLayout row, String label, int weight) {
        addKey(row, label, weight, () -> service.typeText(label));
    }

    private void addKey(LinearLayout row, String label, int weight, Runnable action) {
        Button button = new Button(getContext());
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(label.length() > 2 ? 12 : 18);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setPadding(0, 0, 0, 0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);

        int backgroundColor = "⇧".equals(label) && shiftEnabled
            ? Color.rgb(174, 205, 245)
            : Color.WHITE;
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(backgroundColor)
        );

        LayoutParams layoutParams = new LayoutParams(0, -1, weight);
        layoutParams.setMargins(2, 2, 2, 2);
        row.addView(button, layoutParams);
        button.setOnClickListener(view -> action.run());
    }
}
