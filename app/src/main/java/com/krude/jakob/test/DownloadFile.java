package com.krude.jakob.test;

import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class DownloadFile extends AsyncTask<String, Void, String> {
    AsyncResponse delegate = null;
    @Override
    protected String doInBackground(String... strings) {

        String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
        String fileName = strings[1];  // -> maven.pdf

        FileDownloader.downloadFile(fileUrl, fileName);
        //Toast.makeText(MainActivity.globalContext, "Downloaded File", Toast.LENGTH_SHORT).show(); // For example
        //Toast.makeText(MainActivity.globalContext, "Displayed File", Toast.LENGTH_SHORT).show(); // For example

        return FileScanner.scanPdf(fileUrl);

    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}


class FileDownloader {

    private static final int MEGABYTE = 1024 * 1024;

    static void downloadFile(String fileUrl, String directory) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(directory));
            int totalSize = urlConnection.getContentLength();

            byte[] buffer = new byte[MEGABYTE];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            fileOutputStream.close();
            System.out.println("Done Downloading");
        } catch (FileNotFoundException e) {
            System.err.print("Could not find File");
            //e.printStackTrace();
        } catch (MalformedURLException e) {
            System.err.print("Bad Url");
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}

class FileScanner{
    static String scanPdf(String fileLocation){
        String resultText = "";
        try {
            String parsedText="";
            PdfReader reader = new PdfReader(fileLocation);
            int n = reader.getNumberOfPages();
            for (int i = 0; i <n ; i++) {
                parsedText = parsedText+ PdfTextExtractor.getTextFromPage(reader, i+1).trim()+"\n"; //Extracting the content from the different pages
            }
            reader.close();

            int tmp =parsedText.indexOf("Betroffene");
            String remainingText;
            if(tmp == -1){
                return "Error in Pdf";
            }
            remainingText = parsedText.substring(tmp);
            String[] lines = remainingText.split("\n");

            if(!lines[0].contains("11")){
                return "Class 11 is not affected.";
            }
            for(String line : lines){
                if(line.startsWith("11")) {
                    resultText += line + "\n";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultText;
    }
}
