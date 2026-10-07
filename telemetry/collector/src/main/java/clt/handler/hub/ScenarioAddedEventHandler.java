package clt.handler.hub;

import clt.config.TelemetryProperties;
import clt.kafka.KafkaEventClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    public ScenarioAddedEventHandler(KafkaEventClient client, TelemetryProperties properties) {
        super(client, properties);
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto proto = event.getScenarioAdded();
        return ScenarioAddedEventAvro.newBuilder()
                .setName(proto.getId())
                .setConditions(proto.getConditionsList().stream()
                        .map(this::toConditionAvro)
                        .toList())
                .setActions(proto.getActionsList().stream()
                        .map(this::toActionAvro)
                        .toList())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    private ScenarioConditionAvro toConditionAvro(ScenarioConditionProto proto) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(ConditionTypeAvro.valueOf(proto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(proto.getOperation().name()))
                .setValue(switch (proto.getValueCase()) {
                    // смотрим на ЯРЛЫК: что заполнено?
                    case INT_VALUE -> proto.getIntValue();
                    // там число -> достаём ЧИСЛО (например 500)
                    case BOOL_VALUE -> proto.getBoolValue();
                    // там да/нет -> достаём true/false
                    case VALUE_NOT_SET -> null;
                    // пусто -> null
                })
                .build();
    }

    private DeviceActionAvro toActionAvro(DeviceActionProto proto) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(ActionTypeAvro.valueOf(proto.getType().name()))  // enum по имени
                .setValue(proto.hasValue() ? proto.getValue() : null)     // optional: нет значения -> null
                .build();
    }
}
