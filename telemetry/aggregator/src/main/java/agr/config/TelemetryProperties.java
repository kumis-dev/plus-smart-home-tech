package agr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agr.telemetry")
public class TelemetryProperties {

    private String bootstrap;

    private Sensors sensors = new Sensors();
    private Snapshots snapshots = new Snapshots();

    @Data
    public static class Sensors {
        private String topic;
    }

    @Data
    public static class Snapshots {
        private String topic;
    }
}
