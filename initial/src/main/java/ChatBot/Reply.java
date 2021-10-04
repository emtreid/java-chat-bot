package ChatBot;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reply {
    @Getter
    private final String reply;
    @Getter
    private final List<String> topics;

    public Reply(String reply, String[] topics) {
        this.reply = reply;
        this.topics = new ArrayList<String>();
        this.topics.addAll(Arrays.asList(topics));
    }
}
