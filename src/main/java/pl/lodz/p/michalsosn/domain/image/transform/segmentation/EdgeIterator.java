package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.BaseStream;

/**
 * @author Michał Sośnicki
 */
public final class EdgeIterator<T, S extends BaseStream<T, S>>
        implements Iterator<ClassifiedRegionSet<T, S>> {

    private final RegionEdge edge;
    private final Deque<RegionTree.Node<T, S>> itinerary = new ArrayDeque<>();

    public EdgeIterator(RegionEdge edge, RegionTree.Node<T, S> root) {
        this.edge = edge;
        itinerary.push(root);
    }

    @Override
    public boolean hasNext() {
        return !itinerary.isEmpty();
    }

    @Override
    public ClassifiedRegionSet<T, S> next() {
        while (true) {
            RegionTree.Node<T, S> nextNode = itinerary.pop();
            Optional<ClassifiedRegionSet<T, S>> maybeNext = nextNode.accept(
                    NodeVisitor.of(
                            leaf -> Optional.of(leaf.getRegion()),
                            inner -> {
                                switch (edge) {
                                    case RIGHT:
                                        itinerary.push(inner.getTopRight());
                                        itinerary.push(inner.getBottomRight());
                                        break;
                                    case LEFT:
                                        itinerary.push(inner.getTopLeft());
                                        itinerary.push(inner.getBottomLeft());
                                        break;
                                    case TOP:
                                        itinerary.push(inner.getTopRight());
                                        itinerary.push(inner.getTopLeft());
                                        break;
                                    case BOTTOM:
                                        itinerary.push(inner.getBottomRight());
                                        itinerary.push(inner.getBottomLeft());
                                        break;
                                    default:
                                        throw new IllegalStateException(
                                                "Unknown edge " + edge
                                        );
                                }
                                return Optional.empty();
                            }
                    )
            );
            if (maybeNext.isPresent()) {
                return maybeNext.get();
            }
        }
    }

}


