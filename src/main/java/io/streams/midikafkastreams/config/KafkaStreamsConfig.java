package io.streams.midikafkastreams.config;

import io.streams.midikafkastreams.dto.Chord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
public class KafkaStreamsConfig {

    private final String bootstrapAddress;

    private final String chordsDBtopic;

    public KafkaStreamsConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapAddress,
                              @Value("${kafka.input.chords.topic.name}") String chordsDBtopic) {
        this.bootstrapAddress = bootstrapAddress;
        this.chordsDBtopic = chordsDBtopic;
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "midi-streams-app");
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(PROCESSING_GUARANTEE_CONFIG, EXACTLY_ONCE_V2);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public GlobalKTable<String, Chord> chordGlobalKTable(StreamsBuilder streamsBuilder) {
        JsonSerde<Chord> chordJsonSerde = new JsonSerde<>(Chord.class);
        return streamsBuilder.globalTable(chordsDBtopic, Consumed.with(Serdes.String(), chordJsonSerde), Materialized.with(Serdes.String(), chordJsonSerde));
    }
}