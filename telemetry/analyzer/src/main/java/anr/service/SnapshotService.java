package anr.service;

import anr.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class SnapshotService {

    private final ScenarioRepository scenarioRepository;

    // записывать сюда ниче нельзя
    @Transactional(readOnly = true)
    public void analyze(SensorsSnapshotAvro snapshot) {
        switch (snapshot.getSensorsState()) {

            default -> log.warn("Неизвестное состояние снапшота: {}", snapshot.getSensorsState());
        }

        // для проверки условий состояний снашпотов
        // почему ключ String: у Avro map ключи всегда строки; id датчика придумывает хаб — это текст (UUID), в sensors тоже VARCHAR
        // SensorStateAvro (значение): timestamp + data — показания датчика (одного из 5 видов)
        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        // связанные с ним сценарии — сценарии этого хаба
    }
}
