package antifraud.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class FeedbackLimits {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    private String number;

    private Long allowed;

    private Long manual;

    private Long prohibited;

    private Long maxAllowedAmount;

    private Long maxManualAmount;

    public FeedbackLimits() {
    }

    public FeedbackLimits(String number, Long allowed, Long manual, Long prohibited, Long maxAllowedAmount, Long maxManualAmount) {
        this.number = number;
        this.allowed = allowed;
        this.manual = manual;
        this.prohibited = prohibited;
        this.maxAllowedAmount = maxAllowedAmount;
        this.maxManualAmount = maxManualAmount;
    }
}
