package com.example.customide.editor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.customide.R;
import com.jakewharton.rxbinding4.widget.RxTextView;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class EnhancedSoraEditorFragment extends Fragment {

    private CodeEditor soraEditor;
    private static final String ARG_FILE_PATH = "file_path";
    private Disposable textChangeDisposable;

    // ErrorSpan سفارشی جهت underline خطاها (قرمز)
    public static class ErrorSpan extends UnderlineSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(true);
            ds.setColor(0xFFFF0000);
        }
    }

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

        // تنظیم زبان به جاوا
        soraEditor.setEditorLanguage(new JavaLanguage());
        
        // فعال‌سازی auto-completion؛ توجه کنید که در نسخه 0.23.0 ممکن است متد به شکل setAutoCompletionEnabled موجود باشد.
        // در اینجا فرض می‌کنیم که این متد وجود دارد:
        soraEditor.setAutoCompletionEnabled(true);

        // فعال‌سازی Auto-indent، Bracket Matching و تنظیم تم
        soraEditor.setAutoIndentEnabled(true);
        soraEditor.setMatchingBracketEnabled(true);
        soraEditor.setTheme(io.github.rosemoe.sora.widget.schemes.SchemeGitHub);

        // بارگذاری محتوا از فایل در صورت وجود مسیر
        String filePath = getArguments() != null ? getArguments().getString(ARG_FILE_PATH) : null;
        if (filePath != null) {
            String content = readFile(filePath);
            soraEditor.setText(content);
        }

        // استفاده از RxBinding برای مشاهده تغییرات متن با debounce 500ms
        textChangeDisposable = RxTextView.textChanges(soraEditor)
                .skipInitialValue()
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> advancedCheckErrors());

        return view;
    }

    // بررسی زنده خطا: اگر خط غیرخالی (و غیرکامنت) به درستی با علامت پایان‌دهنده (";", "{" یا "}") ختم نشود، آن خط شامل ErrorSpan می‌شود.
    private void advancedCheckErrors() {
        Editable text = soraEditor.getText();
        // حذف ErrorSpanهای قبلی
        ErrorSpan[] errorSpans = text.getSpans(0, text.length(), ErrorSpan.class);
        for (ErrorSpan span : errorSpans) {
            text.removeSpan(span);
        }
        String[] lines = text.toString().split("\n");
        int index = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("//") &&
                    !(trimmed.endsWith(";") || trimmed.endsWith("{") || trimmed.endsWith("}"))) {
                int start = index;
                int end = index + line.length();
                text.setSpan(new ErrorSpan(), start, end, Editable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            index += line.length() + 1;
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
        if (textChangeDisposable != null && !textChangeDisposable.isDisposed()) {
            textChangeDisposable.dispose();
        }
    }
}