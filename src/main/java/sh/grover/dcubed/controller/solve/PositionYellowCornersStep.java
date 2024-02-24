package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.CornerPiece;
import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Move;
import sh.grover.dcubed.util.ArrayUtil;

import java.util.List;

public class PositionYellowCornersStep extends AbstractSolveStep {

    private static final int[] YELLOW_CONNECTION_CORNER_INDICES = new int[] {
            -1, // white
            6, // red
            2, // orange
            -1, // yellow
            4, // green
            0, // blue
    };

    public PositionYellowCornersStep(Cube cube, List<Move> moves) {
        super(cube, moves);
    }

    @Override
    public void solve() {
        var unsolved = this.numUnsolvedCorners();
        if (unsolved == 0) {
            return;
        }

        if (unsolved == 4) {
            this.repositionAround(0);
        }

        var solvedSide = this.findConnectedSideOfSolvedCorner();
        for (var limit = 0; limit < 2; limit++) {
            this.repositionAround(solvedSide);

            if (this.numUnsolvedCorners() == 0) {
                return;
            }
        }

        if (this.numUnsolvedCorners() != 0) {
            throw new IllegalStateException("couldn't reposition yellow corners");
        }
    }

    private int numUnsolvedCorners() {
        var unsolved = 0;
        for (var iConn = 0; iConn < 4; iConn++) {
            if (!this.yellowConnectionMatchesExpectedCorner(iConn)) {
                unsolved++;
            }
        }
        return unsolved;
    }

    private int findConnectedSideOfSolvedCorner() {
        for (var iConn = 0; iConn < 4; iConn++) {
            if (this.yellowConnectionMatchesExpectedCorner(iConn)) {
                return iConn;
            }
        }

        throw new IllegalStateException("no corners are solved");
    }

    private void repositionAround(int connectedSideIndex) {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        var left = ArrayUtil.loopedIndex(connections, connectedSideIndex + 1).side();
        var right = ArrayUtil.loopedIndex(connections, connectedSideIndex - 1).side();

        this.clockwise(FaceColor.YELLOW);
        this.clockwise(right);
        this.counterClockwise(FaceColor.YELLOW);
        this.counterClockwise(left);
        this.clockwise(FaceColor.YELLOW);
        this.counterClockwise(right);
        this.counterClockwise(FaceColor.YELLOW);
        this.clockwise(left);
    }

    private boolean yellowConnectionMatchesExpectedCorner(int connectionIndex) {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        var connection = connections[connectionIndex];
        var prevConnection = ArrayUtil.loopedIndex(connections, connectionIndex - 1);

        var yellowFaceIndex = YELLOW_CONNECTION_CORNER_INDICES[connection.side()];
        var corner = this.cube.getCornerPiece(FaceColor.YELLOW, yellowFaceIndex);
        var expected = new CornerPiece(FaceColor.YELLOW, connection.side(), prevConnection.side());
        return corner.equals(expected);
    }
}
