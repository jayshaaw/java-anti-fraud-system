package antifraud.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fraud.detection")
public class FraudDetectionConfig {

    /** Maximum transaction amount considered ALLOWED by default */
    private long maxAllowedAmount = 200L;

    /** Maximum transaction amount considered MANUAL_PROCESSING by default */
    private long maxManualAmount = 1500L;

    /** Lookback window in minutes for correlation checks */
    private int correlationWindowMinutes = 60;
}
