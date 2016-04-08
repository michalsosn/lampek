package pl.lodz.p.michalsosn.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Michał Sośnicki
 */
public final class Maps {

    private Maps() {
    }

    public static <K, U, V> Map<K, V> applyToValues(
            Map<K, U> map, Function<U, V> function
    ) {
        Map<K, V> result = new HashMap<>();
        map.forEach((key, value) -> {
            result.put(key, function.apply(value));
        });
        return result;
    }

}
