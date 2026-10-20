package anr.processor;

import anr.config.TelemetryProperties;
import anr.deserialization.HubEventDeserializer;
import anr.service.HubEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private final TelemetryProperties properties;
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final HubEventService hubEventService;
    // оффсеты не нужны - повторы не страшны даже если событие хаба придет еще

    public HubEventProcessor(TelemetryProperties properties, HubEventService service) {
        this.properties = properties;
        this.hubEventService = service;

        this.consumer = new KafkaConsumer<>(getConsumerProperties());
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(properties.getHubs().getTopic()));

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        hubEventService.handle(record.value());
                    } catch (Exception e) {
                        log.error("Не удалось обработать событие хаба: {}", record.value(), e);
                    }
                    log.info("Получено событие хаба: {}", record.value());
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка во время обработки событий хабов", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private Properties getConsumerProperties() {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "analyzer");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getHubs().getGroupId());
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrap());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class.getCanonicalName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return consumerProperties;
    }
}
