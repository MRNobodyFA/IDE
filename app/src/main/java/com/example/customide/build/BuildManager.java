package com.example.customide.build;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuildManager {
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public interface BuildCallback {
        void onOutput(String output);
        void onError(String error);
        void onComplete(int exitCode);
    }

    public void runBuildCommand(String[] command, BuildCallback callback) {
        executorService.execute(() -> {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            try {
                Process process = processBuilder.start();

                BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = stdOutReader.readLine()) != null) {
                    postOutput(callback, line + "\n");
                }
                while ((line = stdErrReader.readLine()) != null) {
                    postError(callback, line + "\n");
                }
                int exitCode = process.waitFor();
                postComplete(callback, exitCode);
            } catch (IOException | InterruptedException e) {
                postError(callback, "Exception: " + e.getMessage() + "\n");
                postComplete(callback, -1);
            }
        });
    }

    private void postOutput(BuildCallback callback, String line) {
        uiHandler.post(() -> callback.onOutput(line));
    }

    private void postError(BuildCallback callback, String line) {
        uiHandler.post(() -> callback.onError(line));
    }

    private void postComplete(BuildCallback callback, int exitCode) {
        uiHandler.post(() -> callback.onComplete(exitCode));
    }
}