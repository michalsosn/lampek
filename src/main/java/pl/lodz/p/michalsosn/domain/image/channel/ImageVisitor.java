package pl.lodz.p.michalsosn.domain.image.channel;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Michał Sośnicki
 */
public interface ImageVisitor<T> extends Function<Image, T> {

    @Override
    default T apply(Image image) {
        return image.accept(this);
    }

    T visit(GrayImage grayImage);

    T visit(RgbImage rgbImage);

    static ImageVisitor<?> grayVisitor(
            Consumer<? super GrayImage> grayConsumer
    ) {
        return new ImageVisitor<Void>() {
            @Override
            public Void visit(GrayImage grayImage) {
                grayConsumer.accept(grayImage);
                return null;
            }

            @Override
            public Void visit(RgbImage rgbImage) {
                return null;
            }
        };
    }

    static ImageVisitor<?> rgbVisitor(Consumer<? super RgbImage> rgbConsumer) {
        return new ImageVisitor<Void>() {
            @Override
            public Void visit(GrayImage grayImage) {
                return null;
            }

            @Override
            public Void visit(RgbImage rgbImage) {
                rgbConsumer.accept(rgbImage);
                return null;
            }
        };
    }

    static <T> ImageVisitor<T> imageVisitor(
            Function<? super GrayImage, ? extends T> grayConsumer,
            Function<? super RgbImage, ? extends T> rgbConsumer
    ) {
        return new ImageVisitor<T>() {
            @Override
            public T visit(GrayImage grayImage) {
                return grayConsumer.apply(grayImage);
            }

            @Override
            public T visit(RgbImage rgbImage) {
                return rgbConsumer.apply(rgbImage);
            }
        };
    }
}
