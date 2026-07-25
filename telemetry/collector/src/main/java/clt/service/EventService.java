package clt.service;

import clt.config.TelemetryProperties;
import clt.kafka.KafkaEventClient;
import clt.model.hub.main.HubEvent;
import clt.model.hub.mapper.HubMapper;
import clt.model.sensor.main.SensorEvent;
import clt.model.sensor.mapper.SensorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final HubMapper hubMapper;
    private final SensorMapper sensorMapper;
    private final KafkaEventClient client;
    private final TelemetryProperties properties;

    public void addHubEvent(HubEvent hubEvent) {
        log.info("Обрабатываем HubEvent: {}", hubEvent.getType());
        HubEventAvro hubEventAvro = hubMapper.toHubEventAvro(hubEvent);

        // Берём топик внутри метода, потому что при инициализации поля зависимости ещё не внедрены
        String hubTopic = properties.getHubs().getTopic();

        log.info("Отправляем в топик: {}", hubTopic);
        client.getProducer().send(new ProducerRecord<>(hubTopic, hubEventAvro));
    }

    public void addSensorEvent(SensorEvent sensorEvent) {
        log.info("Обрабатываем SensorEvent: {}", sensorEvent.getType());
        SensorEventAvro sensorEventAvro = sensorMapper.toSensorEventAvro(sensorEvent);

        String sensorTopic = properties.getSensors().getTopic();

        log.info("Отправляем в топик: {}", sensorTopic);
        client.getProducer().send(new ProducerRecord<>(sensorTopic, sensorEventAvro));
    }

}
