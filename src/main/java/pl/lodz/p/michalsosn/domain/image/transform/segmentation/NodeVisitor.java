package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import java.util.function.Function;
import java.util.stream.BaseStream;

/**
 * @author Michał Sośnicki
 */
public interface NodeVisitor<T, S extends BaseStream<T, S>, R> {
    R visit(RegionTree<T, S>.LeafNode leafNode);
    R visit(RegionTree<T, S>.InnerNode innerNode);

    static <T, S extends BaseStream<T, S>, R> NodeVisitor<T, S, R> of(
            Function<RegionTree<T, S>.LeafNode, R> leafFunction,
            Function<RegionTree<T, S>.InnerNode, R> innerFunction
    ) {
        return new NodeVisitor<T, S, R>() {
            @Override
            public R visit(RegionTree<T, S>.LeafNode leafNode) {
                return leafFunction.apply(leafNode);
            }

            @Override
            public R visit(RegionTree<T, S>.InnerNode innerNode) {
                return innerFunction.apply(innerNode);
            }
        };
    }
}
