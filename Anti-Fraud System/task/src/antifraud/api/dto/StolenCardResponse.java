package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class StolenCardResponse {

    private Long id;

    private String number;
}
