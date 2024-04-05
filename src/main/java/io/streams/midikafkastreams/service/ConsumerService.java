package io.streams.midikafkastreams.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class ConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ConsumerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "keyboard-midi-events", groupId = "my-consumer-group")
    public void midiEventsListen(ConsumerRecord<String, String> record) {
        System.out.println("Received message: " + record.value());
        // Process the received message here
        messagingTemplate.convertAndSend("/topic/midi-events", record.value());
    }

    @KafkaListener(topics = "keyboard-chord-events", groupId = "my-consumer-group")
    public void chordEventsListen(ConsumerRecord<String, String> record) {
        System.out.println("Received message: " + record.value());
        // Process the received message here
        messagingTemplate.convertAndSend("/topic/chord-events", record.value());
    }

}
