package anr.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "scenarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "hub_id")
    private String hubId;
    private String name;
    // каскад», то есть цепочка, по которой одно действие переходит дальше
    // зачем: save(scenario) сохраняет всё сам по порядку:
    //   1) датчики — НЕ сохраняет, они уже должны быть в базе с DEVICE_ADDED
    //   2) сам сценарий
    //   3) его действия - это и делает cascade
    //   без него — сохранять каждое действие самому до сценария
    //   4) связки «сценарий — датчик — действие»
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, // у одного сценария много действий
            // orphanRemoval = удаление сирот; сирота — строка (id) в таблице actions
            // на которую больше никто не ссылается: ни один сценарий о ней не знает
            // поэтому включаем: при обновлении сценария старые действия убираются из Map
            // и без этой настройки они остались бы в actions лишним мусором навсегда
    orphanRemoval = true)
    // LAZY: взять из базы только сам сценарий (id, хаб, имя)
    // а вместо действий положить пустышку
    // Действия он загрузит позже, когда к ним обратимся
    // EAGER: взять сценарий и сразу его действия, пока соединение с базой открыто
    // потом оно закрывается, и подгрузить их уже нельзя

    // связка = таблица «сценарий | устройство | действие»;
    // без неё не узнать, чьё действие
    // в actions нет ни сценария, ни устройства
    @JoinTable(name = "scenario_actions",
            // joinColumns — массив столбцов для связи с нашим сценарием
            // Название во множественном числе, потому что столбцов может быть несколько
            // inverseJoinColumns — массив столбцов для связи с другой сущностью
            joinColumns = @JoinColumn(name = "scenario_id"),
            inverseJoinColumns =  @JoinColumn(name = "action_id"))
    // Почему joinColumns, а не joinColumn?
    // Потому что JoinTable ожидает список столбцов, а не один столбец
    // Даже если столбец только один, его можно указать отдельно
    // Почему не просто @JoinColumn?
    // Потому что сначала нужно указать саму промежуточную таблицу через @JoinTable
    // а внутри неё — столбцы связи. Эти параметры могут содержать несколько столбцов
    // поэтому их названия во множественном числе
    // почему не просто @JoinColumn: просто @JoinColumn = колонка в таблице actions
    // joinColumns = @JoinColumn = колонка сценария в таблице-связке scenario_actions
    //  то есть: в actions колонки scenario_id нет
    //  она есть только в scenario_actions — поэтому указываем её внутри @JoinTable

    @MapKeyJoinColumn(name = "sensor_id")
    // ключ Map (датчик) лежит в колонке sensor_id связки
    // для ключа своя аннотация, в joinColumns/inverseJoinColumns его не положить
    // они только про сценарий и действие
    // Join = по sensor_id идём в таблицу sensors и достаём датчик целиком
    // без аннотации Hibernate ищет несуществующую колонку actions_key
    // падает при сохранении
    // ключ Sensor, а не String: сразу видно, что это датчик
    // id и hubId у датчика не меняются, поэтому ключ в Map не потеряется
    @Builder.Default
    private Map<Sensor, Action> actions = new HashMap<>();

    // без каскада он сохранит само условие и отключит соединение, а действия не успеют сохраниться
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "scenario_conditions",
            joinColumns = @JoinColumn(name = "scenario_id"),
            inverseJoinColumns =  @JoinColumn(name = "condition_id"))
    @MapKeyJoinColumn(name = "sensor_id")
    @Builder.Default
    private Map<Sensor, Condition> conditions = new HashMap<>();
}
