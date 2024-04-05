package io.streams.midikafkastreams.dto;

import java.util.List;

public class Chord {
    private String name;
    private String route;
    private List<String> notes;
    private List<String> intervals;
    private List<Integer> midiKeys;

    // Constructor, getters, and setters
    public Chord() {
    }

    public Chord(String name, String route, List<String> notes, List<String> intervals, List<Integer> midiKeys) {
        this.name = name;
        this.route = route;
        this.notes = notes;
        this.intervals = intervals;
        this.midiKeys = midiKeys;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public List<String> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<String> intervals) {
        this.intervals = intervals;
    }

    public List<Integer> getMidiKeys() {
        return midiKeys;
    }

    public void setMidiKeys(List<Integer> midiKeys) {
        this.midiKeys = midiKeys;
    }
}