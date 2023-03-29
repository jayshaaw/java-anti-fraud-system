package antifraud.model;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;

@Data
@Entity
@Getter
public class SuspiciousIP {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String ip;

    public SuspiciousIP() {
    }

    public SuspiciousIP(String ip) {
        this.ip = ip;
    }
}
