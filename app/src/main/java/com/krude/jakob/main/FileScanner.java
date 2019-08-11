package com.krude.jakob.main;

//import android.util.Log;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

class FileScanner{

    static ScannendPdfObj scanPdf(String fileLocation, String schoolClass, String[] visitedCourses){
        final String TAG = "FileScanner";

        StringBuilder parsedText= new StringBuilder();
        PdfReader reader;
        try {
            reader = new PdfReader(fileLocation);

            //Log.d(TAG, "started PdfReader");
            int n = reader.getNumberOfPages();
            for (int i = 0; i <n ; i++) {
                parsedText.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim()).append("\n"); //Extracting the content from the different pages
            }
            //Log.d(TAG, "extracted Text");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            //Log.d(TAG, ScannendPdfObj.State.IO_EXCEPTION.toString());
            return new ScannendPdfObj(ScannendPdfObj.State.IO_EXCEPTION);
        }
        int indexBeforeDate = parsedText.indexOf("Vertretungsplan");
        indexBeforeDate += 17;
        String date = parsedText.substring(indexBeforeDate,indexBeforeDate+5);
        String[] parts = date.split("\\.");
        if(parts.length != 2){
            return new ScannendPdfObj(ScannendPdfObj.State.BAD_LAYOUT);
        }

        List<String> allChanges;
        List<String> additionInfo;
        List<String> relevantChangesClass = new ArrayList<>();
        List<String> relevantChangesCourses = new ArrayList<>();
        
        // ---------- check if pdf-layout is correct ----------
        int idxOfBetroffene =parsedText.indexOf("Betroffene");
        if(idxOfBetroffene == -1){
            //Log.d(TAG, ScannendPdfObj.State.BAD_LAYOUT.toString());
            return new ScannendPdfObj(ScannendPdfObj.State.BAD_LAYOUT);
        }
        String [] tmpAdditionInfo = parsedText.substring(
                parsedText.indexOf("Online"),idxOfBetroffene)
                .split("\n");
        additionInfo = new ArrayList<>(Arrays.asList(tmpAdditionInfo));

        // ---------- cut out the needed part of the parsedText ----------
        String parsedTextString = parsedText.toString().substring(0,parsedText.indexOf("G r u b e r "));
        parsedTextString = parsedTextString.substring(idxOfBetroffene);

        // convert the rest of the pdf to a List of Strings representing lines in the pdf
        allChanges = new ArrayList<>(Arrays.asList(parsedTextString.split("\n")));
        if (allChanges.size() == 0)
            return new ScannendPdfObj(ScannendPdfObj.State.BAD_LAYOUT);

        // ---------- check for the relevance of the date ----------
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH)+1;
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        if (month == currentMonth){
            if(day < currentDay){
                //Log.d(TAG, ScannendPdfObj.State.OUT_OF_DATE.toString());
                return new ScannendPdfObj(ScannendPdfObj.State.OUT_OF_DATE,date, additionInfo, allChanges);
            }
        }
        if( month < currentMonth) {
            //Log.d(TAG, ScannendPdfObj.State.OUT_OF_DATE.toString());
            return new ScannendPdfObj(ScannendPdfObj.State.OUT_OF_DATE, date, additionInfo, allChanges);
        }

        // check for relevance regarding the class
        if(!allChanges.get(0).contains(schoolClass)){
            //Log.d(TAG, ScannendPdfObj.State.NOT_AFFECTED.toString());
            return new ScannendPdfObj(ScannendPdfObj.State.NOT_AFFECTED, date, additionInfo, allChanges);
        }else{
            allChanges.remove(0); // remove affected classes
            allChanges.remove(0); //remove irrelevant information
        }
        // ---------- reformat the lines of allChanges ----------
        for(int i = 0; i < allChanges.size(); ++i){
            String line = allChanges.get(i);
            if(line.length() <= 1)
                continue;
            if(Character.isDigit(line.charAt(0)) && line.charAt(1) == '.'){
                allChanges.set(i,line.charAt(0)+". Stunde");
            }

        }


        // add all lines that contain the specified class
        for(String line : allChanges){
            if(line.startsWith(schoolClass) || line.startsWith("("+schoolClass+")")
            || line.startsWith("12, "+schoolClass) || line.startsWith("11, "+schoolClass)) {
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
            return new ScannendPdfObj(date, additionInfo, relevantChangesClass, allChanges);
        }
        //Log.d(TAG, "scanned File");

        if(relevantChangesCourses.size() == 0)
            //there are courses specified and no occurrences are found -> state = not affected
            return new ScannendPdfObj(ScannendPdfObj.State.NOT_AFFECTED, date, additionInfo, allChanges);
        else
            //in this case there are relevant changes regarding specified courses
            return new ScannendPdfObj(date, additionInfo, relevantChangesCourses, allChanges);

    }
}

