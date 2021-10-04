package ChatBot.util;

import java.util.List;
import java.util.stream.Collectors;

public class Intersect {
    public static <E> List<E> intersect(List<E> list1, List<E> list2) {
        return list1.stream()
                .distinct()
                .filter(list2::contains)
                .collect(Collectors.toList());
    }
}
