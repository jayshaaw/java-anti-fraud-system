package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuspiciousIPListResponse {

    private Long id;
    private String ip;
}
