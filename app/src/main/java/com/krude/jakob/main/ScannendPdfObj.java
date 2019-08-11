package com.krude.jakob.main;

import java.util.List;

public class ScannendPdfObj {

    public enum State {
        OUT_OF_DATE,
        BAD_LAYOUT,
        NOT_AFFECTED,
        IO_EXCEPTION,
        AFFECTED
    }


    private String date;
    private List<String> additionalInfo; //the header of schedule change pdf
    private List<String> relevantChanges; //can be either referring to the class or to the courses
    private List<String> allChanges;
    private State state;

    // if everything is known and all worked out, the state should be AFFECTED
    public ScannendPdfObj(String date, List<String> additionalInfo, List<String> relevantChanges, List<String> allChanges) {
        this.date = date;
        this.additionalInfo = additionalInfo;
        this.relevantChanges = relevantChanges;
        this.allChanges = allChanges;
        this.state = State.AFFECTED;
    }

    // if the State is not NOT_AFFECTED or OUT_OF_DATE
    public ScannendPdfObj(State state, String date, List<String> additionalInfo, List<String> allChanges) {
        this.date = date;
        this.additionalInfo = additionalInfo;
        this.allChanges = allChanges;
        this.state = State.NOT_AFFECTED;
        this.state = state;
    }


    public ScannendPdfObj(State state) {
        this.state = state;
    }

    public String getDate() {
        return date;
    }

    public List<String> getRelevantChanges() {
        return relevantChanges;
    }

    public List<String> getAllChanges() {
        return allChanges;
    }

    public List<String> getAdditionalInfo() {
        return additionalInfo;
    }

    public State getState() {
        return state;
    }
}
