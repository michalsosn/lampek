package pl.lodz.p.michalsosn.image.image;

import pl.lodz.p.michalsosn.image.Size2d;
import pl.lodz.p.michalsosn.image.channel.Channel;
import pl.lodz.p.michalsosn.util.Lift;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public interface Image extends Size2d, Lift<UnaryOperator<Channel>, Image> {

    <T> T accept(ImageVisitor<T> visitor);

}
