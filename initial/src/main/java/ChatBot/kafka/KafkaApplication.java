package ChatBot.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.management.monitor.StringMonitor;

public class KafkaApplication {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("chatbot", message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println(("Unable to send message=[" + message + "] due to:" + ex.getMessage()));
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println(("Sent message = [" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]"));
            }
        });
    }
}
