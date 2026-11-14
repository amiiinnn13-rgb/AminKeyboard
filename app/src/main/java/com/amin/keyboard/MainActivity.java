package com.amin.keyboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        TextView title = new TextView(this);
        title.setText("Amin Keyboard\n\nکیبورد فارسی با کشیده‌گذاری خودکار چهارگانه");
        title.setTextSize(21);
        root.addView(title);
        Button settings = new Button(this);
        settings.setText("فعال‌سازی کیبورد");
        settings.setOnClickListener(v -> startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS")));
        root.addView(settings);
        Button choose = new Button(this);
        choose.setText("انتخاب کیبورد Amin");
        choose.setOnClickListener(v -> ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker());
        root.addView(choose);
        setContentView(root);
    }
}
