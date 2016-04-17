package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import pl.lodz.p.michalsosn.domain.util.Record;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.ToIntFunction;
import java.util.stream.BaseStream;

/**
 * @author Michał Sośnicki
 */
public final class RegionTree<T, S extends BaseStream<T, S>> {

    private final RegionClassifier<T, S> classifier;
    private final BinaryOperator<S> merger;
    private final Node<T, S> root;
    private final SplitRegion<T, S> rootRegion;

    public RegionTree(RegionClassifier<T, S> classifier, BinaryOperator<S> merger,
                      SplitRegion<T, S> rootRegion) {
        this.classifier = classifier;
        this.merger = merger;
        this.rootRegion = rootRegion;
        this.root = makeNode(rootRegion);
    }

    private Node<T, S> makeNode(SplitRegion<T, S> region) {
        if (classifier.checkUniform(region)) {
            return new LeafNode(region);
        } else {
            return new InnerNode(
                    makeNode(region.subRegion(SubRegion.TOP_LEFT)),
                    makeNode(region.subRegion(SubRegion.TOP_RIGHT)),
                    makeNode(region.subRegion(SubRegion.BOTTOM_LEFT)),
                    makeNode(region.subRegion(SubRegion.BOTTOM_RIGHT))
            );
        }
    }

    public void merge() {
        root.merge();
    }

    public Mask[] collectMasks() {
        final Set<Record<Set<SplitRegion<T, S>>>> classes = new HashSet<>();

        root.accept(new NodeVisitor<T, S, Void>() {
            @Override
            public Void visit(LeafNode leafNode) {
                classes.add(leafNode.getRegion().getClassRegions());
                return null;
            }

            @Override
            public Void visit(InnerNode innerNode) {
                innerNode.getBottomLeft().accept(this);
                innerNode.getBottomRight().accept(this);
                innerNode.getTopLeft().accept(this);
                innerNode.getTopRight().accept(this);
                return null;
            }
        });

        return classes.stream().map(Record::get)
                .map(this::makeMask)
                .toArray(Mask[]::new);
    }

    private Mask makeMask(Set<SplitRegion<T, S>> regions) {
        int height = rootRegion.getHeight();
        int width = rootRegion.getWidth();
        boolean[][] maskArray = new boolean[height][width];

        double unique = Math.random();
        regions.forEach(region ->
                region.forEach((y, x) ->
                        maskArray[y][x] = true
                )
        );

        return new Mask(maskArray);
    }


    public interface Node<T, S extends BaseStream<T, S>> {
        void merge();

        Iterator<ClassifiedRegionSet<T, S>> iterator(RegionEdge edge);
        <R> R accept(NodeVisitor<T, S, R> visitor);
    }

    public final class LeafNode implements Node<T, S> {
        private final ClassifiedRegionSet<T, S> region;

        public LeafNode(SplitRegion<T, S> region) {
            this.region = new ClassifiedRegionSet<>(region, merger);
        }

        @Override
        public void merge() {
            // do nothing, leaf is already uniform
        }

        @Override
        public Iterator<ClassifiedRegionSet<T, S>> iterator(RegionEdge edge) {
            return new EdgeIterator<>(edge, this);
        }

        @Override
        public <R> R accept(NodeVisitor<T, S, R> visitor) {
            return visitor.visit(this);
        }

        public ClassifiedRegionSet<T, S> getRegion() {
            return region;
        }
    }

    public final class InnerNode implements Node<T, S> {
        private final Node<T, S> topLeft;
        private final Node<T, S> topRight;
        private final Node<T, S> bottomLeft;
        private final Node<T, S> bottomRight;

        public InnerNode(Node<T, S> topLeft, Node<T, S> topRight,
                         Node<T, S> bottomLeft, Node<T, S> bottomRight) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
        }

        @Override
        public void merge() {
            topLeft.merge();
            topRight.merge();
            bottomLeft.merge();
            bottomRight.merge();

            mergeVerticalEdge(bottomLeft.iterator(RegionEdge.RIGHT),
                              bottomRight.iterator(RegionEdge.LEFT));
            mergeVerticalEdge(topLeft.iterator(RegionEdge.RIGHT),
                              topRight.iterator(RegionEdge.LEFT));
            mergeHorizontalEdge(bottomLeft.iterator(RegionEdge.TOP),
                                topLeft.iterator(RegionEdge.BOTTOM));
            mergeHorizontalEdge(bottomRight.iterator(RegionEdge.TOP),
                                topRight.iterator(RegionEdge.BOTTOM));
        }

        private void mergeVerticalEdge(Iterator<ClassifiedRegionSet<T, S>> lefts,
                                       Iterator<ClassifiedRegionSet<T, S>> rights) {
            mergeEdge(lefts, rights, SplitRegion::getY, SplitRegion::getHeight);
        }

        private void mergeHorizontalEdge(Iterator<ClassifiedRegionSet<T, S>> bottoms,
                                         Iterator<ClassifiedRegionSet<T, S>> tops) {
            mergeEdge(bottoms, tops, SplitRegion::getX, SplitRegion::getWidth);
        }

        private void mergeEdge(Iterator<ClassifiedRegionSet<T, S>> lefts,
                               Iterator<ClassifiedRegionSet<T, S>> rights,
                               ToIntFunction<SplitRegion<T, S>> getPos,
                               ToIntFunction<SplitRegion<T, S>> getLength) {
            ClassifiedRegionSet<T, S> leftClass = lefts.next();
            ClassifiedRegionSet<T, S> rightClass = rights.next();
            while (true) {
                ClassifiedRegionPair<T, S> regionCandidate
                        = new ClassifiedRegionPair<>(leftClass, rightClass, merger);
                if (classifier.checkUniform(regionCandidate)) {
                    ClassifiedRegionSet.merge(leftClass, rightClass);
                }

                SplitRegion<T, S> leftSquare = leftClass.getRegion();
                SplitRegion<T, S> rightSquare = rightClass.getRegion();
                int leftEnd = getPos.applyAsInt(leftSquare)
                        + getLength.applyAsInt(leftSquare);
                int rightEnd = getPos.applyAsInt(rightSquare)
                        + getLength.applyAsInt(rightSquare);
                if (leftEnd < rightEnd) {
                    leftClass = lefts.next();
                } else if (rightEnd < leftEnd) {
                    rightClass = rights.next();
                } else if (lefts.hasNext() && rights.hasNext()) {
                    leftClass = lefts.next();
                    rightClass = rights.next();
                } else {
                    break;
                }
            }
        }

        @Override
        public Iterator<ClassifiedRegionSet<T, S>> iterator(RegionEdge edge) {
            return new EdgeIterator<>(edge, this);
        }

        @Override
        public <R> R accept(NodeVisitor<T, S, R> visitor) {
            return visitor.visit(this);
        }

        public Node<T, S> getTopLeft() {
            return topLeft;
        }

        public Node<T, S> getTopRight() {
            return topRight;
        }

        public Node<T, S> getBottomLeft() {
            return bottomLeft;
        }

        public Node<T, S> getBottomRight() {
            return bottomRight;
        }
    }

}


