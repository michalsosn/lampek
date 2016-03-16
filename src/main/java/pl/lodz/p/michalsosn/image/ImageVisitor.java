package pl.lodz.p.michalsosn.image;

import java.util.function.Consumer;

/**
 * @author Michał Sośnicki
 */
public interface ImageVisitor {

    default void visit(GrayImage grayImage) {
    }

    default void visit(RgbImage rgbImage) {
    }

    static ImageVisitor grayVisitor(Consumer<GrayImage> grayConsumer) {
        return new ImageVisitor() {
            @Override
            public void visit(GrayImage grayImage) {
                grayConsumer.accept(grayImage);
            }
        };
    }

    static ImageVisitor rgbVisitor(Consumer<RgbImage> rgbConsumer) {
        return new ImageVisitor() {
            @Override
            public void visit(RgbImage rgbImage) {
                rgbConsumer.accept(rgbImage);
            }
        };
    }

    static ImageVisitor imageVisitor(Consumer<GrayImage> grayConsumer, Consumer<RgbImage> rgbConsumer) {
        return new ImageVisitor() {

            @Override
            public void visit(GrayImage grayImage) {
                grayConsumer.accept(grayImage);
            }

            @Override
            public void visit(RgbImage rgbImage) {
                rgbConsumer.accept(rgbImage);
            }
        };
    }
}
