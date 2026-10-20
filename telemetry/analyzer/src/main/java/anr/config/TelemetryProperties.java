package anr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "anr.telemetry")
public class TelemetryProperties {

    private String bootstrap;

    private Hubs hubs = new Hubs();
    private Snapshots snapshots = new Snapshots();

    @Data
    public static class Hubs {
        private String topic;
        private String groupId;
    }

    @Data
    public static class Snapshots {
        private String topic;
        private String groupId;
    }
}