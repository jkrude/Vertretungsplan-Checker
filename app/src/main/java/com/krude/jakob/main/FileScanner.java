package com.krude.jakob.main;

import android.util.Log;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.util.Calendar;

class FileScanner{
    static final String outOfDate = "OUT_OF_DATE";
    static final String badLayout = "BAD_LAYOUT";
    static final String notAffected = "NOT_AFFECTED";
    static final String ioException = "IO_EXCEPTION";


    static String[] scanPdf(String fileLocation, String ... strings){
        final String TAG = "FileScanner";
        String schoolClass = strings[0];

        StringBuilder resultText = new StringBuilder();

        StringBuilder parsedText= new StringBuilder();
        PdfReader reader;
        try {
            reader = new PdfReader(fileLocation);

            Log.d(TAG, "started PdfReader");
            int n = reader.getNumberOfPages();
            for (int i = 0; i <n ; i++) {
                parsedText.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim()).append("\n"); //Extracting the content from the different pages
            }
            Log.d(TAG, "extracted Text");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, ioException);
            return new String[]{ ioException, ""};
        }
        int index_before_date = parsedText.indexOf("Vertretungsplan");
        index_before_date += 17;
        String date = parsedText.substring(index_before_date,index_before_date+5);
        String[] parts = date.split("\\.");
        if(parts.length != 2){
            return new String[]{badLayout, ""};
        }

        String remainingText;
        String additionalInfo;
        int tmp =parsedText.indexOf("Betroffene");
        if(tmp == -1){
            Log.d(TAG, badLayout);
            return new String[]{badLayout, ""};
        }
        additionalInfo = parsedText.substring(parsedText.indexOf("Online"),tmp);

        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH)+1;
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        if (month == currentMonth){
            if(day < currentDay){
                Log.d(TAG, outOfDate);
                return new String[]{outOfDate, additionalInfo, date};
            }
        }
        if( month < currentMonth){ return new String[]{outOfDate, additionalInfo, date}; }


        remainingText = parsedText.substring(tmp);
        String[] lines = remainingText.split("\n");

        if(!lines[0].contains(schoolClass)){
            Log.d(TAG, notAffected);
            return new String[]{notAffected,additionalInfo,date};
        }

        for(String line : lines){
            if(line.startsWith(schoolClass) || line.startsWith("("+schoolClass+")")) {
                if(strings.length > 1){
                    for(int i = 1; i < strings.length; ++i){
                        if(line.contains(strings[i])){
                            resultText.append(line).append("\n");
                        }
                    }
                } else{
                    resultText.append(line).append("\n");
                }
            }
        }
        Log.d(TAG, "scanned File");
        return new String[]{resultText.toString(), additionalInfo, date};

    }
}

