package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestTransResponse {

    private Long amount;

    private String ip;

    private String number;

    private String region;

    private Date date;

}
