package com.krude.jakob.main;

import android.util.Log;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

class FileScanner{

    static ScanedPdf scanPdf(String fileLocation, String schoolClass, String[] visitedCourses){
        final String TAG = "FileScanner";

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
            Log.d(TAG, ScanedPdf.State.IO_EXCEPTION.toString());
            return new ScanedPdf(ScanedPdf.State.IO_EXCEPTION);
        }
        int indexBeforeDate = parsedText.indexOf("Vertretungsplan");
        indexBeforeDate += 17;
        String date = parsedText.substring(indexBeforeDate,indexBeforeDate+5);
        String[] parts = date.split("\\.");
        if(parts.length != 2){
            return new ScanedPdf(ScanedPdf.State.BAD_LAYOUT);
        }

        List<String> allChanges;
        List<String> additionInfo;
        List<String> relevantChangesClass = new ArrayList<>();
        List<String> relevantChangesCourses = new ArrayList<>();
        
        // ---------- check if pdf-layout is correct ----------
        int idxOfBetroffene =parsedText.indexOf("Betroffene");
        if(idxOfBetroffene == -1){
            Log.d(TAG, ScanedPdf.State.BAD_LAYOUT.toString());
            return new ScanedPdf(ScanedPdf.State.BAD_LAYOUT);
        }
        String [] tmpAdditionInfo = parsedText.substring(
                parsedText.indexOf("Online"),idxOfBetroffene)
                .split("\n");
        additionInfo = new ArrayList<>(Arrays.asList(tmpAdditionInfo));

        // convert the rest of the pdf to a List of Strings representing lines in the pdf
        allChanges = new ArrayList<>(Arrays.asList(parsedText.substring(idxOfBetroffene).split("\n")));
        if (allChanges.size() == 0)
            return new ScanedPdf(ScanedPdf.State.BAD_LAYOUT);

        // ---------- check for the relevance of the date ----------
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH)+1;
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        if (month == currentMonth){
            if(day < currentDay){
                Log.d(TAG, ScanedPdf.State.OUT_OF_DATE.toString());
                return new ScanedPdf(ScanedPdf.State.OUT_OF_DATE,date, additionInfo, allChanges);
            }
        }
        if( month < currentMonth) {
            Log.d(TAG, ScanedPdf.State.OUT_OF_DATE.toString());
            return new ScanedPdf(ScanedPdf.State.OUT_OF_DATE, date, additionInfo, allChanges);
        }

        // check for relevance regarding the class
        if(!allChanges.get(0).contains(schoolClass)){
            Log.d(TAG, ScanedPdf.State.NOT_AFFECTED.toString());
            return new ScanedPdf(ScanedPdf.State.NOT_AFFECTED, date, additionInfo, allChanges);
        }else{
            allChanges.remove(0);
        }

        // add all lines that contain the specified class
        for(String line : allChanges){
            if(line.startsWith(schoolClass) || line.startsWith("("+schoolClass+")")) {
                relevantChangesClass.add(line);
            }
        }
        // if courses were specified check for relevance regarding those
        if(visitedCourses != null){
            for(String line : relevantChangesClass){
                for(String course : visitedCourses){
                    if(line.contains(course)){
                        relevantChangesCourses.add(line);
                    }
                }
            }
        }else{
            // there are no courses specified
            return new ScanedPdf(date, additionInfo, relevantChangesClass, allChanges);
        }
        Log.d(TAG, "scanned File");

        if(relevantChangesCourses.size() == 0)
            //there are courses specified and no occurrences are found -> state = not affected
            return new ScanedPdf(ScanedPdf.State.NOT_AFFECTED, date, additionInfo, allChanges);
        else
            //in this case there are relevant changes regarding specified courses
            return new ScanedPdf(date, additionInfo, relevantChangesCourses, allChanges);

    }
}

