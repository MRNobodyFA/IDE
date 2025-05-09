package com.example.customide.editor;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.customide.R;

// استفاده از مسیر به‌روز شده JavaLanguage در نسخه 0.23.0
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
// تنظیم تم با استفاده از SchemeGitHub؛ در صورتی که در نسخه شما موجود است.
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class EnhancedSoraEditorFragment extends Fragment {

    private CodeEditor soraEditor;
    private static final String ARG_FILE_PATH = "file_path";
    private Handler handler = new Handler();
    private Runnable pollRunnable;
    private String lastText = "";

    public static EnhancedSoraEditorFragment newInstance(String filePath) {
        EnhancedSoraEditorFragment fragment = new EnhancedSoraEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sora_editor, container, false);
        soraEditor = view.findViewById(R.id.soraEditor);

        // بررسی وجود view مربوط به CodeEditor
        if (soraEditor == null) {
            throw new NullPointerException("soraEditor is null! لطفاً فایل fragment_sora_editor.xml را بررسی کنید.");
        }

        // تنظیم زبان به جاوا با استفاده از مسیر نوین
        try {
            soraEditor.setEditorLanguage(new JavaLanguage());
        } catch (Exception e) {
            Log.e("EnhancedSoraEditor", "Error setting editor language: " + e.getMessage());
        }

        // تنظیم تم از طریق SchemeGitHub
        try {
            soraEditor.setColorScheme(new SchemeGitHub());
        } catch (Exception e) {
            Log.e("EnhancedSoraEditor", "Error setting color scheme: " + e.getMessage());
        }

        // بارگذاری محتوا از فایل در صورت ارائه مسیر
        String filePath = (getArguments() != null) ? getArguments().getString(ARG_FILE_PATH) : null;
        if (filePath != null && !filePath.isEmpty()) {
            String content = readFile(filePath);
            soraEditor.setText(content);
        }

        // شروع نظارت (polling) بر تغییرات متن هر 500 میلی‌ثانیه
        startPolling();

        return view;
    }

    private void startPolling() {
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String currentText = soraEditor.getText().toString();
                    if (!currentText.equals(lastText)) {
                        lastText = currentText;
                        advancedCheckErrors();
                    }
                } catch (Exception e) {
                    Log.e("EnhancedSoraEditor", "Polling error: " + e.getMessage());
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(pollRunnable, 500);
    }

    // بررسی ساده خطا: اگر خطی غیرخالی (و غیرکامنت) به ";"، "{" یا "}" ختم نشود، در Log گزارش می‌شود.
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
            Log.e("EnhancedSoraEditor", "Error reading file: " + e.getMessage());
        }
        return builder.toString();
    }

    @Override
    public void onDestroyView() {
        if (pollRunnable != null) {
            handler.removeCallbacks(pollRunnable);
        }
        super.onDestroyView();
    }
}