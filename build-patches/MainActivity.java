package com.amin.keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("typo_settings", MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(24));

        TextView title = new TextView(this);
        title.setText("تــــایــــپــــو\nتنظیمات کیبورد");
        title.setTextSize(24);
        root.addView(title);

        status = new TextView(this);
        status.setTextSize(16);
        status.setPadding(0, dp(18), 0, dp(18));
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
        addButton(root, "ارتفاع کیبورد: کوتاه / معمولی / بلند", v -> {
            int next = (prefs.getInt("keyboard_height", 1) + 1) % 3;
            prefs.edit().putInt("keyboard_height", next).apply();
            refreshStatus();
        });
        addButton(root, "اندازه نوشته کلیدها", v -> {
            float current = prefs.getFloat("key_scale", 1.3f);
            float next = current >= 1.29f ? 1.0f : (current >= 1.14f ? 1.3f : 1.15f);
            prefs.edit().putFloat("key_scale", next).apply();
            refreshStatus();
        });
        addButton(root, "مدیریت میانبرهای متن", v -> manageShortcuts());
        addButton(root, "فعال‌سازی کیبورد", v -> startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS")));
        addButton(root, "انتخاب کیبورد تایپو", v -> ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker());

        TextView note = new TextView(this);
        note.setText("برای اعمال تنظیمات جدید، کیبورد را یک‌بار ببند و دوباره باز کن.");
        note.setPadding(0, dp(20), 0, 0);
        root.addView(note);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);
        setContentView(scroll);
        refreshStatus();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void addButton(LinearLayout root, String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(listener);
        root.addView(b);
    }

    private void manageShortcuts() {
        Set<String> saved = new HashSet<>(prefs.getStringSet("text_shortcuts", new HashSet<>()));
        if (saved.isEmpty()) {
            showAddShortcutDialog();
            return;
        }
        String[] entries = saved.toArray(new String[0]);
        String[] labels = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            int split = entries[i].indexOf('\t');
            labels[i] = split > 0 ? entries[i].substring(0, split) + "  ←  " + entries[i].substring(split + 1) : entries[i];
        }
        new AlertDialog.Builder(this)
                .setTitle("میانبرها؛ برای حذف روی مورد بزن")
                .setItems(labels, (dialog, which) -> {
                    saved.remove(entries[which]);
                    prefs.edit().putStringSet("text_shortcuts", saved).apply();
                    refreshStatus();
                    Toast.makeText(this, "میانبر حذف شد.", Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("افزودن میانبر", (dialog, which) -> showAddShortcutDialog())
                .setNegativeButton("بستن", null)
                .show();
    }

    private void showAddShortcutDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);
        EditText trigger = new EditText(this);
        trigger.setSingleLine(true);
        trigger.setHint("عبارت کوتاه؛ مثلاً omw");
        trigger.setInputType(InputType.TYPE_CLASS_TEXT);
        form.addView(trigger);
        EditText expansion = new EditText(this);
        expansion.setHint("متن کامل جایگزین");
        expansion.setMinLines(2);
        expansion.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        form.addView(expansion);

        new AlertDialog.Builder(this)
                .setTitle("ساخت میانبر جدید")
                .setView(form)
                .setPositiveButton("ذخیره", (dialog, which) -> {
                    String key = trigger.getText().toString().trim();
                    String value = expansion.getText().toString();
                    if (key.isEmpty() || value.trim().isEmpty() || key.matches(".*\\s+.*")) {
                        Toast.makeText(this, "عبارت کوتاه بدون فاصله و متن جایگزین را وارد کن.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Set<String> all = new HashSet<>(prefs.getStringSet("text_shortcuts", new HashSet<>()));
                    all.add(key + '\t' + value);
                    prefs.edit().putStringSet("text_shortcuts", all).apply();
                    refreshStatus();
                    Toast.makeText(this, "میانبر ذخیره شد؛ با زدن فاصله جایگزین می‌شود.", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void refreshStatus() {
        String[] themes = {"بنفش و یاسی", "آبی روشن", "سبز نعنایی"};
        int theme = Math.max(0, Math.min(2, prefs.getInt("theme", 0)));
        String[] modes = {"تمام‌عرض", "یک‌دستی چپ", "یک‌دستی راست"};
        int mode = Math.max(0, Math.min(2, prefs.getInt("one_hand", 0)));
        float scale = prefs.getFloat("key_scale", 1.3f);
        String[] heights = {"کوتاه", "معمولی", "بلند"};
        int height = Math.max(0, Math.min(2, prefs.getInt("keyboard_height", 1)));
        int shortcuts = prefs.getStringSet("text_shortcuts", new HashSet<>()).size();
        status.setText("تم: " + themes[theme] +
                "\nردیف اعداد: " + (prefs.getBoolean("number_row", true) ? "روشن" : "خاموش") +
                "\nحالت: " + modes[mode] +
                "\nارتفاع کیبورد: " + heights[height] +
                "\nاندازه نوشته کلیدها: " + Math.round(scale * 100) + "%" +
                "\nمیانبرهای ذخیره‌شده: " + shortcuts);
    }
}