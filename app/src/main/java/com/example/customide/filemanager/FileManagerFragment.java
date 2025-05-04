package com.example.customide.filemanager;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.customide.R;
import com.example.customide.editor.EnhancedSoraEditorFragment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerFragment extends Fragment {
    private RecyclerView recyclerViewFileTree;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_manager, container, false);
        recyclerViewFileTree = view.findViewById(R.id.recyclerViewFileTree);
        recyclerViewFileTree.setLayoutManager(new LinearLayoutManager(getContext()));
        
        File rootDir = Environment.getExternalStorageDirectory();
        FileNode rootNode = FileTreeBuilder.buildFileTree(rootDir);

        List<FileNode> nodeList = new ArrayList<>();
        flattenTree(rootNode, nodeList);

        FileTreeAdapter adapter = new FileTreeAdapter(nodeList, node -> {
            if (node.getFile().isFile()) {
                // باز کردن فایل: انتقال به EnhancedSoraEditorFragment با مسیر فایل
                getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, EnhancedSoraEditorFragment.newInstance(node.getFile().getAbsolutePath()))
                    .addToBackStack(null)
                    .commit();
            } else {
                // در صورت کلیک روی پوشه می‌توانید امکانات گسترش/فروپاشی را اضافه کنید
            }
        });
        recyclerViewFileTree.setAdapter(adapter);
        
        return view;
    }
    
    private void flattenTree(FileNode node, List<FileNode> list) {
        list.add(node);
        if (node.hasChildren()) {
            for (FileNode child : node.getChildren()) {
                flattenTree(child, list);
            }
        }
    }
}