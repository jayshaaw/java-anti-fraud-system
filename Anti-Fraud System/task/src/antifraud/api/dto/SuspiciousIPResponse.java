package antifraud.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuspiciousIPResponse {
    private Long id;
    private String ip;
}
