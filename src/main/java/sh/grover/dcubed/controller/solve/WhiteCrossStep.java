package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;

public class WhiteCrossStep extends AbstractSolveStep {

    /**
     * The distance of a side to another side. E.g. [RED][ORANGE] returns 2 because they are
     * opposite to each other. This only accounts for sides touching the white side. It can also
     * work for the yellow side indexing the faces backwards.
     */
    private static final int[][] SIDE_DISTANCES = new int[][] {
            { 99, 99, 99, 99, 99, 99 },
            { 99,  0,  2, 99,  1, -1 },
            { 99,  2,  0, 99, -1,  1 },
            { 99, 99, 99, 99, 99, 99 },
            { 99, -1,  1, 99,  0,  2 },
            { 99,  1, -1, 99,  2,  0 },
    };

    private final boolean[] inWhiteCross = new boolean[6];

    private static int sideDistance(int side1, int side2) {
        var distance = SIDE_DISTANCES[side1][side2];
        if (distance == 99) {
            throw new IndexOutOfBoundsException("can't find distance between " + side1 + " and " + side2);
        }
        return distance;
    }

    public WhiteCrossStep(Cube cube) {
        super(cube);
    }

    @Override
    public void solve() {
        this.rotateWhiteSideBest();
        this.ensureWhiteEdgesCorrect();

        var i = 0;
        while (!this.whiteCrossComplete()) {
            this.rotateSideTopWhiteEdges();
            this.rotateSideSideWhiteEdges();
            this.rotateSideBottomWhiteEdges();
            this.rotateWhiteEdgesOnYellow();

            if (i++ == 4) {
                throw new IllegalStateException("couldn't form white cross");
            }
        }
    }

    private void rotateWhiteSideBest() {
        var sides = this.cube.getSides();

        var distanceVote = new int[4];

        for (var touch : Cube.getConnections(FaceColor.WHITE)) {
            var whiteSideEdge = cube.getColorOfEdgePiece(FaceColor.WHITE, touch.side());
            if (whiteSideEdge != FaceColor.WHITE) {
                continue;
            }

            var touchingEdgeIndex = touch.faces()[1];
            var touchingEdgeColor = sides[touch.side()].toColors()[touchingEdgeIndex];

            var distance = sideDistance(touch.side(), touchingEdgeColor);
            distanceVote[distance + 1]++; // distances are [-1, 2], so +1 to normalize that to [0, 3]
        }

        var highestVotes = 0;
        var bestMove = 0;
        for (var iDist = 0; iDist < distanceVote.length; iDist++) {
            var numVotes = distanceVote[iDist];
            if (numVotes > highestVotes) {
                highestVotes = numVotes;
                bestMove = iDist - 1; // -1 to undo the normalization above
            }
        }

        this.rotate(FaceColor.WHITE, bestMove);
    }

