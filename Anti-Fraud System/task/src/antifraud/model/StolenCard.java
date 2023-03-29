package antifraud.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class StolenCard {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    private String number;

    public StolenCard(String number) {
        this.number = number;
    }

    public StolenCard() {
    }
}
