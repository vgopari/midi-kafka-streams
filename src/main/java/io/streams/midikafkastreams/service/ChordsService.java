package io.streams.midikafkastreams.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streams.midikafkastreams.MidiKeyboardListener;
import io.streams.midikafkastreams.dto.Chord;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ChordsService {

    private final Logger logger = LoggerFactory.getLogger(ChordsService.class);

    private final KafkaProducer<String, String> kafkaProducer;

    private final String chordsTopic;

    @Autowired
    public ChordsService(KafkaProducer<String, String> kafkaProducer,
                         @Value("${kafka.input.chords.topic.name}") String chordsTopic) {
        this.kafkaProducer = kafkaProducer;
        this.chordsTopic = chordsTopic;
    }

    public void produceChordsJSON() {
        String filePath = "src/main/resources/chords.json";

        try {
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                Chord chord = convertToChord(jsonArray.getJSONObject(i));
                String jsonString = convertChordToString(chord);
                logger.info(jsonString);
                logger.info(Arrays.toString(chord.getNotes().toArray()));
                kafkaProducer.send(new ProducerRecord<>(chordsTopic, Arrays.toString(chord.getNotes().stream().sorted().toArray()), jsonString));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Chord convertToChord(JSONObject jsonObject) {
        String name = jsonObject.getString("name");
        String route = jsonObject.getString("route");
        List<String> notes = jsonArrayToList(jsonObject.getJSONArray("notes"));
        List<String> intervals = jsonArrayToList(jsonObject.getJSONArray("intervals"));
        List<Integer> midiKeys = jsonArrayToListInt(jsonObject.getJSONArray("midiKeys"));

        return new Chord(name, route, notes, intervals, midiKeys);
    }

    private List<Integer> jsonArrayToListInt(JSONArray jsonArray) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getInt(i));
        }
        return list;
    }
    private List<String> jsonArrayToList(JSONArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }

    private String convertChordToString(Chord chord) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(chord);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}