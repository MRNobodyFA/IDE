package com.example.customide.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileNode {
    private File file;
    private List<FileNode> children;
    private boolean isExpanded;

    public FileNode(File file) {
        this.file = file;
        this.children = new ArrayList<>();
        this.isExpanded = false;
    }

    public File getFile() {
        return file;
    }

    public List<FileNode> getChildren() {
        return children;
    }

    public void addChild(FileNode child) {
        children.add(child);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}