package clt.model.hub.device;

import clt.model.hub.ActionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceAction {
    @NotNull
    private String sensorId;
    @NotNull
    private ActionType type;
    private Integer value;
}
