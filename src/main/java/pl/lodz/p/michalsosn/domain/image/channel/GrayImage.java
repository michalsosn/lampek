package pl.lodz.p.michalsosn.domain.image.channel;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * An image with a single channel.
 * The class is immutable.
 * @author Michał Sośnicki
 */
public final class GrayImage implements Image {

    public static final String GRAY = "gray";

    private final Channel gray;

    public GrayImage(Channel gray) {
        this.gray = gray;
    }

    public static GrayImage fromChannels(Map<String, Channel> channels) {
        List<String> expectedChannels = Collections.singletonList(GRAY);
        if (!channels.keySet().equals(new HashSet<>(expectedChannels))) {
            throw new IllegalArgumentException(
                    "Map does not contain channels " + expectedChannels
                            + " but " + channels
            );
        }

        return new GrayImage(channels.get(GRAY));
    }

    public Channel getGray() {
        return gray;
    }

    @Override
    public int getHeight() {
        return gray.getHeight();
    }

    @Override
    public int getWidth() {
        return gray.getWidth();
    }

    @Override
    public Image map(UnaryOperator<Channel> channelMapper) {
        Channel newGray = channelMapper.apply(gray);
        return new GrayImage(newGray);
    }

    @Override
    public <T> T accept(ImageVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Map<String, Channel> getChannels() {
        Map<String, Channel> channels = new HashMap<>();
        channels.put(GRAY, gray);
        return channels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GrayImage grayImage = (GrayImage) o;

        return gray.equals(grayImage.gray);
    }

    @Override
    public int hashCode() {
        return gray.hashCode();
    }

    @Override
    public String toString() {
        return "GrayImage{"
            +  "gray=" + gray
            +  '}';
    }
}
