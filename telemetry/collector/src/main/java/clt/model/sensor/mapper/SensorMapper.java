package clt.model.sensor.mapper;

import clt.model.sensor.LightSensorEvent;
import clt.model.sensor.MotionSensorEvent;
import clt.model.sensor.SwitchSensorEvent;
import clt.model.sensor.climate.ClimateSensorEvent;
import clt.model.sensor.climate.TemperatureSensorEvent;
import clt.model.sensor.main.SensorEvent;
import org.mapstruct.Mapper;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Mapper(componentModel = "spring")
public interface SensorMapper {
    // Обычный маппинг тут не сработает,
    // потому что HubEvent — это базовый класс,
    // а payload (конкретное событие) может быть разным
    // (DeviceAddedEvent, ScenarioAddedEvent и т.д.)
    // Нужно делать разный маппинг в зависимости от типа
    // Поэтому мы делаем метод default
    default SensorEventAvro toSensorEventAvro(SensorEvent event) {
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        switch (event) {
            case ClimateSensorEvent climateEvent -> {
                ClimateSensorAvro payload = toClimateSensorAvro(climateEvent);
                return builder.setPayload(payload).build();
            }
            case TemperatureSensorEvent temperatureEvent -> {
                TemperatureSensorAvro payload = toTemperatureSensorAvro(temperatureEvent);
                return builder.setPayload(payload).build();
            }
            case LightSensorEvent lightEvent -> {
                LightSensorAvro payload = toLightSensorAvro(lightEvent);
                return builder.setPayload(payload).build();
            }
            case MotionSensorEvent motionEvent -> {
                MotionSensorAvro payload = toMotionSensorAvro(motionEvent);
                return builder.setPayload(payload).build();
            }
            case SwitchSensorEvent switchEvent -> {
                SwitchSensorAvro payload = toSwitchSensorAvro(switchEvent);
                return builder.setPayload(payload).build();
            }
            default -> throw new IllegalArgumentException("Данный тип сенсора неизвестен: " + event.getClass());
        }
    }

    ClimateSensorAvro toClimateSensorAvro(ClimateSensorEvent event);

    TemperatureSensorAvro toTemperatureSensorAvro(TemperatureSensorEvent event);

    LightSensorAvro toLightSensorAvro(LightSensorEvent event);

    MotionSensorAvro toMotionSensorAvro(MotionSensorEvent event);

    SwitchSensorAvro toSwitchSensorAvro(SwitchSensorEvent event);
}
