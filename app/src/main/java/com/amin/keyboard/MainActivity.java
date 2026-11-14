package com.amin.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("typo_settings", MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        TextView title = new TextView(this);
        title.setText("تــــایــــپــــو\\n\\nتنظیمات کیبورد");
        title.setTextSize(24);
        root.addView(title);

        status = new TextView(this);
        status.setTextSize(16);
        status.setPadding(0, 24, 0, 24);
        root.addView(status);

        addButton(root, "تم بعدی", v -> {
            int next = (prefs.getInt("theme", 0) + 1) % 3;
            prefs.edit().putInt("theme", next).apply();
            refreshStatus();
        });
        addButton(root, "ردیف اعداد: روشن / خاموش", v -> {
            prefs.edit().putBoolean("number_row", !prefs.getBoolean("number_row", true)).apply();
            refreshStatus();
        });
        addButton(root, "حالت یک‌دستی: تمام‌عرض / چپ / راست", v -> {
            int next = (prefs.getInt("one_hand", 0) + 1) % 3;
            prefs.edit().putInt("one_hand", next).apply();
            refreshStatus();
        });
        addButton(root, "فعال‌سازی کیبورد", v -> startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS")));
        addButton(root, "انتخاب کیبورد تایپو", v -> ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker());
        TextView note = new TextView(this);
        note.setText("برای اعمال تنظیمات جدید، کیبورد را یک‌بار ببند و دوباره باز کن.");
        note.setPadding(0, 24, 0, 0);
        root.addView(note);
        setContentView(root);
        refreshStatus();
    }

    private void addButton(LinearLayout root, String label, android.view.View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(listener);
        root.addView(b);
    }

    private void refreshStatus() {
        String[] themes = {"بنفش و یاسی", "آبی روشن", "سبز نعنایی"};
        int theme = prefs.getInt("theme", 0);
        String[] modes = {"تمام‌عرض", "یک‌دستی چپ", "یک‌دستی راست"};
        int mode = prefs.getInt("one_hand", 0);
        status.setText("تم: " + themes[theme] +
                "\\nردیف اعداد: " + (prefs.getBoolean("number_row", true) ? "روشن" : "خاموش") +
                "\\nحالت: " + modes[mode]);
    }
}
