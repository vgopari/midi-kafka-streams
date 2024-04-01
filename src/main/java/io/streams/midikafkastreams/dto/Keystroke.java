package io.streams.midikafkastreams.dto;

public class Keystroke {
    private String eventType;
    private String noteName;
    private int velocity;
    private int noteNum;
    private long timestamp;

    public Keystroke() {
    }

    public Keystroke(String eventType, String noteName, int velocity, int noteNum, long timestamp) {
        this.eventType = eventType;
        this.noteName = noteName;
        this.velocity = velocity;
        this.noteNum = noteNum;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getNoteNum() {
        return noteNum;
    }

    public void setNoteNum(int noteNum) {
        this.noteNum = noteNum;
    }
}
