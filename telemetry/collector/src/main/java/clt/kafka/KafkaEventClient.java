package clt.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class KafkaEventClient {

    private Producer<Void, SpecificRecordBase> producer;

    public Producer<Void, SpecificRecordBase> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        producer = new KafkaProducer<>(getConfig());
    }

    private Properties getConfig() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventAvroSerializer.class);
        return config;
    }

    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }
}
