package io.streams.midikafkastreams.controller;

import io.streams.midikafkastreams.MidiKeyboardListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(allowedHeaders = "http://127.0.0.1:5501")
public class RecordingController {

    private final MidiKeyboardListener midiKeyboardListener;

    @Autowired
    public RecordingController(MidiKeyboardListener midiKeyboardListener) {
        this.midiKeyboardListener = midiKeyboardListener;
    }

    @GetMapping("/start")
    public String startRecording() {
        return "{\"message\": \"Started recording - " + midiKeyboardListener.toggleRecording(true) + "\"}";
    }

    @GetMapping("/stop")
    public String stopRecording() {
        midiKeyboardListener.toggleRecording(false);
        return "{\"message\": \"stopped recording\"}";
    }

}
