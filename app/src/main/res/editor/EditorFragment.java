package com.example.customide.editor;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

public class EditorFragment extends Fragment {
    public static EnhancedSoraEditorFragment newInstance(String filePath) {
        return EnhancedSoraEditorFragment.newInstance(filePath);
    }
}