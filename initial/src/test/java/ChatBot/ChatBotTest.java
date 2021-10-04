package ChatBot;

import org.junit.Assert;
import org.junit.Test;

public class ChatBotTest {

    @Test
    public void createChatBot() {
        ChatBot chatBot = new ChatBot();
    }

    @Test
    public void addRemoveTopics() {
        ChatBot chatBot = new ChatBot();
        chatBot.addTopic("orders");
        chatBot.addTopic("trades");
        Assert.assertEquals(chatBot.getTopics().size(), 2);
        chatBot.removeTopic("orders");
        Assert.assertEquals(chatBot.getTopics().size(), 1);
        Assert.assertEquals(chatBot.getTopics().get(0), "trades");
    }
}
