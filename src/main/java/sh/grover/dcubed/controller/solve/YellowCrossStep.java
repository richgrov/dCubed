package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.util.ArrayUtil;

public class YellowCrossStep extends AbstractSolveStep {

    private static final boolean[] NO_YELLOW = new boolean[] { false, false, false, false };
    private static final boolean[] YELLOW_L = new boolean[] { true, true, false, false };
    private static final boolean[] YELLOW_LINE = new boolean[] { true, false, true, false };
    private static final boolean[] YELLOW_SOLVED = new boolean[] { true, true, true, true };

    public YellowCrossStep(Cube cube) {
        super(cube);
    }

    @Override
    public void solve() {
        if (this.findYellowPattern(NO_YELLOW) != -1) {
            this.permute(0);
        }

        var lConnection = this.findYellowPattern(YELLOW_L);
        if (lConnection != -1) {
            this.permute(lConnection + 2);
        }

        var lineConnection = this.findYellowPattern(YELLOW_LINE);
        if (lineConnection != -1) {
            this.permute(lineConnection);
        }

        if (this.findYellowPattern(YELLOW_SOLVED) == -1) {
            throw new IllegalStateException("couldn't solve yellow cross");
        }
    }

    @Override
    public String stepId() {
        return "yellowCross";
    }

    private int findYellowPattern(boolean[] pattern) {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        outer: for (var iConn = 0; iConn < connections.length; iConn++) {
            for (var iExpected = 0; iExpected < pattern.length; iExpected++) {
                var side = ArrayUtil.loopedIndex(connections, iConn + iExpected).side();
                var isYellow = this.cube.getColorOfEdgePiece(FaceColor.YELLOW, side) == FaceColor.YELLOW;
                if (isYellow != pattern[iExpected]) {
                    continue outer;
                }
            }
            return iConn;
        }

        return -1;
    }

    private void permute(int rightSideConnectionIndex) {
        var yellowConnections = Cube.getConnections(FaceColor.YELLOW);
        var leftSide = ArrayUtil.loopedIndex(yellowConnections, rightSideConnectionIndex + 1).side();
        var rightSide = ArrayUtil.loopedIndex(yellowConnections, rightSideConnectionIndex).side();

        this.clockwise(leftSide);
        this.clockwise(rightSide);
        this.clockwise(FaceColor.YELLOW);
        this.counterClockwise(rightSide);
        this.counterClockwise(FaceColor.YELLOW);
        this.counterClockwise(leftSide);
    }
}
