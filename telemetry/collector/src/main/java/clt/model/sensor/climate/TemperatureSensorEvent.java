package clt.model.sensor.climate;

import clt.model.sensor.main.SensorEvent;
import clt.model.sensor.main.SensorEventType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class TemperatureSensorEvent extends SensorEvent {
    @NotNull
    private Integer temperatureC;
    @NotNull
    private Integer temperatureF;

    @Override
    public @NonNull SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}
