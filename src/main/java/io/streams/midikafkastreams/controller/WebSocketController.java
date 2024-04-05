package io.streams.midikafkastreams.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://127.0.0.1:5501")
@RestController
public class WebSocketController {

    @MessageMapping("/midi-events")
    @SendTo("/topic/midi-events")
    public String handleMidiEvent(String message) {
        return message;
    }

    @MessageMapping("/chord-events")
    @SendTo("/topic/chord-events")
    public String handleChordEvent(String message) {
        return message;
    }
}
