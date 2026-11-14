package com.amin.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;\nimport android.view.Gravity;\nimport android.widget.FrameLayout;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import android.text.TextUtils;

public class AminImeService extends InputMethodService {
    private boolean english = false;
    private static final String TATWEEL = "ــــ"; // exactly four U+0640 characters
    private char lastTypedChar = 0;
    private boolean hasLastTypedChar = false;
    private int lastKnownSelectionStart = -1;

    @Override
    public View onCreateInputView() {
        FrameLayout container = new FrameLayout(this);
        AminKeyboardView keyboard = new AminKeyboardView(this, this);
        int mode = getSharedPreferences("typo_settings", MODE_PRIVATE).getInt("one_hand", 0);
        int width = mode == 0 ? FrameLayout.LayoutParams.MATCH_PARENT
                : Math.round(getResources().getDisplayMetrics().widthPixels * 0.78f);
        int gravity = mode == 1 ? Gravity.BOTTOM | Gravity.LEFT
                : (mode == 2 ? Gravity.BOTTOM | Gravity.RIGHT : Gravity.BOTTOM);
        container.addView(keyboard, new FrameLayout.LayoutParams(
                width, FrameLayout.LayoutParams.WRAP_CONTENT, gravity));
        return container;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        clearTypingCache();
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        clearTypingCache();
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        if (newSelStart != newSelEnd || newSelStart == 0) {
            clearTypingCache();
            lastKnownSelectionStart = newSelStart;
            return;
        }
        InputConnection ic = getCurrentInputConnection();
        CharSequence before = ic == null ? null : ic.getTextBeforeCursor(1, 0);
        if (before != null && before.length() > 0) {
            rememberChar(before.charAt(before.length() - 1));
        } else if (lastKnownSelectionStart >= 0 && newSelStart != lastKnownSelectionStart) {
            clearTypingCache();
        }
        lastKnownSelectionStart = newSelStart;
    }

    void typeText(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || TextUtils.isEmpty(text)) return;
        if (!english && text.length() == 1 && canReceive(text.charAt(0))) {
            char current = text.charAt(0);
            CharSequence before = ic.getTextBeforeCursor(1, 0);
            char previous = 0;
            boolean hasPrevious = false;
            if (before != null && before.length() > 0) {
                previous = before.charAt(before.length() - 1);
                hasPrevious = true;
            } else if (hasLastTypedChar) {
                previous = lastTypedChar;
                hasPrevious = true;
            }
            String committed = hasPrevious && canJoinForward(previous)
                    ? TATWEEL + text : text;
            ic.commitText(committed, 1);
            rememberChar(current);
            if (lastKnownSelectionStart >= 0) lastKnownSelectionStart += committed.length();
        } else {
            typeTextRaw(text);
        }
    }

    // Used for punctuation, spaces, emoji and diacritics so they never trigger auto-tatweel.
    void typeTextRaw(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || TextUtils.isEmpty(text)) return;
        ic.commitText(text, 1);
        if (text.length() == 1 && isPersianLetter(text.charAt(0))) rememberChar(text.charAt(0));
        else clearTypingCache();
        if (lastKnownSelectionStart >= 0) lastKnownSelectionStart += text.length();
    }

    void backspace() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence before = ic.getTextBeforeCursor(32, 0);
        if (before == null || before.length() == 0) {
            if (lastKnownSelectionStart == 0) clearTypingCache();
            return;
        }
        int remove = 1;
        int i = before.length() - 2;
        while (i >= 0 && before.charAt(i) == '\u0640') { remove++; i--; }
        ic.deleteSurroundingText(remove, 0);
        CharSequence remaining = ic.getTextBeforeCursor(1, 0);
        if (remaining != null && remaining.length() > 0) rememberChar(remaining.charAt(remaining.length() - 1));
        else if (lastKnownSelectionStart <= remove) clearTypingCache();
        if (lastKnownSelectionStart >= 0) lastKnownSelectionStart = Math.max(0, lastKnownSelectionStart - remove);
    }

    void setEnglish(boolean value) {
        english = value;
        clearTypingCache();
    }

    private void rememberChar(char c) { lastTypedChar = c; hasLastTypedChar = true; }
    private void clearTypingCache() { lastTypedChar = 0; hasLastTypedChar = false; }

    private boolean isPersianLetter(char c) {
        return canJoinForward(c) || canReceive(c);
    }

    private boolean canJoinForward(char c) {
        String dual = "بتثجحخسشصضطظعغفقکلمنهيپچژگک\u06cc";
        return dual.indexOf(c) >= 0;
    }

    private boolean canReceive(char c) {
        String receive = "ابتثجحخدذرزسشصضطظعغفقکلمنوهىيپچژگک\u06cc";
        return receive.indexOf(c) >= 0;
    }
}
