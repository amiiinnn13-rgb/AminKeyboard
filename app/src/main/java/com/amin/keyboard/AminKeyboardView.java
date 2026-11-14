package com.amin.keyboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

public class AminKeyboardView extends LinearLayout {
    private final AminImeService service;
    private boolean english = false;
    private boolean shifted = false;
    private boolean capsLock = false;
    private int page = 0; // 0 letters, 1 symbols, 2 emoji

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
            {"*","\"","'",";",":","!","؟","،",".","="}
    };
    private final String[][] emojis = {
            {"😀","😃","😄","😁","😆","😅","😂","🤣"},
            {"😊","😍","🥰","😘","😎","🤔","😭","😡"},
            {"👍","👎","👏","🙏","❤️","💕","🔥","✨"},
            {"🌹","🌷","🌸","🌺","🌻","🌈","⭐","🎉"}
    };
    private final String[] diacritics = {"َ","ِ","ُ","ّ","ْ","ً","ٍ","ٌ","ٰ","ـ"};

    public AminKeyboardView(Context context, AminImeService service) {
        super(context);
        this.service = service;
        setOrientation(VERTICAL);
        // Reduced bottom inset by about 1.5 cm to bring the keyboard closer to the screen edge.
        setPadding(dp(4), dp(8), dp(4), dp(36));
        setBackgroundColor(Color.rgb(225, 229, 235));
        render();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int keyboardHeightDp() {
        float screenHeightDp = getResources().getDisplayMetrics().heightPixels
                / getResources().getDisplayMetrics().density;
        return Math.round(Math.max(260f, Math.min(290f, screenHeightDp * 0.34f)));
    }

    private void render() {
        removeAllViews();
        int rowHeight = dp((keyboardHeightDp() - 36) / 4f);

        if (page == 2) {
            for (String[] row : emojis) {
                LinearLayout line = newRow();
                for (String emoji : row) addKey(line, emoji, 1, () -> service.typeTextRaw(emoji));
                addView(line, new LayoutParams(-1, rowHeight));
            }
            addBottomRow(rowHeight);
            return;
        }

        if (page == 1) {
            for (String[] row : symbols) {
                LinearLayout line = newRow();
                for (String key : row) addKey(line, key, 1, () -> service.typeTextRaw(key));
                addView(line, new LayoutParams(-1, rowHeight));
            }
            addBottomRow(rowHeight);
            return;
        }

        String[][] rows = english ? en : fa;
        for (int i = 0; i < rows.length; i++) {
            LinearLayout line = newRow();
            if (i == 2) {
                addKey(line, "⇧", 13, () -> {
                    shifted = !shifted;
                    if (!shifted) capsLock = false;
                    render();
                }, () -> {
                    if (english) {
                        capsLock = !capsLock;
                        shifted = capsLock;
                    } else {
                        shifted = !shifted;
                    }
                    render();
                });
            }

            if (i == 2 && !english && shifted) {
                for (String mark : diacritics) {
                    addKey(line, mark, 10, () -> service.typeTextRaw(mark));
                }
            } else {
                for (String original : rows[i]) {
                    String key = english && shifted ? original.toUpperCase() : original;
                    addKey(line, key, 10, () -> {
                        service.typeText(key);
                        if (english && shifted && !capsLock) {
                            shifted = false;
                            render();
                        }
                    });
                }
            }
            if (i == 2) addKey(line, "⌫", 13, () -> service.backspace());
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
        addKey(bottom, page == 1 ? "ABC" : "?123", 12, () -> {
            page = page == 1 ? 0 : 1;
            shifted = false;
            render();
        });
        addKey(bottom, "☺", 10, () -> {
            page = page == 2 ? 0 : 2;
            render();
        });
        addKey(bottom, english ? "فا" : "EN", 10, () -> {
            english = !english;
            service.setEnglish(english);
            shifted = false;
            capsLock = false;
            page = 0;
            render();
        });
        addKey(bottom, "،", 8, () -> service.typeTextRaw("،"));
        addKey(bottom, "فاصله", 36, () -> service.typeTextRaw(" "));
        addKey(bottom, "⌫", 10, () -> service.backspace());
        addKey(bottom, "↵", 10, () -> service.typeTextRaw("\n"));
        addView(bottom, new LayoutParams(-1, rowHeight));
    }

    private void addKey(LinearLayout row, String label, int weight, Runnable action) {
        addKey(row, label, weight, action, null);
    }

    private void addKey(LinearLayout row, String label, int weight, Runnable action, Runnable longAction) {
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
        if (longAction != null) b.setOnLongClickListener(v -> { longAction.run(); return true; });
    }
}
