package com.amin.keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

public class AminKeyboardView extends LinearLayout {
    private final AminImeService service;
    private final SharedPreferences prefs;
    private boolean english = false;
    private boolean shifted = false;
    private boolean capsLock = false;
    private int page = 0;
    private int theme;
    private boolean numberRow;

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
    private final String[][] symbols = {
            {"۱","۲","۳","۴","۵","۶","۷","۸","۹","۰"},
            {"1","2","3","4","5","6","7","8","9","0"},
            {"@","#","$","%","&","-","+","(",")","/"},
            {"*","\\","'",";",":","!","؟","،",".","="}
    };
    private final String[][] emojis = {
            {"😀","😃","😄","😁","😆","😅","😂","🤣"},
            {"😊","😍","🥰","😘","😎","🤔","😭","😡"},
            {"👍","👎","👏","🙏","❤️","💕","🔥","✨"},
            {"🌹","🌷","🌸","🌺","🌻","🌈","⭐","🎉"}
    };
    private final String[] diacritics = {"َ","ِ","ُ","ّ","ْ","ً","ٍ","ٌ","ٰ","ـ"};
    private final int[] backgrounds = {Color.rgb(237, 232, 250), Color.rgb(235, 246, 255), Color.rgb(239, 248, 241)};
    private final int[] keys = {Color.WHITE, Color.WHITE, Color.WHITE};
    private final int[] accents = {Color.rgb(213, 195, 245), Color.rgb(190, 222, 250), Color.rgb(194, 231, 204)};

    public AminKeyboardView(Context context, AminImeService service) {
        super(context);
        this.service = service;
        prefs = context.getSharedPreferences("typo_settings", Context.MODE_PRIVATE);
        setOrientation(VERTICAL);
        setPadding(dp(4), dp(8), dp(4), dp(36));
        reloadPrefs();
        render();
    }

    private void reloadPrefs() {
        theme = Math.max(0, Math.min(2, prefs.getInt("theme", 0)));
        numberRow = prefs.getBoolean("number_row", true);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int keyboardHeightDp() {
        float h = getResources().getDisplayMetrics().heightPixels / getResources().getDisplayMetrics().density;
        return Math.round(Math.max(260f, Math.min(290f, h * 0.34f)));
    }

    private void render() {
        reloadPrefs();
        removeAllViews();
        setBackgroundColor(backgrounds[theme]);
        int rows = (page == 0 && !numberRow) ? 4 : 5;
        int rowHeight = dp((keyboardHeightDp() - 40) / (float) rows);

        if (page == 2 || page == 1) {
            String[][] data = page == 2 ? emojis : symbols;
            for (String[] row : data) {
                LinearLayout line = newRow();
                for (String key : row) addKey(line, key, 1, () -> service.typeTextRaw(key), false);
                addView(line, new LayoutParams(-1, rowHeight));
            }
            addBottomRow(rowHeight);
            return;
        }

        if (numberRow) {
            LinearLayout numbers = newRow();
            String[] digits = {"۱","۲","۳","۴","۵","۶","۷","۸","۹","۰"};
            for (String digit : digits) addKey(numbers, digit, 1, () -> service.typeTextRaw(digit), false);
            addView(numbers, new LayoutParams(-1, rowHeight));
        }

        String[][] rowsData = english ? en : fa;
        for (int i = 0; i < rowsData.length; i++) {
            LinearLayout line = newRow();
            if (i == 2) {
                addKey(line, "⇧", 13, () -> {
                    shifted = !shifted;
                    if (!shifted) capsLock = false;
                    render();
                }, true);
            }
            if (i == 2 && !english && shifted) {
                for (String mark : diacritics) addKey(line, mark, 10, () -> service.typeTextRaw(mark), false);
            } else {
                for (String original : rowsData[i]) {
                    String key = english && shifted ? original.toUpperCase() : original;
                    addKey(line, key, 10, () -> {
                        service.typeText(key);
                        if (english && shifted && !capsLock) { shifted = false; render(); }
                    }, false);
                }
            }
            if (i == 2) addKey(line, "⌫", 13, service::backspace, true);
            addView(line, new LayoutParams(-1, rowHeight));
        }
        addBottomRow(rowHeight);
    }

    private LinearLayout newRow() {
        LinearLayout line = new LinearLayout(getContext());
        line.setGravity(Gravity.CENTER);
        line.setOrientation(HORIZONTAL);
        return line;
    }

    private void addBottomRow(int rowHeight) {
        LinearLayout bottom = newRow();
        addKey(bottom, page == 1 ? "ABC" : "?123", 12, () -> { page = page == 1 ? 0 : 1; shifted = false; render(); }, true);
        addKey(bottom, "☺", 10, () -> { page = page == 2 ? 0 : 2; render(); }, true);
        addKey(bottom, english ? "فا" : "EN", 10, () -> {
            english = !english; service.setEnglish(english); shifted = false; capsLock = false; page = 0; render();
        }, true);
        addKey(bottom, "،", 8, () -> service.typeTextRaw("،"), false);
        addKey(bottom, "فاصله", 36, () -> service.typeTextRaw(" "), false);
        addKey(bottom, "⌫", 10, service::backspace, true);
        addKey(bottom, "↵", 10, () -> service.typeTextRaw("\n"), true);
        addView(bottom, new LayoutParams(-1, rowHeight));
    }

    private void addKey(LinearLayout row, String label, int weight, Runnable action, boolean accent) {
        Button b = new Button(getContext());
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(label.length() > 2 ? 12 : 18);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setPadding(0, 0, 0, 0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setBackgroundTintList(ColorStateList.valueOf(accent ? accents[theme] : keys[theme]));
        LayoutParams lp = new LayoutParams(0, -1, weight);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(b, lp);
        b.setOnClickListener(v -> action.run());
    }
}
