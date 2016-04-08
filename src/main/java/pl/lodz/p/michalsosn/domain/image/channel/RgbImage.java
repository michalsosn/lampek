package pl.lodz.p.michalsosn.domain.image.channel;

import pl.lodz.p.michalsosn.domain.image.Size2d;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * An image with three channels of red, green and blue color.
 * The class is immutable.
 * @author Michał Sośnicki
 */
public final class RgbImage implements Image {

    private final Channel red;
    private final Channel green;
    private final Channel blue;

    public RgbImage(Channel red, Channel green, Channel blue) {
        if (!Size2d.allSameSize(red, green, blue)) {
            throw new IllegalArgumentException("Channels differ in size");
        }

        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static RgbImage fromChannels(Map<String, Channel> channels) {
        List<String> expectedChannels = Arrays.asList("red", "green", "blue");
        if (!channels.keySet().equals(new HashSet<>(expectedChannels))) {
            throw new IllegalArgumentException(
                    "Map does not contain channels " + expectedChannels
                            + " but " + channels
            );
        }

        return new RgbImage(
                channels.get("red"), channels.get("green"), channels.get("blue")
        );
    }

    public Channel getRed() {
        return red;
    }

    public Channel getGreen() {
        return green;
    }

    public Channel getBlue() {
        return blue;
    }

    @Override
    public int getHeight() {
        return red.getHeight();
    }

    @Override
    public int getWidth() {
        return red.getWidth();
    }

    @Override
    public Image map(UnaryOperator<Channel> channelMapper) {
        Channel newRed = channelMapper.apply(red);
        Channel newGreen = channelMapper.apply(green);
        Channel newBlue = channelMapper.apply(blue);

        return new RgbImage(newRed, newGreen, newBlue);
    }

    @Override
    public Map<String, Channel> getChannels() {
        Map<String, Channel> channels = new HashMap<>();
        channels.put("red", red);
        channels.put("green", green);
        channels.put("blue", blue);
        return channels;
    }

    @Override
    public <T> T accept(ImageVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RgbImage rgbImage = (RgbImage) o;

        return red.equals(rgbImage.red)
            && green.equals(rgbImage.green)
            && blue.equals(rgbImage.blue);
    }

    @Override
    public int hashCode() {
        int result = red.hashCode();
        result = 31 * result + green.hashCode();
        result = 31 * result + blue.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RgbImage{"
             + "red=" + red
             + ", green=" + green
             + ", blue=" + blue
             + '}';
    }
}
