package com.example.customide.editor;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.customide.R;
// اصلاح مسیر JavaLanguage در نسخه 0.23.0
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
// استفاده از MonokaiScheme از پکیج جدید
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EnhancedSoraEditorFragment extends Fragment {

    private CodeEditor soraEditor;
    private static final String ARG_FILE_PATH = "file_path";
    private Handler handler = new Handler();
    private Runnable debounceRunnable;

    public static EnhancedSoraEditorFragment newInstance(String filePath) {
        EnhancedSoraEditorFragment fragment = new EnhancedSoraEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sora_editor, container, false);
        soraEditor = view.findViewById(R.id.soraEditor);

        // تنظیم زبان به جاوا (مسیر پکیج جدید)
        soraEditor.setEditorLanguage(new JavaLanguage());

        // حذف فراخوانی متد setAutoCompletionEnabled (که در این نسخه وجود ندارد)
        // تنظیم auto-indent و bracket matching از طریق پروپس
        soraEditor.getProps().setAutoIndent(true);
        soraEditor.getProps().setBracketMatching(true);

        // تنظیم تم: استفاده از MonokaiScheme موجود در پکیج schemes
        soraEditor.setColorScheme(new SchemeGitHub());

        // بارگذاری محتوا از فایل در صورت وجود مسیر
        String filePath = getArguments() != null ? getArguments().getString(ARG_FILE_PATH) : null;
        if (filePath != null) {
            String content = readFile(filePath);
            soraEditor.setText(content);
        }

        // استفاده از TextWatcher برای دریافت تغییرات متن به همراه debouncing (500ms)
        soraEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // بدون عمل
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // بدون عمل
            }

            @Override
            public void afterTextChanged(Editable s) {
                // اجرای debounce: حذف callback قبلی و ارسال Runnable جدید
                if (debounceRunnable != null) {
                    handler.removeCallbacks(debounceRunnable);
                }
                debounceRunnable = new Runnable() {
                    @Override
                    public void run() {
                        advancedCheckErrors();
                    }
                };
                handler.postDelayed(debounceRunnable, 500);
            }
        });

        return view;
    }

    // روش ساده بررسی زنده خطا: در این مثال، اگر یک خط غیرخالی (و غیرکامنت)
    // به درستی (با ";"، "{" یا "}") خاتمه نیابد، خطا در لاگ گزارش می‌شود.
    private void advancedCheckErrors() {
        String text = soraEditor.getText().toString();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("//")
                && !(trimmed.endsWith(";") || trimmed.endsWith("{") || trimmed.endsWith("}"))) {
                Log.e("SoraEditorError", "خطا در پایان خط: " + line);
            }
        }
    }

    private String readFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return "";
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                builder.append(ln).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (debounceRunnable != null) {
            handler.removeCallbacks(debounceRunnable);
        }
    }
}