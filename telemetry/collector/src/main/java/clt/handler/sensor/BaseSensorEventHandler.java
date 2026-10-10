package clt.handler.sensor;

import clt.config.TelemetryProperties;
import clt.handler.SensorEventHandler;
import clt.kafka.KafkaEventClient;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {

    private final KafkaEventClient client;

    // настройки с названиями топиков
    private final TelemetryProperties properties;

    // для наследников - чтобы каждый тип превращался из прото файла в авро
    protected abstract T mapToAvro(SensorEventProto eventProto);

    @Override
    public void handle(SensorEventProto event) {

        SensorEventAvro avro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setPayload(mapToAvro(event))
                .setTimestamp(toInstant(event.getTimestamp()))
                .build();

        String topic = properties.getSensors().getTopic();

        log.info("Отправляем событие датчика {} ({}) в топик {}",
                event.getId(), event.getPayloadCase(), topic);

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
