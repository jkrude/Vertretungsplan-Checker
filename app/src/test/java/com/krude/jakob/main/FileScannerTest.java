package com.krude.jakob.main;

import org.junit.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileScannerTest {
    private String path;
    private String schoolClass;
    private String[] visitedClasses = {"pwGK1"};
    private ScannendPdfObj obj;

    @Before
    public void setUp(){
        URL url = getClass().getResource("/ovp_1.pdf");
        if(url == null)
            throw new NullPointerException();
        path = url.getPath();
        schoolClass = "11";
        obj = FileScanner.scanPdf(path, schoolClass,visitedClasses);
    }

    @Test
    public void testCutLastLine(){
        assertNotNull(obj.getAllChanges());
        for(String line : obj.getAllChanges()){
            if(line.contains("G r u b e r"))
                fail();
        }

    }

    @Test
    public void testDate(){
        assertEquals("12.8.",obj.getDate());

    }

    @Test
    public void testRelevant(){
        List<String> relChanges = new ArrayList<>();
        relChanges.add("11 pwGK1 --- --- Entfall");
        assertEquals(relChanges, obj.getRelevantChanges());
        assertEquals(ScannendPdfObj.State.AFFECTED, obj.getState());
    }

    @Test
    public void testRelevantWithoutCourse(){
        obj = FileScanner.scanPdf(path,schoolClass,null);
        List<String> relChanges = new ArrayList<>();
        relChanges.add("11 LaLK1 --- --- Entfall");
        relChanges.add("11 pwGK1 --- --- Entfall");
        relChanges.add("12, 11 Sp-1/3 --- --- Entfall");
        assertEquals(relChanges, obj.getRelevantChanges());
    }
    @Test
    public void testNotRelevant(){
        schoolClass = "UIId";
        obj = FileScanner.scanPdf(path,schoolClass,null);
        assertEquals(ScannendPdfObj.State.NOT_AFFECTED, obj.getState());
        assertNull(obj.getRelevantChanges());
    }

    @Test
    public void testAllChangesCutsTopLine(){
        assertNotNull(obj.getAllChanges());
        for(String line : obj.getAllChanges()){
             if(line.contains("Klasse(n)"))
                 fail();
        }

    }

    @Test
    public void testAllChangesContainsWrongFormats(){
        for(String line : obj.getAllChanges()){
            if(line.contains("1. 1."))
                fail();
            if(line.contains("Stu Stund"))
                fail();
        }
    }

}
