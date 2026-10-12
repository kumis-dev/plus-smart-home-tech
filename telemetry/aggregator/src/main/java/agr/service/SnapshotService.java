package agr.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {

        SensorsSnapshotAvro snapshot = snapshots.get(event.getHubId());

        if (snapshot == null) {
            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(event.getHubId())
                    .setTimestamp(event.getTimestamp())
                    .setSensorsState(new HashMap<>())
                    .build();
            snapshots.put(event.getHubId(), snapshot);
        }

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(event.getTimestamp())
                || oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        // берём из снапшота мапу данных датчиков и кладём в нее новое состояние (данные) этого датчика
        snapshot.getSensorsState().put(event.getId(), newState);

        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshot);
    }
}
