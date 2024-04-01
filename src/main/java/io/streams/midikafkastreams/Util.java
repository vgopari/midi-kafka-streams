package io.streams.midikafkastreams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streams.midikafkastreams.dto.Keystroke;

public class Util {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Util() {
    }

    // Utility function to get note name from MIDI note number
    public static String getNoteName(int noteNumber) {
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int octave = (noteNumber / 12) - 1;
        int noteIndex = noteNumber % 12;
        return noteNames[noteIndex] + octave;
    }

    public static String toJson(Keystroke bankTransaction) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(bankTransaction);
    }

}
