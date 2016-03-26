package pl.lodz.p.michalsosn.domain.util;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public interface Lift<From, To> {

    static <From, To extends Lift<From, To>> UnaryOperator<To> lift(From from) {
        return to -> to.map(from);
    }

    To map(From from);

}
