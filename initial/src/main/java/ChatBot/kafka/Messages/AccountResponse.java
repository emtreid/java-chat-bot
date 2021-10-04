package ChatBot.kafka.Messages;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccountResponse {

    private String header;

    private String username;

    private String gbp;

    private String btc;

    public AccountResponse(String username, long gbp, long btc) {
        header = "AccountResponse";
        this.username = username;
        BigDecimal bigdecgbp = new BigDecimal(gbp).divide(new BigDecimal(10000), RoundingMode.HALF_UP);
        BigDecimal bigdecbtc = new BigDecimal(btc).divide(new BigDecimal(100), RoundingMode.HALF_UP);
        this.gbp = String.valueOf(bigdecgbp);
        this.btc = String.valueOf(bigdecbtc);
    }
}
