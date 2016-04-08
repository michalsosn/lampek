package pl.lodz.p.michalsosn.domain.image.channel;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.image.Size2d;

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

    static Image fromChannels(Map<String, Channel> channels) {
        try {
            return GrayImage.fromChannels(channels);
        } catch (IllegalArgumentException arg) {
            return RgbImage.fromChannels(channels);
        }
    }
}
