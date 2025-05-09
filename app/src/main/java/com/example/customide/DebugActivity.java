package com.example.customide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DebugActivity extends Activity {

    private String[] exceptionTypes = {
            "StringIndexOutOfBoundsException",
            "IndexOutOfBoundsException",
            "ArithmeticException",
            "NumberFormatException",
            "ActivityNotFoundException"
    };

    private String[] exceptionMessages = {
            "Invalid string operation\n",
            "Invalid list operation\n",
            "Invalid arithmetical operation\n",
            "Invalid toNumber block operation\n",
            "Invalid intent operation"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            // اگر نیاز به استفاده از یک layout دارید، می‌توانید setContentView را اضافه کنید.
            // setContentView(R.layout.activity_debug);

            Intent intent = getIntent();
            String errorMessage = "";
            String madeErrorMessage = "";

            if (intent != null && intent.hasExtra("error")) {
                errorMessage = intent.getStringExtra("error");
                if (errorMessage == null) {
                    errorMessage = "No error message provided.";
                }
                String[] split = errorMessage.split("\n");
                try {
                    for (int j = 0; j < exceptionTypes.length; j++) {
                        if (split[0].contains(exceptionTypes[j])) {
                            madeErrorMessage = exceptionMessages[j];
                            int addIndex = split[0].indexOf(exceptionTypes[j]) + exceptionTypes[j].length();
                            if (split[0].length() > addIndex) {
                                madeErrorMessage += split[0].substring(addIndex);
                            }
                            madeErrorMessage += "\n\nDetailed error message:\n" + errorMessage;
                            break;
                        }
                    }
                    if (madeErrorMessage.isEmpty()) {
                        madeErrorMessage = errorMessage;
                    }
                } catch (Exception e) {
                    madeErrorMessage += "\n\nError while processing error: " + e.getMessage();
                }
            } else {
                madeErrorMessage = "No error message provided in Intent.";
            }

            // ذخیره گزارش خطا در یک فایل خارج از حافظه برنامه (دایرکتوری Documents)
            writeErrorToExternalFile(madeErrorMessage);

            // نمایش AlertDialog جهت اطلاع‌رسانی به کاربر
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("An error occurred");
            builder.setMessage(madeErrorMessage);
            builder.setPositiveButton("End Application", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.create().show();
        } catch (Exception ex) {
            Toast.makeText(this, "Exception in DebugActivity onCreate: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * این متد پیام خطا را به یک فایل در دایرکتوری Documents حافظه خارجی می‌نویسد.
     */
    private void writeErrorToExternalFile(String error) {
        try {
            // دریافت دایرکتوری عمومی Documents در حافظه خارجی
            File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!externalDir.exists()) {
                externalDir.mkdirs();
            }
            // تعریف فایل گزارش خطا (برای مثال error_log.txt)
            File logFile = new File(externalDir, "error_log.txt");
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write((error + "\n").getBytes());
            fos.close();
        } catch (IOException e) {
            // در صورت بروز خطا در نوشتار، آن را به صورت Toast نمایش می‌دهیم.
            Toast.makeText(this, "Error writing to external file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}