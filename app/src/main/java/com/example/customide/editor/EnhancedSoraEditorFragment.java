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
// مسیر جدید JavaLanguage در نسخه 0.23.0
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
// از کلاس سفارشی MonokaiScheme برای تم استفاده می‌کنیم
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

        // تنظیم زبان به جاوا (مسیر به‌روز شده برای نسخه 0.23.0)
        soraEditor.setEditorLanguage(new JavaLanguage());

        // تنظیم تم از طریق کلاس سفارشی MonokaiScheme
        soraEditor.setColorScheme(new SchemeGitHub());

        // بارگذاری محتوا از فایل در صورت ارائه مسیر
        String filePath = (getArguments() != null) ? getArguments().getString(ARG_FILE_PATH) : null;
        if (filePath != null && !filePath.isEmpty()) {
            String content = readFile(filePath);
            soraEditor.setText(content);
        }

        // استفاده از polling با Handler برای نظارت بر تغییرات متن هر 500 میلی‌ثانیه
        startPolling();

        return view;
    }

    private void startPolling(){
        pollRunnable = new Runnable(){
            @Override
            public void run() {
                String currentText = soraEditor.getText().toString();
                if (!currentText.equals(lastText)){
                    lastText = currentText;
                    advancedCheckErrors();
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(pollRunnable, 500);
    }

    // بررسی ساده زنده خطا: اگر یک خط (غیرخالی و غیرکامنت) به ";"، "{" یا "}" ختم نشود،
    // در لاگ ثبت می‌شود.
    private void advancedCheckErrors(){
        String text = soraEditor.getText().toString();
        String[] lines = text.split("\n");
        for(String line: lines){
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("//")
                && !(trimmed.endsWith(";") || trimmed.endsWith("{") || trimmed.endsWith("}"))) {
                Log.e("SoraEditorError", "خطا در پایان خط: " + line);
            }
        }
    }

    private String readFile(String filePath){
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
    public void onDestroyView(){
        if (pollRunnable != null){
            handler.removeCallbacks(pollRunnable);
        }
        super.onDestroyView();
    }
}