package pl.lodz.p.michalsosn.domain.image.image;

import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.Lift;

import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * A rectangular image consisting of a number of channels.
 * @author Michał Sośnicki
 */
public interface Image extends Size2d, Lift<UnaryOperator<Channel>, Image> {

    int MIN_VALUE = 0;
    int MAX_VALUE = 255;

    <T> T accept(ImageVisitor<T> visitor);

    Map<String, Channel> getChannels();

}
