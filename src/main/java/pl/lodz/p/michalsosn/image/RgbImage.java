package pl.lodz.p.michalsosn.image;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public class RgbImage implements Image {

    private final Channel red;
    private final Channel green;
    private final Channel blue;

    public RgbImage(Channel red, Channel green, Channel blue) {
        if (COMPARE_XY.compare(red, green) != 0 || COMPARE_XY.compare(red, blue) != 0) {
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
    public void accept(ImageVisitor visitor) {
        visitor.visit(this);
    }
}
