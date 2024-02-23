package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.CornerPiece;
import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.util.ArrayUtil;

public class WhiteCornersStep extends AbstractSolveStep {

    private static final int[] WHITE_CORNER_COLORS = new int[] {
            -1, // white
            Cube.TOP_LEFT, // red
            Cube.BOTTOM_RIGHT, // orange
            -1, // yellow
            Cube.TOP_RIGHT, // green
            Cube.BOTTOM_LEFT, // blue
    };

    private static final int[] YELLOW_CORNER_COLORS = new int[] {
            -1, // white
            Cube.BOTTOM_LEFT, // red
            Cube.TOP_RIGHT, // orange
            -1, // yellow
            Cube.BOTTOM_RIGHT, // green
            Cube.TOP_LEFT, // blue
    };

    public WhiteCornersStep(Cube cube) {
        super(cube);
    }

    @Override
    public void solve() {
        var connections = Cube.getConnections(FaceColor.WHITE);
        for (var iConn = 0; iConn < connections.length; iConn++) {
            var leftSideOfCorner = connections[iConn].side();
            var rightSideOfCorner = ArrayUtil.loopedIndex(connections, iConn + 1).side();

            if (this.isCornerSolved(leftSideOfCorner, rightSideOfCorner)) {
                continue;
            }

            this.findAndAlignCornerAbove(leftSideOfCorner, rightSideOfCorner);
            this.insertCornerWithCorrectRotation(leftSideOfCorner);
        }
    }

    private void findAndAlignCornerAbove(int leftSideOfCorner, int rightSideOfCorner) {
        var originSide = this.getSideOfCorner(FaceColor.WHITE, leftSideOfCorner, rightSideOfCorner, WHITE_CORNER_COLORS);
        var needsRestore = false;
        if (originSide != -1) {
            this.counterClockwise(originSide);
            needsRestore = true;
        } else {
            originSide = this.getSideOfCorner(FaceColor.YELLOW, leftSideOfCorner, rightSideOfCorner, YELLOW_CORNER_COLORS);
            if (originSide == -1) {
                throw new IllegalStateException("couldn't find white corner");
            }
        }

        var distance = sideDistance(leftSideOfCorner, originSide);
        this.rotate(FaceColor.YELLOW, distance);
        if (needsRestore) {
            if (distance == -1) {
                this.clockwise(FaceColor.YELLOW);
                this.clockwise(originSide);
                this.counterClockwise(FaceColor.YELLOW);
            } else {
                this.clockwise(originSide);
            }
        }
    }

    private void insertCornerWithCorrectRotation(int leftSideOfCorner) {
        for (var i = 0; i < 3; i++) {
            this.counterClockwise(leftSideOfCorner);
            this.counterClockwise(FaceColor.YELLOW);
            this.clockwise(leftSideOfCorner);

            var whiteFaces = this.cube.getSides()[FaceColor.WHITE].toColors();
            var correctRotation = whiteFaces[WHITE_CORNER_COLORS[leftSideOfCorner]] == FaceColor.WHITE;
            if (correctRotation) {
                return;
            }

            this.counterClockwise(leftSideOfCorner);
            this.counterClockwise(FaceColor.YELLOW);
            this.clockwise(leftSideOfCorner);
            this.clockwise(FaceColor.YELLOW);
        }

        throw new IllegalStateException("couldn't rotate corner in under 3 moves");
    }

    private int getSideOfCorner(int baseSide, int leftSideOfCorner, int rightSideOfCorner, int[] cornerLookup) {
        var target = new CornerPiece(FaceColor.WHITE, leftSideOfCorner, rightSideOfCorner);
        for (var connection : Cube.getConnections(baseSide)) {
            var corner = this.cube.getCornerPiece(baseSide, cornerLookup[connection.side()]);
            if (corner.equals(target)) {
                return connection.side();
            }
        }

        return -1;
    }

    private boolean isCornerSolved(int leftSideOfCorner, int rightSideOfCorner) {
        var sides = this.cube.getSides();
        return sides[leftSideOfCorner].toColors()[Cube.BOTTOM_RIGHT] == leftSideOfCorner &&
                sides[rightSideOfCorner].toColors()[Cube.BOTTOM_LEFT] != rightSideOfCorner;
    }
}
