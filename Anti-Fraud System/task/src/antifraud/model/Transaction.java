package antifraud.model;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Getter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private Long amount;

    @NotNull
    private String ip;

    @NotNull
    private String number;
    private String region;
    private LocalDateTime date;
    private String result;
    private String feedback;

    public Transaction() {
    }

    public Transaction(Long amount, String ip, String number, String region,
                       LocalDateTime date, String result, String feedback) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = result;
        this.feedback = feedback;
    }
}
