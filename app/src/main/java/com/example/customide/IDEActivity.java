package com.example.customide;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.customide.console.ConsoleFragment;
import com.example.customide.editor.EditorFragment;
import com.example.customide.filemanager.FileManagerFragment;

public class IDEActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ide);

        // تنظیم Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("IDE Custom Advanced");

        // تنظیم ناوبری سفارشی از طریق دکمه‌ها
        Button btnEditor = findViewById(R.id.btnEditor);
        Button btnFiles = findViewById(R.id.btnFiles);
        Button btnConsole = findViewById(R.id.btnConsole);

        btnEditor.setOnClickListener(v -> replaceFragment(EditorFragment.newInstance(null)));
        btnFiles.setOnClickListener(v -> replaceFragment(new FileManagerFragment()));
        btnConsole.setOnClickListener(v -> replaceFragment(new ConsoleFragment()));

        // نمایش اولیه EditorFragment
        replaceFragment(EditorFragment.newInstance(null));
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }
}