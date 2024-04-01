package io.streams.midikafkastreams;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.streams.midikafkastreams.dto.Keystroke;
import jm.music.data.Note;
import jm.music.data.Phrase;
import jm.music.tools.ChordAnalysis;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sound.midi.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.streams.midikafkastreams.Util.getNoteName;
import static io.streams.midikafkastreams.Util.toJson;
import static jm.music.tools.ga.NormalDistributionFE.scale;

@Component
public class MidiKeyboardListener implements Receiver {

    private final Logger logger = LoggerFactory.getLogger(MidiKeyboardListener.class);

    private final KafkaProducer<String, String> kafkaProducer;

    private boolean isRecording = false;

    private final String inputTopic;

    private String songId;

    private final Map<Integer, Boolean> noteStateMap; // Map to keep track of the state of each note

    private final Synthesizer synth; // Synthesizer instance

    private final ExecutorService executorService; // Thread pool for MIDI playback
    private final Map<Integer, Long> noteStartTimeMap; // Map to keep track of start time of each note

    @Autowired
    public MidiKeyboardListener(KafkaProducer<String, String> kafkaProducer,
                                @Value("${kafka.input.topic.name}") String inputTopic) {
        this.kafkaProducer = kafkaProducer;
        this.inputTopic = inputTopic;
        this.noteStartTimeMap = new HashMap<>();
        this.executorService = Executors.newSingleThreadExecutor(); // Create a single-threaded executor
        this.noteStateMap = new HashMap<>();
        Synthesizer tempSynth = null;
        try {
            tempSynth = MidiSystem.getSynthesizer();
            tempSynth.open();
            Receiver synthReceiver = tempSynth.getReceiver();
            Transmitter transmitter = MidiSystem.getTransmitter();
            transmitter.setReceiver(synthReceiver);
            // Adjust buffer size and latency for optimal performance
            MidiChannel[] channels = tempSynth.getChannels();
            for (MidiChannel channel : channels) {
                if (channel != null) {
                    channel.controlChange(0x200, 0); // Set buffer size
                    channel.controlChange(0x201, 1); // Set latency
                }
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
        this.synth = tempSynth;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {

        if (isRecording && message instanceof ShortMessage sm) {
            int command = sm.getCommand();
            if (command == ShortMessage.NOTE_ON || command == ShortMessage.NOTE_OFF) {
                int noteNum = sm.getData1();
                int velocity = sm.getData2();
                String eventType = (command == ShortMessage.NOTE_ON) ? "Note On" : "Note Off";
                // Play sound
//                executorService.execute(() -> playSound(command, noteNum, velocity));

                // Create Keystroke object and send to Kafka
                String noteName = getNoteName(noteNum);
                long currentTimeStamp = System.nanoTime();
                Keystroke keystroke = new Keystroke(eventType, noteName, velocity, noteNum, currentTimeStamp);
                ProducerRecord<String, String> producerRecord = null;
                if (!Objects.equals(songId, "")) {
                    try {
                        producerRecord = new ProducerRecord<>(inputTopic, songId, toJson(keystroke));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    kafkaProducer.send(producerRecord);
                }
            }
        }
    }

    private int calculateTonic(int key) {
        // Map MIDI key numbers to note names
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        // Calculate tonic based on the MIDI key number
        int tonic = key % 12; // Get tonic (0-11)
        System.out.println("Tonic: " + noteNames[tonic]); // Print the tonic note name
        return tonic;
    }

    private void playSound(int command, int noteNum, int velocity) {
        if (command == ShortMessage.NOTE_ON) {
            playNoteOn(noteNum, velocity);
        }
        if (command == ShortMessage.NOTE_OFF) {
            stopNote(noteNum);
        }
    }

    private void playNoteOn(int noteNum, int velocity) {
        try {
            MidiChannel[] channels = synth.getChannels();
            channels[0].noteOn(noteNum, velocity); // Play note
            noteStateMap.put(noteNum, true); // Mark note as playing
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
    }

    private void stopNote(int noteNum) {
        Boolean isNotePlaying = noteStateMap.get(noteNum);
        if (isNotePlaying != null && isNotePlaying) { // Check if note is currently playing
            try {
                MidiChannel[] channels = synth.getChannels();
                channels[0].noteOff(noteNum); // Turn off note
                noteStateMap.put(noteNum, false); // Mark note as not playing
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        }
    }


    @Override
    public void close() {
        executorService.shutdown(); // Shutdown the executor service

        if (synth != null) {
            synth.close();
        }
        // Cleanup resources if needed
    }

    // Utility function to start or stop recording
    public String toggleRecording(boolean toggleOnOrOff) {
        System.out.println(toggleOnOrOff);
        isRecording = toggleOnOrOff;
        if (isRecording && Objects.equals(songId, "")) {
            songId = UUID.randomUUID().toString();
        } else {
            songId = "";
        }
        logger.info(String.format("Recording %s, - %s", (isRecording ? "started" : "stopped"), songId));
        return songId;
    }
}