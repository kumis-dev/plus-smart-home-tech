package clt.handler.hub;

import clt.config.TelemetryProperties;
import clt.kafka.KafkaEventClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Component
public class ScenarioRemovedEventHandler extends BaseHubEventHandler<ScenarioRemovedEventAvro> {
    public ScenarioRemovedEventHandler(KafkaEventClient client, TelemetryProperties properties) {
        super(client, properties);
    }

    @Override
    protected ScenarioRemovedEventAvro mapToAvro(HubEventProto event) {
        ScenarioRemovedEventProto proto = event.getScenarioRemoved();
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(proto.getId())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }
}
