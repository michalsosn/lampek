package pl.lodz.p.michalsosn.image.image;

import pl.lodz.p.michalsosn.image.channel.Channel;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public class GrayImage implements Image {

    private final Channel gray;

    public GrayImage(Channel gray) {
        this.gray = gray;
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
}
