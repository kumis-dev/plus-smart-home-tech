package clt.handler.hub;

import clt.config.TelemetryProperties;
import clt.handler.HubEventHandler;
import clt.kafka.KafkaEventClient;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {
    private final KafkaEventClient client;

    // настройки с названиями топиков
    private final TelemetryProperties properties;

    // для наследников - чтобы каждый тип превращался из прото файла в авро
    protected abstract T mapToAvro(HubEventProto eventProto);

    @Override
    public void handle(HubEventProto event) {

        HubEventAvro avro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setPayload(mapToAvro(event))
                .setTimestamp(toInstant(event.getTimestamp()))
                .build();

        String topic = properties.getHubs().getTopic();

        log.info("Отправляем событие хаба {} ({}) в топик {}",
                event.getHubId(), event.getPayloadCase(), topic);

        client.getProducer().send(new ProducerRecord<>(topic, avro));
        // Отправляем в Kafka.
        //   client.getProducer() — достаём отправщика
        //   new ProducerRecord<>(topic, avro)
        //   «письмо»: в ящик topic, внутри avro
        //   .send(...)  — отправить
    }

    private Instant toInstant(Timestamp timestamp) {
        // Зачем: в Protobuf время = два числа (секунды + наносекунды),
        // а в Avro время = один объект Instant. Надо перевести.
        // Откуда знаем, что в Protobuf именно секунды + наносекунды:
        // ТЗ 2 в примере createTemperatureSensorEvent собирает время так:
        // Timestamp.newBuilder().setSeconds(ts.getEpochSecond()).setNanos(ts.getNano())
        // Мы делаем обратное

        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        // «Сделай момент времени из этих секунд и наносекунд»
    }
}
