package clt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sht.telemetry")
public class TelemetryProperties {

    private String bootstrap;

    private Sensors sensors = new Sensors();
    private Hubs hubs = new Hubs();

    @Data
    public static class Sensors {
        private String topic;
    }

    @Data
    public static class Hubs {
        private String topic;
    }
}
