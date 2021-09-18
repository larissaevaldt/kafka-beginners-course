package com.github.larissajusto.kafka.tutorial1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerDemo {

    public static void main(String[] args) {
        String bootstrapServers = "127.0.0.1:9092";

        // 1. CREATE THE PRODUCER PROPERTIES

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // the next properties help the producer note what type of value you're sending to Kafka and
        // how this should be serialized to bytes. Because Kafka client will convert whatever we send to Kafka into bytes (0,1)
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 2. CREATE THE PRODUCER

        // we want the Key to be a String and the Value to be a String as well. We need to pass in the properties.
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        // 3. CREATE A PRODUCER RECORD
        ProducerRecord<String, String> record =
                new ProducerRecord<String, String>("first_topic", "hello world");

        // 4. SEND DATA - asynchronous, will happen in the background

        // the send function takes a Producer record as an input
        producer.send(record);

        // wait for the data to be produced
        producer.flush(); //flush data
        producer.close(); //flush and close producer


    }
}
