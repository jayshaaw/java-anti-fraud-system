package antifraud.api.dto;

import antifraud.AntiFraudApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TransactionResponse {

    private AntiFraudApplication.TransactionState result;

}
