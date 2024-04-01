package io.streams.midikafkastreams.service;

import io.streams.midikafkastreams.MidiKeyboardListener;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ChordsService {

    private final Logger logger = LoggerFactory.getLogger(ChordsService.class);

    private final KafkaProducer<String, String> kafkaProducer;

    private final String chordsTopic;

    @Autowired
    public ChordsService(KafkaProducer<String, String> kafkaProducer,
                         @Value("${kafka.chords.topic.name}") String chordsTopic) {
        this.kafkaProducer = kafkaProducer;
        this.chordsTopic = chordsTopic;
    }

    public void produceChordsJSON() {
        String filePath = "src/main/resources/chords.json";

        try {
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // Convert JSON object to string
                String jsonString = jsonObject.toString();
                logger.info(jsonString);
                // Produce to Kafka topic
                kafkaProducer.send(new ProducerRecord<>(chordsTopic, jsonString));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
