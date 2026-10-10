package clt.handler.sensor;

import clt.config.TelemetryProperties;
import clt.kafka.KafkaEventClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;

@Component
public class LightSensorEventHandler extends BaseSensorEventHandler<LightSensorAvro> {

    public LightSensorEventHandler(KafkaEventClient client, TelemetryProperties properties) {
        super(client, properties);   // передаём родителю то, что ему нужно
    }

    @Override
    protected LightSensorAvro mapToAvro(SensorEventProto event) {
        LightSensorProto proto = event.getLightSensor();
        return LightSensorAvro.newBuilder()
                .setLinkQuality(proto.getLinkQuality())
                .setLuminosity(proto.getLuminosity())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
        // Ответ на вопрос «за какой тип ты отвечаешь?».
        // Контроллер при запуске спросит это у всех детей
        // и составит справочник: LIGHT_SENSOR -> этот объект
        // LIGHT_SENSOR — потому что в .proto поле light_sensor
    }
}
