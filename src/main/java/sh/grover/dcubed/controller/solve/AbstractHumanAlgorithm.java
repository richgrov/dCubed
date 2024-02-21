package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Move;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHumanAlgorithm implements ISolvingAlgorithm {

    /**
     * The distance of a side to another side. E.g. [RED][ORANGE] returns 2 because they are
     * opposite to each other. This only accounts for sides touching the white side.
     */
    private static final int[][] SIDE_DISTANCES = new int[][] {
            { 99, 99, 99, 99, 99, 99 },
            { 99,  0,  2, 99,  1, -1 },
            { 99,  2,  0, 99, -1,  1 },
            { 99, 99, 99, 99, 99, 99 },
            { 99, -1,  1, 99,  0,  2 },
            { 99,  1, -1, 99,  2,  0 },
    };

    private static int sideDistance(int side1, int side2) {
        var distance = SIDE_DISTANCES[side1][side2];
        if (distance == 99) {
            throw new IndexOutOfBoundsException();
        }
        return distance;
    }

    private static Cube.SideConnection getConnectingInfo(int side, int touching) {
        for (var connected : Cube.getConnections(side)) {
            if (connected.side() == touching) {
                return connected;
            }
        }
        return null;
    }

    protected Cube cube;
    protected final List<Move> moves = new ArrayList<>();

    public void whiteCross() {
        this.rotateWhiteSideBest();
    }

    protected void clockwise(int color) {
        this.moves.add(new Move(color, true));
        this.cube.rotateClockwise(color);
    }

    protected void counterClockwise(int color) {
        this.moves.add(new Move(color, false));
        this.cube.rotateCounterClockwise(color);
    }

    private void rotateWhiteSideBest() {
        var sides = this.cube.getSides();

        var distanceVote = new int[4];

        var whiteConnections = Cube.getConnections(FaceColor.WHITE);
        for (var iConn = 0; iConn < whiteConnections.length; iConn++) {
            var touch = whiteConnections[iConn];

            var sideColor = touch.side();
            var touchingEdgeIndex = touch.faces()[1];
            var touchingEdgeColor = sides[sideColor].toColors()[touchingEdgeIndex];
            var whiteSideEdge = cube.getColorOfEdgePiece(FaceColor.WHITE, FaceColor.values()[sideColor]);
            if (whiteSideEdge != FaceColor.WHITE) {
                continue;
            }

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

        for (var move = 0; move < Math.abs(bestMove); move++) {
            if (bestMove < 0) {
                this.counterClockwise(FaceColor.WHITE);
            } else {
                this.clockwise(FaceColor.WHITE);
            }
        }
    }
}
