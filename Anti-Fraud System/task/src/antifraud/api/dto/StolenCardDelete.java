package antifraud.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StolenCardDelete {
    private String status;
}
