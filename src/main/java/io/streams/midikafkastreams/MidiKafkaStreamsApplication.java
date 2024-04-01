package io.streams.midikafkastreams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

@SpringBootApplication
@EnableKafkaStreams
public class MidiKafkaStreamsApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(MidiKafkaStreamsApplication.class, args);

		// Get the MIDI keyboard listener bean
		MidiKeyboardListener midiListener = context.getBean(MidiKeyboardListener.class);

		// Set up MIDI listener
		try {

			MidiSystem.getTransmitter().setReceiver(midiListener);
			System.out.println("TO start Listening for MIDI messages. trigger /start");

		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}

}
