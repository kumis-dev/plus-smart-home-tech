package clt.handler;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorEventHandler {
    SensorEventProto.TypeCase getMessageType();

    void handle(SensorEventProto event);
}
