package agr.deserialization;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

// для чтения событий датчиков
public class SensorEventDeserializer extends BaseAvroDeserializer<SensorEventAvro> {
    public SensorEventDeserializer() {
        super(SensorEventAvro.getClassSchema());
    }
}
