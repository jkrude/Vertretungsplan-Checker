package com.krude.jakob.main;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class DownloadFile extends AsyncTask<String, Void, Void> {
    private final String TAG = "AsyncTask";

    private static boolean success;
    AsyncResponse delegate = null;

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "started background Job");
        success = false;
        String fileUrl = strings[0];   // -> https://www.graues-kloster.de/files/ovp_1.pdf
        String fileName = strings[1];  // -> ovp_1.pdf

        FileDownloader fileDownloader = new FileDownloader();
        fileDownloader.downloadFile(fileUrl,fileName);
        Log.d(TAG, "Downloaded File");

        return null;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(TAG, "onPostExecution");

        Log.d(TAG, "delegate: " + delegate);

        delegate.downloadFinished(success);
    }

    private class FileDownloader {
        private static final int MEGABYTE = 1024 * 1024;

        private void downloadFile(String fileUrl, String directory) {
            final String TAG = "FileDownloader";
            Log.d(TAG, "started Download");

            try {

                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                Log.d(TAG, "connected to server");

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(new File(directory));
                int totalSize = urlConnection.getContentLength();

                byte[] buffer = new byte[MEGABYTE];
                int bufferLength = 0;
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();

                Log.d(TAG, "done downloading and closed file");

                DownloadFile.success = true;

            } catch (FileNotFoundException e) {
                Log.d(TAG, "FileNotFoundException");
                System.err.print("Could not find File");
                //e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.d(TAG, "MalformedURLException");
                System.err.print("Bad Url");
                //e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "IOException");
                //e.printStackTrace();
            }
        }
    }

}