package clt.handler.sensor;

import clt.config.TelemetryProperties;
import clt.kafka.KafkaEventClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

@Component
public class ClimateSensorEventHandler extends BaseSensorEventHandler<ClimateSensorAvro> {
    public ClimateSensorEventHandler(KafkaEventClient client, TelemetryProperties properties) {
        super(client, properties);   // передаём родителю то, что ему нужно
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEventProto event) {
        ClimateSensorProto proto = event.getClimateSensor();
        return ClimateSensorAvro.newBuilder()
                .setCo2Level(proto.getCo2Level())
                .setHumidity(proto.getHumidity())
                .setTemperatureC(proto.getTemperatureC())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
        // Ответ на вопрос «за какой тип ты отвечаешь?».
    }
}
