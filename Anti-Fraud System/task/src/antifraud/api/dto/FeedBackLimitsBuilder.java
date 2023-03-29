package antifraud.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedBackLimitsBuilder {
    private String number;

    private Long ALLOWED;

    private Long MANUAL;

    private Long PROHIBITED;
}
