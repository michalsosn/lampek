package pl.lodz.p.michalsosn.image.image;

import pl.lodz.p.michalsosn.image.channel.Channel;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public class RgbImage implements Image {

    private final Channel red;
    private final Channel green;
    private final Channel blue;

    public RgbImage(Channel red, Channel green, Channel blue) {
        if (!red.isEqualSize(green) || !red.isEqualSize(blue)) {
            throw new IllegalArgumentException("Channels differ in size");
        }

        this.red = red;
        this.green = green;
        this.blue = blue;
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
    public <T> T accept(ImageVisitor<T> visitor) {
        return visitor.visit(this);
    }
}