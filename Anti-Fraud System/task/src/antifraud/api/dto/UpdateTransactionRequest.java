package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UpdateTransactionRequest {

    @NotNull
    private Long transactionId;

    @NotNull
    private String feedback;
}
