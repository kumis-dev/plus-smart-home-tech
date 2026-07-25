package clt.model.sensor;

import clt.model.sensor.main.SensorEvent;
import clt.model.sensor.main.SensorEventType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class MotionSensorEvent extends SensorEvent {
    @NotNull
    private Integer linkQuality;
    @NotNull
    private Boolean motion;
    @NotNull
    private Integer voltage;

    @Override
    public @NonNull SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}
