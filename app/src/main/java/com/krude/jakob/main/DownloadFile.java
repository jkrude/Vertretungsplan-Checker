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


public class DownloadFile extends AsyncTask<String, Void, String[]> {
    private final String TAG = "AsyncTask";

    static boolean success;
    AsyncResponse delegate = null;

    @Override
    protected String[] doInBackground(String... strings) {
        Log.d(TAG, "started background Job");
        success = false;
        String fileUrl = strings[0];   // -> https://www.graues-kloster.de/files/ovp_1.pdf
        String fileName = strings[1];  // -> ovp_1.pdf

        FileDownloader.downloadFile(fileUrl, fileName);
        Log.d(TAG, "Downloaded File");

        String[] rest = new String[strings.length-2];
        System.arraycopy(strings, 2, rest, 0, strings.length - 2);
        return FileScanner.scanPdf(fileUrl, rest);

    }

    @Override
    protected void onPostExecute(String[] result) {
        Log.d(TAG, "onPostExecution");

        Log.d(TAG, "delegate: "+ delegate);

        delegate.processFinished(result, success);
    }
}


class FileDownloader {
    private static final int MEGABYTE = 1024 * 1024;

    static void downloadFile(String fileUrl, String directory) {
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

