package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Move;
import sh.grover.dcubed.util.ArrayUtil;

import java.util.List;

/**
 * Moves the white edge of the cube to line up with the corresponding connected
 * faces. There are several steps to this process:
 * 1. If there are any white edges already on the white side, rotate the white
 * side so as many of the white edges align with their connected sides as
 * possible. {@link this#rotateWhiteSideBest()}
 *
 * 2. If there are any white edges on the white side that are touching the wrong
 * side, move those to the correct side. {@link this#ensureWhiteEdgesCorrect()}
 *
 * 3. For each side that connects to the white side:
 * 3a. Check if there's a white face on the bottom of the side
 * 3b. Check if there's a white face on the top of the side
 * 3c. Check if there's a white face on the corresponding edge of the yellow
 * side
 * 3d. Check if there's a white face on the left or right of the side
 * If any of these find a match, fully move that side to where it belongs, and
 * return to step 3. This process repeats up to four times. Because there are
 * four white edges, it would be a bug to have to do this process more than
 * that.
 */
public class WhiteCrossStep extends AbstractSolveStep {

    public WhiteCrossStep(Cube cube, List<Move> moves) {
        super(cube, moves);
    }

    @Override
    public void solve() {
        this.rotateWhiteSideBest();
        this.ensureWhiteEdgesCorrect();

        for (var limit = 0; limit < 4; limit++) {
            for (var connection : Cube.getConnections(FaceColor.WHITE)) {
                var connectedFaces = this.cube.side(connection.side());

                if (connectedFaces.face(Cube.BOTTOM_MIDDLE) == FaceColor.WHITE) {
                    this.bottomOfSide(connection);
                    break;
                }

                if (connectedFaces.face(Cube.TOP_MIDDLE) == FaceColor.WHITE) {
                    this.topOfSide(connection);
                    break;
                }

                if (this.cube.getColorOfEdgePiece(FaceColor.YELLOW, connection.side()) == FaceColor.WHITE) {
                    this.edgeOnYellow(connection);
                    break;
                }

                this.sideOfSide(connection);
            }
        }

        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            if (!this.inWhiteCross(connection.side())) {
                throw new IllegalStateException(connection.side() + " not in white cross");
            }
        }
    }

    private void rotateWhiteSideBest() {
        var distanceVote = new int[4];

        for (var touch : Cube.getConnections(FaceColor.WHITE)) {
            var whiteSideEdge = cube.getColorOfEdgePiece(FaceColor.WHITE, touch.side());
            if (whiteSideEdge != FaceColor.WHITE) {
                continue;
            }

            var touchingEdgeIndex = touch.faces()[1];
            var touchingEdgeColor = this.cube.side(touch.side()).face(touchingEdgeIndex);

            var distance = distanceAroundWhite(touch.side(), touchingEdgeColor);
            distanceVote[distance + 1]++; // distances are [-1, 2], so +1 to normalize that to [0, 3]
        }

        var bestMove = ArrayUtil.indexOfHighest(distanceVote);
        if (bestMove != -1) {
            this.rotate(FaceColor.WHITE, bestMove - 1); // -1 to undo the normalization above
        }
    }

    private void ensureWhiteEdgesCorrect() {
        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            var whiteSideEdge = cube.getColorOfEdgePiece(FaceColor.WHITE, connection.side());
            if (whiteSideEdge != FaceColor.WHITE) {
                continue;
            }

            var connectedEdgeIndex = connection.faces()[1];
            var connectedEdge = this.cube.side(connection.side()).face(connectedEdgeIndex);

            var distance = distanceAroundYellow(connectedEdge, connection.side());
            if (distance == 0) {
                continue;
            }

            this.rotate(connection.side(), 2);
            this.rotate(FaceColor.YELLOW, distance);
            this.rotate(connectedEdge, 2);
        }
    }

    private void topOfSide(Cube.SideConnection connection) {
        var targetSide = this.cube.getColorOfEdgePiece(FaceColor.YELLOW, connection.side());
        var distance = distanceAroundYellow(connection.side(), targetSide);
        var connectedSide = connection.side();
        if (distance == 0 || distance == 2) {
            this.rotate(FaceColor.YELLOW, 1);
            distance--;
            connectedSide = Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, connectedSide, 1).side();
        }

        var rotationDirection = distance < 0 ? 1 : -1;

        var connectedNeedsRestore = this.inWhiteCross(connectedSide);
        this.rotate(connectedSide, rotationDirection);
        this.rotate(targetSide, -rotationDirection);
        if (connectedNeedsRestore) {
            this.rotate(connectedSide, -rotationDirection);
        }
    }

    private void sideOfSide(Cube.SideConnection connection) {
        final var SIDES = new int[] { Cube.MIDDLE_LEFT, Cube.MIDDLE_RIGHT };
        final var ROTATE = new int[] { 1, -1 };

        for (var direction = 0; direction < 2; direction++) {
            var colors = this.cube.side(connection.side());

            var side = SIDES[direction];
            var oppositeSide = SIDES[(direction + 1) % SIDES.length];
            var rotation = ROTATE[direction];
            var oppositeRotation = ROTATE[(direction + 1) % ROTATE.length];

            if (colors.face(side) != FaceColor.WHITE) {
                continue;
            }

            var connectedSideColor =
                    Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, connection.side(), rotation)
                            .side();

            var edgeColor = this.cube.side(connectedSideColor).face(oppositeSide);

            if (connectedSideColor == edgeColor) {
                this.rotate(connectedSideColor, rotation);
            } else {
                var connectedNeedsRestore = this.inWhiteCross(connectedSideColor);
                this.rotate(connectedSideColor, oppositeRotation);

                var distance = distanceAroundYellow(connectedSideColor, edgeColor);
                this.rotate(FaceColor.YELLOW, distance);

                if (connectedNeedsRestore) {
                    this.rotate(connectedSideColor, rotation);
                }
                this.rotate(edgeColor, 2);
            }
        }
    }

    private void bottomOfSide(Cube.SideConnection connection) {
        var targetColor = this.cube.getColorOfEdgePiece(FaceColor.WHITE, connection.side());

        var sideToLeft = Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, connection.side(), 1)
                .side();

        var sideToRight = Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, connection.side(), -1)
                .side();

        if (targetColor == sideToLeft) {
            this.clockwise(connection.side());
            this.clockwise(sideToLeft);
            return;
        } else if (targetColor == sideToRight) {
            this.counterClockwise(connection.side());
            this.counterClockwise(sideToRight);
            return;
        }

        this.clockwise(connection.side());
        var leftNeedsRestore = this.inWhiteCross(sideToLeft);
        this.counterClockwise(sideToLeft);
        var distance = distanceAroundYellow(sideToLeft, targetColor);
        this.rotate(FaceColor.YELLOW, distance);
        if (leftNeedsRestore) {
            this.clockwise(sideToLeft);
        }
        this.rotate(targetColor, 2);
    }

    private void edgeOnYellow(Cube.SideConnection connection) {
        var targetColor = this.cube.side(connection.side()).face(Cube.TOP_MIDDLE);
        var distance = distanceAroundYellow(connection.side(), targetColor);
        this.rotate(FaceColor.YELLOW, distance);
        this.rotate(targetColor, 2);
    }

    private boolean inWhiteCross(int side) {
        return this.cube.getColorOfEdgePiece(FaceColor.WHITE, side) == FaceColor.WHITE;
    }
}
