package ChatBot;

import ChatBot.kafka.Messages.AccountRequest;
import ChatBot.util.Intersect;
import ChatBot.util.LengthComparator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ChatBot {

    private static final String reg = "[^a-zA-Z0-9\\s]";

    @Getter
    private static final String[] keywords = new String[]{"orders", "trades", "account"};

    @Getter
    private String currentUsername = "";

    @Getter
    private static final String[] greetings = new String[]{"hello", "hi", "excuse"};

    @Getter
    @Setter
    private String currentTopic = "";

    @Getter
    private List<String> topics;

    @Getter
    private final HashMap<String, String> replies;

    @Getter
    @Setter
    private Reply previousReply;

    @Getter
    @Setter
    private String nextReply = "";

    @Autowired
    Application.MessageProducer producer;

    @Autowired
    Application.MessageListener listener;

    @Getter
    private final String defaultResponse;

    public ChatBot() {
        defaultResponse = "Your account status is: Username: Unknown; Balance (GBP): Unknown; Balance(BTC): Unknown.";
        topics = new ArrayList<String>();
        replies = new HashMap<>();
        addReply("orders", "It sounds like you're having trouble with orders.");
        addReply("trades", "It sounds like you're having trouble with trades.");
        addReply("account", "It sounds like you're having trouble with your account. Would you like to know your current account status?");
        addReply("requestUsername", "Ok, I'll just need some details from you. What is the username of your account?");
        addReply("awaitingResponse", "Just a second while I get that for you.");
        addReply("accountResponse", defaultResponse);
    }

    public void addReply(String topic, String reply) {
        replies.put(topic, reply);
    }

    public void addTopic(String topic) {
        topics.add(topic);
    }

    public void resetTopics() {
        topics = new ArrayList<String>();
    }

    public void removeTopic(String topic) {
        List<String> newTopics = new ArrayList<String>();
        topics.stream().filter(t -> !t.equals(topic)).forEach(newTopics::add);
        topics = newTopics;
    }

    public boolean containsGreeting(String rawMessage) {
        rawMessage = rawMessage.replaceAll(reg, "").toLowerCase();
        List<String> message = Arrays.asList(rawMessage.split("\\s+"));
        return (message.contains("hello") || message.contains("hi"));
    }

    public String chooseReply() {
        if (currentTopic.equals("")) {
            if (topics.size() != 0) {
                currentTopic = topics.get(0);
                removeTopic(topics.get(0));
                System.out.println(topics);
            }
        }
        if (currentTopic.equals("accountResponse")) {
            String reply = replies.get(currentTopic);
            currentTopic = "";
            return reply;
        }
        if (replies.get(currentTopic) != null) return replies.get(currentTopic);
        currentTopic = "";
        return "Sorry, I don't understand. Please try to use more keywords.";
    }

    public void parseMessage(String rawMessage) throws InterruptedException {
        if (currentTopic.equals("awaitingResponse"))
            currentTopic = rawMessage;
        if (currentTopic.equals("account")) {
            parseAccountMessage(rawMessage);
        } else if (currentTopic.equals("requestUsername")) {
            parseUsername(rawMessage);
        } else {
            parseOpeningMessage(rawMessage);
        }
    }

    private void parseAccountMessage(String rawMessage) {
        rawMessage = rawMessage.replaceAll(reg, "").toLowerCase();
        List<String> message = Arrays.asList(rawMessage.split("\\s+"));
        if (message.contains("yes")) {
            currentTopic = "requestUsername";
        } else {
            currentTopic = "";
        }
    }

    private void parseUsername(String message) throws InterruptedException {
        System.out.println("Requesting details for account: " + message);
        producer.sendAccountRequestMessage(new AccountRequest(message));
        listener.latch.await(2, TimeUnit.SECONDS);
        currentTopic = "awaitingResponse";
    }

    private void parseOpeningMessage(String rawMessage) {
        rawMessage = rawMessage.replaceAll(reg, "").toLowerCase();
        List<String> message = Arrays.asList(rawMessage.split("\\s+"));
        if (message.contains("orders") || message.contains("order")) {
            addTopic("orders");
        }
        if (message.contains("trades") || message.contains("trade")) {
            addTopic("trades");
        }
        if (message.contains("account") || message.contains("login")) {
            addTopic("account");
        }
    }

    public String processMessage(String message) {
        try {
            parseMessage(message);
        } catch (Exception e) {
            System.out.println(e);
        }
        if (topics.size() == 0) {
            if (containsGreeting(message)) {
                return ("Hi, I'm Bugsy! How can I help you today?");
            }
        }
        String response = chooseReply();
        return response;
    }
}
