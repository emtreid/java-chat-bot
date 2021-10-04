package ChatBot.util;

import ChatBot.Reply;

import java.sql.Array;
import java.util.Comparator;
import java.util.List;

public class LengthComparator implements Comparator<Reply> {
    public int compare(Reply reply1, Reply reply2) {
        return reply2.getTopics().size() - reply1.getTopics().size();
    }
}
