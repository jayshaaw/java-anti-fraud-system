package antifraud.api.dto;

import antifraud.AntiFraudApplication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    private Long amount;

    @NotNull
    private String ip;

    @NotNull
    private String number;

    private AntiFraudApplication.Region region;

    private LocalDateTime date;

}
