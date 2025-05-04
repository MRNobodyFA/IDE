package com.example.customide.filemanager;

import java.io.File;

public class FileTreeBuilder {
    public static FileNode buildFileTree(File root) {
        FileNode node = new FileNode(root);
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    node.addChild(buildFileTree(file));
                }
            }
        }
        return node;
    }
}