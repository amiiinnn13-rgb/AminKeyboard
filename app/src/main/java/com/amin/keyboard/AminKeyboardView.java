package com.amin.keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.app.AlertDialog;
import android.widget.Toast;
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
    private final Handler deleteHandler = new Handler(Looper.getMainLooper());
    private boolean deletePressed = false;
    private final Runnable repeatDelete = new Runnable() {
        @Override public void run() {
            if (!deletePressed) return;
            service.backspace();
            deleteHandler.postDelayed(this, 45);
        }
    };

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
        return Math.round(Math.max(300f, Math.min(330f, h * 0.39f)));
    }

    private void render() {
        reloadPrefs();
        removeAllViews();
        setBackgroundColor(backgrounds[theme]);
        int rows = (page == 0 && !numberRow) ? 5 : 6;
        int rowHeight = dp((keyboardHeightDp() - 80) / (float) rows);
        addToolbar();

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

    private void addToolbar() {
        HorizontalScrollView scroller = new HorizontalScrollView(getContext());
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout tools = new LinearLayout(getContext());
        tools.setOrientation(HORIZONTAL);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        tools.setPadding(dp(2), 0, dp(2), 0);

        addToolButton(tools, "📋", this::showClipboardHistory);
        addToolButton(tools, "چسباندن", () -> pasteClipboard());
        addToolButton(tools, "انتخاب همه", () -> performEditorAction(android.R.id.selectAll));
        addToolButton(tools, "برش", () -> performEditorAction(android.R.id.cut));
        addToolButton(tools, "کپی", () -> performEditorAction(android.R.id.copy));
        addToolButton(tools, "↔", () -> {
            english = !english;
            service.setEnglish(english);
            shifted = false;
            page = 0;
            render();
        });
        scroller.addView(tools, new HorizontalScrollView.LayoutParams(-2, -1));
        addView(scroller, new LayoutParams(-1, dp(40)));
    }

    private void addToolButton(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(getContext());
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(label.length() > 3 ? 11 : 16);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(34));
        lp.setMargins(dp(2), 0, dp(2), 0);
        parent.addView(button, lp);
        button.setOnClickListener(v -> action.run());
    }

    private void performEditorAction(int actionId) {
        android.view.inputmethod.InputConnection ic = service.getCurrentInputConnection();
        if (ic != null) ic.performContextMenuAction(actionId);
    }

    private void pasteClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
            CharSequence text = clipboard.getPrimaryClip().getItemAt(0).coerceToText(getContext());
            if (text != null) service.typeTextRaw(text.toString());
        }
    }

    private void showClipboardHistory() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        java.util.LinkedHashSet<String> history = new java.util.LinkedHashSet<>(
                prefs.getStringSet("clipboard_history", new java.util.LinkedHashSet<>()));
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
            CharSequence current = clipboard.getPrimaryClip().getItemAt(0).coerceToText(getContext());
            if (current != null && current.length() > 0) {
                history.add(current.toString());
                while (history.size() > 20) history.remove(history.iterator().next());
                prefs.edit().putStringSet("clipboard_history", new java.util.HashSet<>(history)).apply();
            }
        }
        if (history.isEmpty()) {
            Toast.makeText(getContext(), "هنوز متنی در حافظهٔ کلیپ‌بورد ذخیره نشده است.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] entries = history.toArray(new String[0]);
        // Most recently added item appears first.
        for (int i = 0, j = entries.length - 1; i < j; i++, j--) {
            String temp = entries[i]; entries[i] = entries[j]; entries[j] = temp;
        }
        new AlertDialog.Builder(getContext())
                .setTitle("حافظهٔ کلیپ‌بورد")
                .setItems(entries, (dialog, which) -> service.typeTextRaw(entries[which]))
                .setNegativeButton("بستن", null)
                .show();
    }

    private void addDiacriticsKey(LinearLayout row) {
        Button dot = new Button(getContext());
        dot.setText(".");
        dot.setAllCaps(false);
        dot.setTextSize(21);
        dot.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        dot.setPadding(0, 0, 0, 0);
        dot.setMinWidth(0);
        dot.setMinimumWidth(0);
        dot.setBackgroundTintList(ColorStateList.valueOf(keys[theme]));
        LayoutParams lp = new LayoutParams(0, -1, 8);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(dot, lp);

        final boolean[] opened = {false};
        final Runnable showPanel = () -> {
            if (!dot.isPressed()) return;
            opened[0] = true;
            showDiacriticsPanel();
        };
        dot.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    opened[0] = false;
                    deleteHandler.removeCallbacks(showPanel);
                    deleteHandler.postDelayed(showPanel, 2000);
                    return true;
                case MotionEvent.ACTION_UP:
                    deleteHandler.removeCallbacks(showPanel);
                    if (!opened[0]) service.typeTextRaw(".");
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    deleteHandler.removeCallbacks(showPanel);
                    return true;
                default:
                    return true;
            }
        });
    }

    private void showDiacriticsPanel() {
        String[] marks = {"َ","ِ","ُ","ّ","ْ","ً","ٍ","ٌ","ٰ","ٖ","ٗ","ٓ","ـ"};
        new AlertDialog.Builder(getContext())
                .setTitle("اعراب و نشانه‌ها")
                .setItems(marks, (dialog, which) -> service.typeTextRaw(marks[which]))
                .setNegativeButton("بستن", null)
                .show();
    }

    @Override protected void onDetachedFromWindow() {
        deletePressed = false;
        deleteHandler.removeCallbacks(repeatDelete);
        super.onDetachedFromWindow();
    }

    private void addKey(LinearLayout row, String label, int weight, Runnable action, boolean accent) {
        Button b = new Button(getContext());
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(label.length() > 2 ? 13 : 21);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setPadding(0, 0, 0, 0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setBackgroundTintList(ColorStateList.valueOf(accent ? accents[theme] : keys[theme]));
        LayoutParams lp = new LayoutParams(0, -1, weight);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(b, lp);
        if ("⌫".equals(label)) {
            // A tap deletes one character; holding the key repeats until the finger is lifted.
            b.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        deletePressed = true;
                        service.backspace();
                        deleteHandler.removeCallbacks(repeatDelete);
                        deleteHandler.postDelayed(repeatDelete, 320);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        deletePressed = false;
                        deleteHandler.removeCallbacks(repeatDelete);
                        v.performClick();
                        return true;
                    default:
                        return true;
                }
            });
        } else {
            b.setOnClickListener(v -> action.run());
        }
    }
}
