package com.amin.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.text.TextUtils;

public class AminImeService extends InputMethodService {
    private boolean english = false;
    private static final String TATWEEL = "ــــ"; // exactly four U+0640 characters

    @Override public View onCreateInputView() { return new AminKeyboardView(this, this); }

    void typeText(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || TextUtils.isEmpty(text)) return;
        if (!english && text.length() == 1) {
            char current = text.charAt(0);
            CharSequence before = ic.getTextBeforeCursor(1, 0);
            if (before != null && before.length() == 1) {
                char previous = before.charAt(0);
                if (canJoinForward(previous) && canReceive(current)) ic.commitText(TATWEEL + text, 1);
                else ic.commitText(text, 1);
            } else ic.commitText(text, 1);
        } else ic.commitText(text, 1);
    }

    void backspace() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence before = ic.getTextBeforeCursor(32, 0);
        if (before == null || before.length() == 0) return;
        int remove = 1;
        int i = before.length() - 2;
        while (i >= 0 && before.charAt(i) == '\u0640') { remove++; i--; }
        ic.deleteSurroundingText(remove, 0);
    }

    void setEnglish(boolean value) { english = value; }

    private boolean canJoinForward(char c) {
        // Persian/Arabic dual-joining letters. Right-joining-only letters are deliberately excluded.
        String dual = "بتثجحخسشصضطظعغفقكلمنهيپچژگک";
        return dual.indexOf(c) >= 0;
    }
    private boolean canReceive(char c) {
        // Letters that can accept a connection from the preceding letter.
        String receive = "ابتثجحخدذرزسشصضطظعغفقکلمنوهىيپچژگک";
        return receive.indexOf(c) >= 0;
    }
}
