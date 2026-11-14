package com.amin.keyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class AminKeyboardView extends LinearLayout {
    private final AminImeService service;
    private boolean english = false;
    private final String[][] fa = {{"ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج","چ"},{"ش","س","ی","ب","ل","ا","ت","ن","م","ک","گ"},{"ظ","ط","ز","ر","ذ","د","پ","و","ژ"}};
    private final String[][] en = {{"q","w","e","r","t","y","u","i","o","p"},{"a","s","d","f","g","h","j","k","l"},{"z","x","c","v","b","n","m"}};
    public AminKeyboardView(Context context, AminImeService service) {
        super(context); this.service = service; setOrientation(VERTICAL); setPadding(4,8,4,8); setBackgroundColor(Color.rgb(225,229,235)); render();
    }
    private void render() {
        removeAllViews();
        String[][] rows = english ? en : fa;
        for (String[] row : rows) {
            LinearLayout line = new LinearLayout(getContext()); line.setGravity(Gravity.CENTER); line.setOrientation(HORIZONTAL);
            for (String key : row) addKey(line, key, 1);
            addView(line, new LayoutParams(-1, 52));
        }
        LinearLayout bottom = new LinearLayout(getContext()); bottom.setGravity(Gravity.CENTER);
        addKey(bottom, "?123", 1, () -> {});
        addKey(bottom, english ? "فا" : "EN", 1, () -> { english = !english; service.setEnglish(english); render(); });
        addKey(bottom, "،", 1, () -> service.typeText("،"));
        addKey(bottom, "فاصله", 4, () -> service.typeText(" "));
        addKey(bottom, "⌫", 1, () -> service.backspace());
        addKey(bottom, "↵", 1, () -> service.typeText("\n"));
        addView(bottom, new LayoutParams(-1, 54));
    }
    private void addKey(LinearLayout row, String label, int weight) { addKey(row,label,weight,()->service.typeText(label)); }
    private void addKey(LinearLayout row, String label, int weight, Runnable action) {
        Button b = new Button(getContext()); b.setText(label); b.setAllCaps(false); b.setTextSize(label.length()>2?12:18); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setPadding(0,0,0,0); b.setMinWidth(0); b.setMinimumWidth(0); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        LayoutParams lp = new LayoutParams(0,-1,weight); lp.setMargins(2,2,2,2); row.addView(b,lp); b.setOnClickListener(v -> action.run());
    }
}
