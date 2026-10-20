package anr.processor;

import anr.config.TelemetryProperties;
import anr.deserialization.SensorsSnapshotDeserializer;
import anr.service.SnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class SnapshotProcessor {

    private final SnapshotService snapshotService;
    private final TelemetryProperties properties;
    // читает из датчиков
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private static final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    public SnapshotProcessor(SnapshotService snapshotService, TelemetryProperties properties) {
        this.snapshotService = snapshotService;
        this.properties = properties;
        this.consumer = new KafkaConsumer<>(getConsumerProperties());
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(properties.getSnapshots().getTopic()));

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(100));

                int count = 0;
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        snapshotService.analyze(record.value());
                    } catch (Exception e) {
                        log.error("Не удалось обработать снапшот: {}", record.value(), e);
                    }
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка во время обработки снапшотов", e);
        } finally {
            try {
                consumer.commitSync(offsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record,
                                      int count, KafkaConsumer<String, SensorsSnapshotAvro> consumer) {
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
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "snapshot");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getSnapshots().getGroupId());
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrap());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class.getCanonicalName());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return consumerProperties;
    }
}
