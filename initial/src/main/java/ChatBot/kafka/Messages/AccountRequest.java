package ChatBot.kafka.Messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @Getter
    @Setter
    private String header;

    @Getter
    @Setter
    public String body;

    public AccountRequest(String username) {
        header = "Account Request";
        body = username;
    }

    @Override
    public String toString() {
        return header + ", " + body;
    }
}
