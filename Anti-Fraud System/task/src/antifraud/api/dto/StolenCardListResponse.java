package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StolenCardListResponse {
    private Long id;
    private String number;
}
