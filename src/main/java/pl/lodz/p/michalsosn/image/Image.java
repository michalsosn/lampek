package pl.lodz.p.michalsosn.image;

import pl.lodz.p.michalsosn.util.Lift;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public interface Image extends Size2d, Lift<UnaryOperator<Channel>, Image> {

    void accept(ImageVisitor visitor);

}
