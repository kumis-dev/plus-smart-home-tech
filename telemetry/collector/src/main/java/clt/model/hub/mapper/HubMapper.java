package clt.model.hub.mapper;

import clt.model.hub.device.DeviceAddedEvent;
import clt.model.hub.device.DeviceRemovedEvent;
import clt.model.hub.main.HubEvent;
import clt.model.hub.scenario.ScenarioAddedEvent;
import clt.model.hub.scenario.ScenarioRemovedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Mapper(componentModel = "spring")
public interface HubMapper {

    // Обычный маппинг тут не сработает,
    // потому что HubEvent — это базовый класс,
    // а payload (конкретное событие) может быть разным
    // (DeviceAddedEvent, ScenarioAddedEvent и т.д.)
    // Нужно делать разный маппинг в зависимости от типа
    // Поэтому мы делаем метод default
    default HubEventAvro toHubEventAvro(HubEvent event) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        switch (event) {
            case DeviceAddedEvent addedEvent -> {
                DeviceAddedEventAvro payload = toDeviceAddedAvro(addedEvent);
                return builder.setPayload(payload).build();
            }
            case DeviceRemovedEvent removedEvent -> {
                DeviceRemovedEventAvro payload = toDeviceRemovedAvro(removedEvent);
                return builder.setPayload(payload).build();
            }
            case ScenarioAddedEvent scenarioAddedEvent -> {
                ScenarioAddedEventAvro payload = toScenarioAddedAvro(scenarioAddedEvent);
                return builder.setPayload(payload).build();
            }
            case ScenarioRemovedEvent scenarioRemovedEvent -> {
                ScenarioRemovedEventAvro payload = toScenarioRemovedAvro(scenarioRemovedEvent);
                return builder.setPayload(payload).build();
            }
            default -> throw new IllegalArgumentException("Данный тип ивент хаба неизвестен: " + event.getClass());
        }
    }

    @Mapping(target = "type", source = "deviceType")
    DeviceAddedEventAvro toDeviceAddedAvro(DeviceAddedEvent event);

    DeviceRemovedEventAvro toDeviceRemovedAvro(DeviceRemovedEvent event);

    ScenarioAddedEventAvro toScenarioAddedAvro(ScenarioAddedEvent event);

    ScenarioRemovedEventAvro toScenarioRemovedAvro(ScenarioRemovedEvent event);
}
