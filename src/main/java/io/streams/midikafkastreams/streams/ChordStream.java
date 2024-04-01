package io.streams.midikafkastreams.streams;

import io.streams.midikafkastreams.dto.Keystroke;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChordStream {

    private final String chordsTopic;
    Logger logger = LoggerFactory.getLogger(ChordStream.class);

    private final KafkaStreamsConfiguration kStreamsConfig;

    private final String inputTopic;

    @Autowired
    public ChordStream(@Qualifier("defaultKafkaStreamsConfig") KafkaStreamsConfiguration kStreamsConfig,
                       @Value("${kafka.input.topic.name}") String inputTopic,
                       @Value("${kafka.output.chord.topic.name}") String chordsTopic) {
        this.kStreamsConfig = kStreamsConfig;
        this.inputTopic = inputTopic;
        this.chordsTopic = chordsTopic;
    }

    @Autowired
    void buildChordStreamPipeline(StreamsBuilder streamsBuilder) {

        Serde<Keystroke> keystrokeSerde = new JsonSerde<>(Keystroke.class);
        Serde<List<String>> listSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(List.class));

        KStream<String, Keystroke> midiEventsStream = streamsBuilder.stream(inputTopic, Consumed.with(Serdes.String(), keystrokeSerde));


    }

}
