package clt.handler.sensor;

import clt.config.TelemetryProperties;
import clt.kafka.KafkaEventClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Component
public class TemperatureSensorEventHandler extends BaseSensorEventHandler<TemperatureSensorAvro> {
    public TemperatureSensorEventHandler(KafkaEventClient client, TelemetryProperties properties) {
        super(client, properties);
    }

    @Override
    protected TemperatureSensorAvro mapToAvro(SensorEventProto event) {
        TemperatureSensorProto proto = event.getTemperature();
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(proto.getTemperatureC())
                .setTemperatureF(proto.getTemperatureF())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE;
    }
}
