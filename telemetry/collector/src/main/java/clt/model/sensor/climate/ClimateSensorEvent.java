package clt.model.sensor.climate;

import clt.model.sensor.main.SensorEvent;
import clt.model.sensor.main.SensorEventType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class ClimateSensorEvent extends SensorEvent {
    @NotNull
    private Integer temperatureC;
    @NotNull
    private Integer humidity;
    @NotNull
    private Integer co2Level;

    @Override
    public @NonNull SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}
