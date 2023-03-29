package antifraud.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String username;
    private String password;
    private String role;
    private String operation;

    public Users() {
    }

    public Users(String name, String username, String password, String role,
                 String operation) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.operation = operation;
    }

}
