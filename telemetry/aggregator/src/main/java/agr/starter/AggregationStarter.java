package agr.starter;

import agr.config.TelemetryProperties;
import agr.deserialization.SensorEventDeserializer;
import agr.serialization.EventAvroSerializer;
import agr.service.SnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class AggregationStarter {

    private final SnapshotService snapshotService;
    private final TelemetryProperties properties;
    // читает из датчиков
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    // пишем в снапшоты
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;

    private static final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    public AggregationStarter(SnapshotService snapshotService, TelemetryProperties properties) {
        this.snapshotService = snapshotService;
        this.properties = properties;

        this.consumer = new KafkaConsumer<>(getConsumerProperties());
        this.producer = new KafkaProducer<>(getProducerProperties());
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(properties.getSensors().getTopic()));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));

                int count = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    Optional<SensorsSnapshotAvro> updateRecord = snapshotService.updateState(record.value());
                    // map: если в Optional есть снапшот
                    // превращаем его в письмо (ProducerRecord) для топика снапшотов, если пусто — остаётся пусто
                    // ifPresent: если письмо есть — отправляем его продюсером
                    // если пусто (снапшот не изменился) — ничего не отправляем
                    updateRecord.map(snapshot ->
                            new ProducerRecord<String, SensorsSnapshotAvro>(
                                    properties.getSnapshots().getTopic(), snapshot)
                            ).ifPresent(producer::send);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync(offsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SensorEventAvro> record,
                                      int count, KafkaConsumer<String, SensorEventAvro> consumer) {
        offsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        // каждые 10 событий сообщаем кафке докуда дочитали:
        // если приложение упадёт, уже подтверждённые события не придут снова
        // повторятся только те, что после последнего подтверждения (не больше 10)
        if(count % 10 == 0) {
            consumer.commitAsync(
                offsets, (offsets, exception) -> {
                    if(exception != null) {
                        log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                    }
                }
            );
        }
    }

    private Properties getConsumerProperties() {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "aggregator");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "telemetry.aggregator");
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getCanonicalName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return consumerProperties;
    }

    private Properties getProducerProperties() {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.CLIENT_ID_CONFIG, "aggregator");
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventAvroSerializer.class.getCanonicalName());

        return producerProperties;
    }
}
