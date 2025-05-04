package com.example.customide.filemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.customide.R;
import java.util.List;

public class FileTreeAdapter extends RecyclerView.Adapter<FileTreeAdapter.FileTreeViewHolder> {
    private List<FileNode> fileNodes;
    private OnFileClickListener listener;

    public interface OnFileClickListener {
        void onFileClick(FileNode node);
    }

    public FileTreeAdapter(List<FileNode> fileNodes, OnFileClickListener listener) {
        this.fileNodes = fileNodes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileTreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                      .inflate(R.layout.item_file_node, parent, false);
        return new FileTreeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileTreeViewHolder holder, int position) {
        FileNode node = fileNodes.get(position);
        holder.fileNameTextView.setText(node.getFile().getName());
        holder.itemView.setOnClickListener(v -> listener.onFileClick(node));
    }

    @Override
    public int getItemCount() {
        return fileNodes.size();
    }

    public static class FileTreeViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        public FileTreeViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.textFileNode);
        }
    }
}