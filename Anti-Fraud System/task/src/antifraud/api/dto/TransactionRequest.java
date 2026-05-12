package antifraud.api.dto;

import antifraud.AntiFraudApplication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull
    @Positive
    private Long amount;

    @NotBlank
    private String ip;

    @NotBlank
    private String number;

    @NotNull
    private AntiFraudApplication.Region region;

    @NotNull
    private LocalDateTime date;

}
