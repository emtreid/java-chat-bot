package ChatBot;


import ChatBot.kafka.Messages.AccountRequest;
import ChatBot.kafka.Messages.AccountResponse;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class Application {


    public static void main(String[] args) {
        //new SpringApplicationBuilder(Application.class).run(args);

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        MessageProducer producer = context.getBean(MessageProducer.class);
        MessageListener listener = context.getBean(MessageListener.class);
        //SpringApplication.run(Application.class, args);

        producer.sendMessage("Hello, World!");
    }


    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

        };
    }

    //    @Value("${rt.server.host}")
    //private String host = "http://localhost";

    //    @Value("${rt.server.port}")
    //private Integer port = 3010;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        //config.setHostname(host);
        config.setPort(4001);
        return new SocketIOServer(config);
    }

    @Bean
    public MessageProducer messageProducer() {
        return new MessageProducer();
    }

    @Bean
    public MessageListener messageListener() {
        return new MessageListener();
    }


    public static class MessageProducer {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        @Autowired
        private KafkaTemplate<String, AccountRequest> accountRequestKafkaTemplate;

        @Value(value = "${message.topic.name}")
        private String topicName;

        @Value(value = "${accountRequest.topic.name}")
        private String accountRequestTopicName;

        @Value(value = "${partitioned.topic.name}")
        private String partitionedTopicName;

        @Value(value = "${filtered.topic.name}")
        private String filteredTopicName;

        public void sendMessage(String message) {

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);

            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                @Override
                public void onSuccess(SendResult<String, String> result) {
                    System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata()
                            .offset() + "]");
                }

                @Override
                public void onFailure(Throwable ex) {
                    System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
                }
            });
        }

        public void sendMessageToPartition(String message, int partition) {
            kafkaTemplate.send(partitionedTopicName, partition, null, message);
        }

        public void sendMessageToFiltered(String message) {
            kafkaTemplate.send(filteredTopicName, message);
        }

        public void sendAccountRequestMessage(AccountRequest accountRequest) {
            System.out.println("SEND ACCOUNT REQUEST MESSAGE");
            System.out.println(accountRequestTopicName);
            ListenableFuture<SendResult<String, AccountRequest>> future = accountRequestKafkaTemplate.send(accountRequestTopicName, accountRequest);
            System.out.println(future.toString());
            future.addCallback(new ListenableFutureCallback<SendResult<String, AccountRequest>>() {
                @Override
                public void onSuccess(SendResult<String, AccountRequest> result) {
                    System.out.println("Sent request for account info for username=[" + accountRequest.getBody() + "] with offset=[" + result.getRecordMetadata()
                            .offset() + "]");
                }

                @Override
                public void onFailure(Throwable ex) {
                    System.out.println("Unable to send request for username=[" + accountRequest.getBody() + "] due to : " + ex.getMessage());
                }
            });
        }
    }

    public static class MessageListener {

        @Autowired
        ChatBot chatBot;

        @Autowired
        SocketIOServer server;

        public CountDownLatch latch = new CountDownLatch(3);

        public CountDownLatch partitionLatch = new CountDownLatch(2);

        public CountDownLatch filterLatch = new CountDownLatch(2);

        public CountDownLatch AccountInfoLatch = new CountDownLatch(1);

        private CountDownLatch accountRequestLatch = new CountDownLatch(1);

        @KafkaListener(topics = "${message.topic.name}", groupId = "foo", containerFactory = "fooKafkaListenerContainerFactory")
        public void listenGroupFoo(String message) {
            System.out.println("Received Message in group 'foo': " + message);
            latch.countDown();
        }

//        @KafkaListener(topics = "${message.topic.name}", groupId = "bar", containerFactory = "barKafkaListenerContainerFactory")
//        public void listenGroupBar(String message) {
//            System.out.println("Received Message in group 'bar': " + message);
//            latch.countDown();
//        }

        @KafkaListener(topics = "${message.topic.name}", containerFactory = "headersKafkaListenerContainerFactory")
        public void listenWithHeaders(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
            System.out.println("Received Message: " + message + " from partition: " + partition);
            latch.countDown();
        }

        @KafkaListener(topicPartitions = @TopicPartition(topic = "${partitioned.topic.name}", partitions = {"0", "3"}), containerFactory = "partitionsKafkaListenerContainerFactory")
        public void listenToPartition(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
            System.out.println("Received Message: " + message + " from partition: " + partition);
            this.partitionLatch.countDown();
        }

        @KafkaListener(topics = "${filtered.topic.name}", containerFactory = "filterKafkaListenerContainerFactory")
        public void listenWithFilter(String message) {
            System.out.println("Received Message in filtered listener: " + message);
            this.filterLatch.countDown();
        }

        @KafkaListener(topics = "${accountResponse.topic.name}", containerFactory = "accountResponseConcurrentKafkaListenerContainerFactory")
        public void accountResponseListener(AccountResponse accountResponse) {
            System.out.println("Received account details for: " + accountResponse.getUsername());
            chatBot.addReply("accountResponse", "Your account status is: \nUsername: " + accountResponse.getUsername() +
                    "\nBalance (GBP): " + accountResponse.getGbp() + " \nBalance(BTC): " + accountResponse.getBtc() +
                    "\nIs there anything else I can help you with today?");
            String reply = chatBot.processMessage("accountResponse");
            chatBot.addReply("accountReponse", chatBot.getDefaultResponse());
            server.getBroadcastOperations().sendEvent("serverMessage", reply);
            this.accountRequestLatch.countDown();
        }

    }

}
