package com.example.testpdfviewer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private String filename = "unknown.pdf";
    private String fileUrl = "https://www.chp.gov.hk/files/pdf/building_list_chi.pdf";
    private String sourcePath, destinationPath;
    private final int WRITE_EXTERNAL_STORAGE = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnDownloadFromUrl = findViewById(R.id.btnDownloadFromUrl);
        Button btnDownloadFromInternal = findViewById(R.id.btnDownloadFromInternal);
        btnDownloadFromInternal.setEnabled(false);
        btnDownloadFromUrl.setOnClickListener(view -> {
            new DownloadFile().execute(fileUrl, filename);
            btnDownloadFromInternal.setEnabled(true);
        });
        btnDownloadFromInternal.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==-1)
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            new SaveFromInternalToExternal().execute();
        });
    }

    public static void DownloadFromUrl(String fileUrl, File directory){
        final int  MEGABYTE = 1024 * 1024;
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(directory);
            int totalSize = urlConnection.getContentLength();
            byte[] buffer = new byte[MEGABYTE];
            int bufferLength = 0;
            while((bufferLength = inputStream.read(buffer))>0 ){
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            fileOutputStream.close();
            System.out.println("File downloaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alert = builder
                        .setTitle("ACCESS DENIED")
                        .setMessage("To download file, \nYou must give permission")
                        .setPositiveButton("OK", (dialog, which) -> {}).create();
                alert.show();
            }
        }
    }

    private class SaveFromInternalToExternal extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            File source = new File(sourcePath);
            File destination= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            destinationPath = destination.getAbsolutePath();
            System.out.println("destinationPath: "+destinationPath);
            try {
                try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(destination)) {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
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
            String fileUrl = strings[0];
            String fileName = strings[1];
            File folder = getApplicationContext().getDir("doc", Context.MODE_PRIVATE);
            folder.mkdir();
            File pdfFile = new File(folder, fileName);
            try{
                pdfFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            DownloadFromUrl(fileUrl, pdfFile);
            sourcePath = getApplicationContext().getDir("doc", Context.MODE_PRIVATE).toString()+"/"+fileName;
            System.out.println("Filepath: " + sourcePath);
            return null;
        }
    }



}