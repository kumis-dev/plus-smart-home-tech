package clt.model.sensor;

import clt.model.sensor.main.SensorEvent;
import clt.model.sensor.main.SensorEventType;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class SwitchSensorEvent extends SensorEvent {
    @NonNull
    private Boolean state;

    @Override
    public @NonNull SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}