    private void ensureWhiteEdgesCorrect() {
        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            var sides = this.cube.getSides();
            var whiteSideEdge = cube.getColorOfEdgePiece(FaceColor.WHITE, connection.side());
            if (whiteSideEdge != FaceColor.WHITE) {
                continue;
            }

            var connectedEdgeIndex = connection.faces()[1];
            var connectedEdge = sides[connection.side()].toColors()[connectedEdgeIndex];
            var distance = sideDistance(connection.side(), connectedEdge);
            if (distance == 0) {
                this.inWhiteCross[connection.side()] = true;
                continue;
            }

            this.rotate(connection.side(), 2);
            this.rotate(FaceColor.YELLOW, -distance);
            this.rotate(connectedEdge, 2);
            this.inWhiteCross[connectedEdge] = true;
        }
    }

    private void rotateSideTopWhiteEdges() {
        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            var sides = this.cube.getSides();
            var centerTop = sides[connection.side()].toColors()[1];
            if (centerTop != FaceColor.WHITE) {
                continue;
            }

            var targetSide = this.cube.getColorOfEdgePiece(FaceColor.YELLOW, connection.side());
            var distance = sideDistance(targetSide, connection.side()); // sides are backwards because we're on yellow
            var connectedSide = connection.side();
            if (distance == 0 || distance == 2) {
                this.rotate(FaceColor.YELLOW, 1);
                distance--;
                connectedSide = Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, connectedSide, 1).side();
            }

            var rotationDirection = distance < 0 ? 1 : -1;

            this.rotate(connectedSide, rotationDirection);
            this.rotate(targetSide, -rotationDirection);
            if (this.inWhiteCross[connectedSide]) {
                this.rotate(connectedSide, -rotationDirection);
            }
            this.inWhiteCross[targetSide] = true;
        }
    }

    private void rotateSideSideWhiteEdges() {
        final var SIDES = new int[] { 7, 3 }; // left, right
        final var ROTATE = new int[] { 1, -1 };

        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            for (var direction = 0; direction < 2; direction++) {
                var sides = this.cube.getSides();
                var colors = sides[connection.side()].toColors();

                var side = SIDES[direction];
                var oppositeSide = SIDES[(direction + 1) % SIDES.length];
                var rotation = ROTATE[direction];
                var oppositeRotation = ROTATE[(direction + 1) % ROTATE.length];

                if (colors[side] != FaceColor.WHITE) {
                    continue;
                }

                var connectedSideColor =
                        Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, connection.side(), rotation)
                                .side();

                var edgeColor = sides[connectedSideColor].toColors()[oppositeSide];

                if (connectedSideColor == edgeColor) {
                    this.rotate(connectedSideColor, rotation);
                } else {
                    this.rotate(connectedSideColor, oppositeRotation);

                    var distance = sideDistance(edgeColor, connectedSideColor);
                    this.rotate(FaceColor.YELLOW, distance);

                    if (this.inWhiteCross[connectedSideColor]) {
                        this.rotate(connectedSideColor, rotation);
                    }
                    this.rotate(edgeColor, 2);
                }
                this.inWhiteCross[edgeColor] = true;
            }
        }
    }

    private void rotateSideBottomWhiteEdges() {
        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            var sides = this.cube.getSides();
            var centerBottom = sides[connection.side()].toColors()[5];
            if (centerBottom != FaceColor.WHITE) {
                continue;
            }

            var targetColor = this.cube.getColorOfEdgePiece(FaceColor.WHITE, connection.side());

            var sideToLeft = Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, connection.side(), 1)
                    .side();

            var sideToRight = Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, connection.side(), -1)
                    .side();

            if (targetColor == sideToLeft) {
                this.clockwise(connection.side());
                this.counterClockwise(sideToLeft);
                this.inWhiteCross[targetColor] = true;
                continue;
            } else if (targetColor == sideToRight) {
                this.counterClockwise(connection.side());
                this.clockwise(sideToRight);
                this.inWhiteCross[targetColor] = true;
                continue;
            }

            this.clockwise(connection.side());
            this.counterClockwise(sideToLeft);
            var distance = sideDistance(targetColor, sideToLeft);
            this.rotate(FaceColor.YELLOW, distance);
            if (this.inWhiteCross[sideToLeft]) {
                this.clockwise(sideToLeft);
            }
            this.rotate(targetColor, 2);
            this.inWhiteCross[targetColor] = true;
        }
    }

    private void rotateWhiteEdgesOnYellow() {
        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            var sides = this.cube.getSides();

            var connectingYellow = this.cube.getColorOfEdgePiece(FaceColor.YELLOW, connection.side());
            if (connectingYellow != FaceColor.WHITE) {
                continue;
            }

            var targetColor = sides[connection.side()].toColors()[1];
            var currentColor = connection.side();
            var distance = sideDistance(targetColor, currentColor);
            this.rotate(FaceColor.YELLOW, distance);
            this.rotate(targetColor, 2);
            this.inWhiteCross[targetColor] = true;
        }
    }

    private boolean whiteCrossComplete() {
        var sides = new int[] { FaceColor.RED, FaceColor.ORANGE, FaceColor.GREEN, FaceColor.BLUE };
        for (var side : sides) {
            if (!this.inWhiteCross[side]) {
                return false;
            }
        }
        return true;
    }
}