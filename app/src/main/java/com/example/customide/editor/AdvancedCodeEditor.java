package com.example.customide.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import androidx.appcompat.widget.AppCompatEditText;
import java.util.HashMap;
import java.util.Map;

public class AdvancedCodeEditor extends AppCompatEditText {
    private Paint lineNumberPaint;
    private int lineNumberPadding = 70;  
    private String[] keywords = {"public", "private", "protected", "class", "void", "if", "else", "for", "while"};
    
    // caching نتایج تحلیل متن برای syntax highlighting
    private Map<Integer, String> syntaxCache = new HashMap<>();
    
    // Auto-completion
    private ListPopupWindow suggestionPopup;
    private String[] completions = {"public", "private", "protected", "class", "void", "static", "final"};

    public AdvancedCodeEditor(Context context) {
        super(context);
        init();
    }

    public AdvancedCodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdvancedCodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // تنظیمات Paint برای شماره خطوط
        lineNumberPaint = new Paint();
        lineNumberPaint.setColor(0xFF888888);
        lineNumberPaint.setTextSize(30);

        // افزایش padding جهت شماره‌گذاری
        setPadding(lineNumberPadding, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        
        // راه‌اندازی Auto-completion با استفاده از ListPopupWindow
        suggestionPopup = new ListPopupWindow(getContext());
        suggestionPopup.setAnchorView(this);
        suggestionPopup.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, completions));
        suggestionPopup.setOnItemClickListener((parent, view, position, id) -> {
            String suggestion = (String) parent.getItemAtPosition(position);
            // درج کامل کلمه پیشنهادی به جای کلمه ناقص
            String currentWord = getCurrentWord();
            if (currentWord != null) {
                int cursorPos = getSelectionStart();
                int start = getText().toString().lastIndexOf(currentWord, cursorPos);
                getText().replace(start, cursorPos, suggestion);
            }
            suggestionPopup.dismiss();
        });
        
        // افزودن Listener جهت syntax highlighting و auto-completion
        addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                // استفاده از caching ساده؛ در این مثال بر اساس طول متن
                int textHash = s.toString().hashCode();
                if (!syntaxCache.containsKey(textHash)) {
                    // تحلیل متن و ذخیره نتایج (مثلاً اجرای syntax highlighting ساده)
                    highlightKeywords(s);
                    syntaxCache.put(textHash, s.toString());
                }
                // Auto-completion: نمایش پیشنهاد در صورت تایپ حداقل دو حرف
                String currentWord = getCurrentWord();
                if (currentWord != null && currentWord.length() >= 2) {
                    suggestionPopup.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,
                            filterCompletions(currentWord)));
                    if (!suggestionPopup.isShowing())
                        suggestionPopup.show();
                } else {
                    if (suggestionPopup.isShowing())
                        suggestionPopup.dismiss();
                }
            }
        });
    }

    // استخراج کلمه جاری بر اساس موقعیت cursor
    private String getCurrentWord() {
        String text = getText().toString();
        int cursorPos = getSelectionStart();
        if (cursorPos <= 0) return "";
        int start = text.lastIndexOf(" ", cursorPos - 1);
        start = (start == -1) ? 0 : start + 1;
        int end = text.indexOf(" ", cursorPos);
        if (end == -1) end = text.length();
        return text.substring(start, cursorPos);
    }

    // فیلتر completions بر اساس پیشوند داده شده
    private String[] filterCompletions(String prefix) {
        java.util.List<String> results = new java.util.ArrayList<>();
        for (String comp : completions) {
            if (comp.startsWith(prefix))
                results.add(comp);
        }
        return results.toArray(new String[0]);
    }

    // اعمال syntax highlighting ساده برای کلمات کلیدی
    private void highlightKeywords(Editable s) {
        // حذف span‌های قبلی
        ForegroundColorSpan[] spans = s.getSpans(0, s.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) {
            s.removeSpan(span);
        }
        String text = s.toString();
        for (String keyword : keywords) {
            int index = text.indexOf(keyword);
            while (index >= 0) {
                // بررسی اینکه کاراکترهای اطراف کلمه از حرف یا رقم نباشند
                boolean leftOk = (index == 0) || !Character.isLetterOrDigit(text.charAt(index - 1));
                boolean rightOk = (index + keyword.length() == text.length()) || !Character.isLetterOrDigit(text.charAt(index + keyword.length()));
                if (leftOk && rightOk) {
                    s.setSpan(new ForegroundColorSpan(0xFF0000FF), index, index + keyword.length(),
                            Editable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                index = text.indexOf(keyword, index + keyword.length());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // رسم شماره خطوط
        int baseline = getBaseline();
        int lineCount = getLineCount();
        for (int i = 0; i < lineCount; i++) {
            int y = baseline + i * getLineHeight();
            canvas.drawText(String.valueOf(i + 1), 0, y, lineNumberPaint);
        }
        super.onDraw(canvas);
    }
}