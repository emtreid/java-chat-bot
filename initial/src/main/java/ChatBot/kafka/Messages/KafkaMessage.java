package ChatBot.kafka.Messages;

import lombok.Getter;
import lombok.Setter;

public interface KafkaMessage<E> {
    String getHeader();

    E getBody();
}
