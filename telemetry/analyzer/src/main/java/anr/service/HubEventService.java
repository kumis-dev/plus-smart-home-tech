package anr.service;

import anr.model.Action;
import anr.model.Condition;
import anr.model.Scenario;
import anr.model.Sensor;
import anr.repository.ScenarioRepository;
import anr.repository.SensorRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    @Transactional
    public void handle(HubEventAvro event) {
        // что это: id хаба
        // нужен каждому методу (записать датчик с хабом, искать в этом хабе)
        // почему отдельно: лежит снаружи payload, общий для всех 4 событий
        String hubId = event.getHubId();
        switch (event.getPayload()) {
            case DeviceAddedEventAvro add -> addDevice(hubId, add);
            case DeviceRemovedEventAvro remove -> removeDevice(hubId, remove);
            case ScenarioAddedEventAvro scenarioAdd -> scenarioAdd(hubId, scenarioAdd);
            case ScenarioRemovedEventAvro scenarioRemove -> scenarioRemove(hubId, scenarioRemove);
            default -> log.warn("Неизвестное событие: {}", event.getPayload());
        }
    }

    private void addDevice(String hubId, DeviceAddedEventAvro event) {
        // проверяем: есть ли в базе датчик с таким id — неважно, в каком хабе
        // почему без айди хаба, а только с айди сенсора
        // в таблице sensors один id может быть только один раз на всю таблицу
        // поэтому если такой id уже есть хоть у кого-то
        // 2ой такой же датчик записать нельзя
        // есть - ничего не делаем
        // Если добавляется новое устройство и запись уже есть в бд
        // сообщение о добавлении можно проигнорировать
        // иначе save перезаписал бы чужой датчик и он «уехал» бы в другой хаб из чужих сценариев
        if (sensorRepository.existsById(event.getId())) {
            return;
        }

        Sensor sensor = Sensor.builder()
                .id(event.getId())
                .hubId(hubId)
                .build();
        sensorRepository.save(sensor);
    }

    private void removeDevice(String hubId, DeviceRemovedEventAvro event) {
        sensorRepository.deleteByIdAndHubId(event.getId(), hubId);
    }

    private void scenarioAdd(String hubId, ScenarioAddedEventAvro event) {
        Optional<Scenario> sc = scenarioRepository.findByHubIdAndName(hubId, event.getName());
        if (sc.isPresent()) {
            sc.ifPresent(scenarioRepository::delete);
            // flush чтобы в бд удаление попало раньше вставки
            scenarioRepository.flush();
            // Если коробку не опустошить - orElseGet вернёт удалённый сценарий
            // а билдер не вызовется. После Optional.empty() коробка пустая
            // orElseGet всегда собирает новый
            sc = Optional.empty();
        }

        Scenario scenario = sc.orElseGet(() -> Scenario.builder()
                    .hubId(hubId)
                    .name(event.getName())
                    .conditions(event.getConditions().stream().collect(Collectors.toMap(
                            condition -> sensorRepository
                                    .findByIdAndHubId(condition.getSensorId(), hubId)
                                    .orElseThrow(), this::toCondition)
                    ))
                    .actions(event.getActions().stream().collect(Collectors.toMap(
                            action -> sensorRepository
                                    .findByIdAndHubId(action.getSensorId(), hubId)
                                    .orElseThrow(), this::toAction)
                    ))
                    .build());
        scenarioRepository.save(scenario);
    }

    private void scenarioRemove(String hubId, ScenarioRemovedEventAvro event) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenarioRepository::delete);
    }

    private Condition toCondition(ScenarioConditionAvro scenario) {
        Condition condition = Condition.builder()
                .type(scenario.getType().name())
                .value(scenario.getValue() instanceof Boolean
                        flag ? (flag ? 1:0) : (Integer) scenario.getValue())
                .operation(scenario.getOperation().name())
                .build();
        return condition;
    }

    private Action toAction(DeviceActionAvro actionAvro) {
        Action action = Action.builder()
                .type(actionAvro.getType().name())
                .value(actionAvro.getValue())
                .build();
        return action;
    }
}
