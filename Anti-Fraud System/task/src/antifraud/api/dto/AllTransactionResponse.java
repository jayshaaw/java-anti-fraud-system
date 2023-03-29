package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllTransactionResponse {

    private Long transactionId;
    private Long amount;
    private String ip;
    private String number;
    private String region;
    private LocalDateTime date;
    private String result;
    private String feedback;
}
