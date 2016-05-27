package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;

import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

/**
 * @author Michał Sośnicki
 */
public class FiltersTest {

    @Test
    public void sincFilterNonCausal() throws Exception {
        //when
        TimeRange sampling = TimeRange.ofFrequency(4000);
        final Filter filter = Filters.sincFilter(sampling, 500, 9, false);

        //then
        assertThat(filter.values().boxed().collect(Collectors.toList()),
                   everyItem(lessThanOrEqualTo(filter.getValue(0))));
    }

}