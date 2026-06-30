package clt.model.hub.device;

import clt.model.hub.main.HubEvent;
import clt.model.hub.main.HubEventType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class DeviceRemovedEvent extends HubEvent {
    @NotBlank
    private String id;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }
}
