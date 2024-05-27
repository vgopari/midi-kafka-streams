package io.streams.midikafkastreams.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streams.midikafkastreams.dto.Chord;
import io.streams.midikafkastreams.dto.Keystroke;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class ChordStream {

    private final Map<String, Chord> chordsMap;

    private final String chordsOutputTopic;
    Logger logger = LoggerFactory.getLogger(ChordStream.class);

    private final SimpMessagingTemplate messagingTemplate;

    private final String inputTopic;

    @Autowired
    public ChordStream(@Qualifier("defaultKafkaStreamsConfig") KafkaStreamsConfiguration kStreamsConfig,
                       @Value("${kafka.input.topic.name}") String inputTopic,
                       @Value("${kafka.output.chord.topic.name}") String chordsOutputTopic, SimpMessagingTemplate messagingTemplate) {
        this.inputTopic = inputTopic;
        this.chordsOutputTopic = chordsOutputTopic;
        this.messagingTemplate = messagingTemplate;
        this.chordsMap = new HashMap<>();
        loadChordsFromJson();
    }

    private Map<String, Chord> loadChordsFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String filePath = "src/main/resources/chords.json";
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));
            Chord[] chords = objectMapper.readValue(jsonData, Chord[].class);
            for (Chord chord : chords) {
                this.chordsMap.put(chord.getName(), chord);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this.chordsMap;
    }

    @Autowired
    void buildChordStreamPipeline(StreamsBuilder streamsBuilder, GlobalKTable<String, Chord> globalChordTable) {

//        Serde<Keystroke> keystrokeSerde = new JsonSerde<>(Keystroke.class);
//        Serde<List<String>> listSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(List.class));
//
//        KStream<String, Keystroke> midiEventsStream = streamsBuilder.stream(inputTopic, Consumed.with(Serdes.String(), keystrokeSerde));
//
//        midiEventsStream
//                .groupByKey()
//                .aggregate(
//                        // Initialize an empty list for note names
//                        ArrayList::new,
//                        // Aggregate function to collect all note names
//                        (key, value, aggregate) -> {
//                            String noteName = value.getNoteName();
//                            if (value.getEventType().equals("Note On")) {
//                                // Add the note name for "Note On" events
//                                aggregate.add(noteName.substring(0, noteName.length() - 1));
//                            } else if (value.getEventType().equals("Note Off")) {
//                                // remove the note name from aggregate when event is note off
//                                aggregate.remove(noteName.substring(0, noteName.length() - 1));
//                            }
//                            return aggregate;
//                        },
//                        Materialized.with(Serdes.String(), listSerde)
//                )
//                .toStream()
//                .mapValues(this::getChordName)
//                .filter((key, chordName) -> chordName != null && !chordName.isEmpty()) // Filter out null chordNames
//                .to(chordsOutputTopic, Produced.with(Serdes.String(), Serdes.String()));

        Serde<Keystroke> keystrokeSerde = new JsonSerde<>(Keystroke.class);
        Serde<List<String>> listSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(List.class));
        JsonSerde<Chord> chordJsonSerde = new JsonSerde<>(Chord.class);

        KStream<String, Keystroke> midiEventsStream = streamsBuilder.stream(inputTopic, Consumed.with(Serdes.String(), keystrokeSerde));

        midiEventsStream
                .groupByKey()
                .aggregate(
                        // Initialize an empty list for note names
                        ArrayList::new,
                        // Aggregate function to collect all note names
                        (key, value, aggregate) -> {
                            String noteName = value.getNoteName();
                            if (value.getEventType().equals("Note On")) {
                                // Add the note name for "Note On" events
                                aggregate.add(noteName.substring(0, noteName.length() - 1));
                            } else if (value.getEventType().equals("Note Off")) {
                                // remove the note name from aggregate when event is note off
                                aggregate.remove(noteName.substring(0, noteName.length() - 1));
                            }
                            return aggregate;
                        },
                        Materialized.with(Serdes.String(), listSerde)
                )
                .toStream()
                .leftJoin(
                        globalChordTable,
                        (key, value) -> {
                            String valueStr = Arrays.toString(value.stream().sorted().toArray());
                            System.out.println("CHORDS: " + Arrays.toString(value.stream().sorted().toArray()));
                            return !valueStr.equals("[]") ? valueStr : null;
                        },
                        (noteNameList, chord) -> {
                            if(chord != null) {
                                System.out.println("CHORDS1: " + Arrays.toString(chord.getNotes().toArray()));
                                messagingTemplate.convertAndSend("/topic/chord-events", Objects.requireNonNull(getChordName(noteNameList, chord)));
                                return getChordName(noteNameList, chord);
                            }
                            return null;
                        }
                )
                .filter((key, chordName) -> chordName != null && !chordName.isEmpty()) // Filter out null chordNames
                .to(chordsOutputTopic, Produced.with(Serdes.String(), Serdes.String()));
    }

    private String getChordName(List<String> notes, Chord chord) {
        System.out.println(Arrays.toString(notes.toArray()));
        if (chord == null) {
            return null;
        }
        List<String> chordNotes = chord.getNotes();


        // If sizes are different, chords cannot match
        if (chordNotes.size() != notes.size()) {
            return null;
        }

        // Check if all elements in chordNotes are present in notes
        boolean match = true;
        for (String note : chordNotes) {
            if (!notes.contains(note)) {
                match = false;
                break;
            }
        }

        if (match) {
            return chord.getName();
        }
        return null;
    }

    public String getChordName(List<String> notes) {
        StringBuilder chordNames = new StringBuilder();
        for (Chord chord : chordsMap.values()) {
            List<String> chordNotes = chord.getNotes();

            // If sizes are different, chords cannot match
            if (chordNotes.size() != notes.size()) {
                continue;
            }

            // Check if all elements in chordNotes are present in notes
            boolean match = true;
            for (String note : chordNotes) {
                if (!notes.contains(note)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                // If the chord matches, append its name to chordNames
                if (!chordNames.isEmpty()) {
                    chordNames.append("/");
                }
                chordNames.append(chord.getName());
            }
        }
        return chordNames.toString();
    }
}
