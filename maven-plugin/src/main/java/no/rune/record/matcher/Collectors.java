package no.rune.record.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collector;

final class Collectors {

    static <T, K> Collector<T, ?, Map<K, List<T>>> multiGroupingBy(Collection<? extends K> groups, BiPredicate<? super K, ? super T> isGroupMember) {
        return multiGroupingBy(groups, isGroupMember, Function.identity());
    }

    static <T, K, K2> Collector<T, ?, Map<K2, List<T>>> multiGroupingBy(
            Collection<? extends K> groups,
            BiPredicate<? super K, ? super T> isGroupMember,
            Function<? super K, ? extends K2> groupMapper) {

        return multiGroupingBy(t -> groups.stream().filter(group -> isGroupMember.test(group, t)).map(groupMapper).toList());
    }

    static <T, K> Collector<T, ?, Map<K, List<T>>> multiGroupingBy(Function<? super T, Collection<? extends K>> multiClassifier) {
        return Collector.of(
                LinkedHashMap::new,
                (map, value) -> multiClassifier.apply(value)
                        .forEach(key -> map.computeIfAbsent(key, __ -> new ArrayList<>()).add(value)),
                (map1, map2) -> {
                    map2.forEach((key, values) -> map1.computeIfAbsent(key, __ -> new ArrayList<>()).addAll(values));
                    return map1;
                });
    }

    private Collectors() {
    }
}
