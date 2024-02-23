package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Move;
import sh.grover.dcubed.model.Side;

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
 * 3. Move the white sides to the correct positions based on which of the
 * following states they match:
 * 3a. There's a white face on the bottom of the side {@link this#bottomOfSide}
 * 3b. There's a white face on the top of the side {@link this#topOfSide}
 * 3c. There's a white face on the corresponding edge of the yellow {@link this#edgeOnYellow}
 * side
 * 3d. There's a white face on the left or right of the side {@link this#sideOfSide}
 */
public class WhiteCrossStep extends AbstractSolveStep {

    public WhiteCrossStep(Cube cube, List<Move> moves) {
        super(cube, moves);
    }

    @Override
    public void solve() {
        this.rotateWhiteSideBest();
        this.ensureWhiteEdgesCorrect();

        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            var sides = this.cube.getSides();
            var connectedFaces = sides[connection.side()].toColors();

            var whiteEdgeOnBottom = connectedFaces[5] == FaceColor.WHITE;
            if (whiteEdgeOnBottom) {
                this.bottomOfSide(connection);
                continue;
            }

            var whiteEdgeOnTop = connectedFaces[1] == FaceColor.WHITE;
            if (whiteEdgeOnTop) {
                this.topOfSide(connection);
                continue;
            }

            var connectingYellow = this.cube.getColorOfEdgePiece(FaceColor.YELLOW, connection.side());
            if (connectingYellow == FaceColor.WHITE) {
                this.edgeOnYellow(sides, connection);
                continue;
            }

            this.sideOfSide(connection);
        }

        for (var connection : Cube.getConnections(FaceColor.WHITE)) {
            if (!this.inWhiteCross(connection.side())) {
                throw new IllegalStateException("white cross not solved");
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

            var distance = distanceAroundWhite(touch.side(), touchingEdgeColor);
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
            // Although the piece is on white, if it's wrong, it needs to be brought up and rotated around the yellow
            // axis.
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

        this.rotate(connectedSide, rotationDirection);
        this.rotate(targetSide, -rotationDirection);
        if (this.inWhiteCross(connectedSide)) {
            this.rotate(connectedSide, -rotationDirection);
        }
    }

    private void sideOfSide(Cube.SideConnection connection) {
        final var SIDES = new int[] { 7, 3 }; // left, right
        final var ROTATE = new int[] { 1, -1 };

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

                var distance = distanceAroundYellow(connectedSideColor, edgeColor);
                this.rotate(FaceColor.YELLOW, distance);

                if (this.inWhiteCross(connectedSideColor)) {
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
            this.counterClockwise(sideToLeft);
            return;
        } else if (targetColor == sideToRight) {
            this.counterClockwise(connection.side());
            this.clockwise(sideToRight);
            return;
        }

        this.clockwise(connection.side());
        this.counterClockwise(sideToLeft);
        var distance = distanceAroundYellow(sideToLeft, targetColor);
        this.rotate(FaceColor.YELLOW, distance);
        if (this.inWhiteCross(sideToLeft)) {
            this.clockwise(sideToLeft);
        }
        this.rotate(targetColor, 2);
    }

    private void edgeOnYellow(Side[] sides, Cube.SideConnection connection) {
        var targetColor = sides[connection.side()].toColors()[1];
        var distance = distanceAroundYellow(connection.side(), targetColor);
        this.rotate(FaceColor.YELLOW, distance);
        this.rotate(targetColor, 2);
    }

    private boolean inWhiteCross(int side) {
        return this.cube.getColorOfEdgePiece(FaceColor.WHITE, side) == FaceColor.WHITE;
    }
}
