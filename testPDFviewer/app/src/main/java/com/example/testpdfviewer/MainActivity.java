package com.example.testpdfviewer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private Button btnDownloadFromUrl, btnDownloadFromInternal;
    private String filename = "5e68a50411858.pdf";
    private String filepath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnDownloadFromUrl = findViewById(R.id.btnDownloadFromUrl);
        btnDownloadFromInternal = findViewById(R.id.btnDownloadFromInternal);
        btnDownloadFromUrl.setOnClickListener(view -> {
            new DownloadFile().execute("https://www.radioicare.org/upload/healthinfo/58/pdf/5e68a50411858.pdf", "5e68a50411858.pdf");
        });

        btnDownloadFromInternal.setOnClickListener(view -> {

            new SaveToExternal().execute();
        });



    }

    private class SaveToExternal extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            if(ActivityCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==-1)
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            File source = new File(filepath);
            File destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File destination= new File(destinationPath, filename);
            System.out.println("destinationPath: "+destinationPath);
            try {
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);
                try {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    out.close();
                    in.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            File folder = getApplicationContext().getDir("doc", Context.MODE_PRIVATE);
            folder.mkdir();
            File pdfFile = new File(folder, fileName);
            try{
                pdfFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            FileDownloader.downloadFile(fileUrl, pdfFile);
            filepath = getApplicationContext().getDir("doc", Context.MODE_PRIVATE).toString()+"/"+fileName;
            System.out.println("Filepath: " + filepath);
            return null;
        }
    }
}