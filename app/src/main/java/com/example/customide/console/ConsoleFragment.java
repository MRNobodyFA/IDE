package com.example.customide.console;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.customide.R;

public class ConsoleFragment extends Fragment {
    private TextView consoleTextView;
    private ScrollView consoleScrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_console, container, false);
        consoleTextView = view.findViewById(R.id.textViewConsole);
        consoleScrollView = view.findViewById(R.id.consoleScrollView);
        return view;
    }
    
    public void appendLog(String logText) {
        if (consoleTextView != null) {
            consoleTextView.append(logText);
            consoleScrollView.post(() -> consoleScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }
}