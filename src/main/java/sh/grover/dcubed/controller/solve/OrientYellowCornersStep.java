package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.util.ArrayUtil;

public class OrientYellowCornersStep extends AbstractSolveStep {

    public OrientYellowCornersStep(Cube cube) {
        super(cube);
    }

    @Override
    public void solve() {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        var startingConnection = this.findLeadingUnsolvedCorner();

        if (startingConnection != -1) {
            for (var limit = 0; limit < 4; limit++) {
                this.orient(startingConnection);
                var nextUnsolvedConnection = this.findLeadingUnsolvedCorner();
                if (nextUnsolvedConnection == -1) {
                    break;
                }

                var distance = distanceAroundYellow(connections[nextUnsolvedConnection].side(), connections[startingConnection].side());
                this.rotate(FaceColor.YELLOW, distance);
            }
        }

        this.finalYellowRotation();
    }

    @Override
    public String stepId() {
        return "orientYellowCorners";
    }

    private int findLeadingUnsolvedCorner() {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        var foundUnsolved = false;

        for (int iConn = 0; iConn < connections.length; iConn++) {
            var side = this.cube.side(connections[iConn].side());
            if (side.face(Cube.TOP_LEFT) != side.face(Cube.TOP_MIDDLE)) {
                foundUnsolved = true;
                var nextSide = this.cube.side(ArrayUtil.loopedIndex(connections, iConn + 1).side());
                if (nextSide.face(Cube.TOP_LEFT) == nextSide.face(Cube.TOP_MIDDLE)) {
                    return iConn;
                }
            }
        }

        if (foundUnsolved) {
            return 0;
        } else {
            return -1;
        }
    }

    private void orient(int connectedSideIndex) {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        for (var limit = 0; limit < 4; limit++) {
            this.addMarker("yellowCorner");
            var side = connections[connectedSideIndex].side();
            this.counterClockwise(side);
            this.clockwise(FaceColor.WHITE);
            this.clockwise(side);
            this.counterClockwise(FaceColor.WHITE);

            var colors = this.cube.side(side);
            var thisCornerMatch = colors.face(Cube.TOP_LEFT) == colors.face(Cube.TOP_MIDDLE);

            var nextColors = this.cube.side(ArrayUtil.loopedIndex(connections, connectedSideIndex + 1).side());
            var nextCornerMatch = nextColors.face(Cube.TOP_RIGHT) == nextColors.face(Cube.TOP_MIDDLE);
            if (thisCornerMatch && nextCornerMatch) {
                return;
            }
        }
        throw new IllegalStateException("couldn't orient corner");
    }

    private void finalYellowRotation() {
        var edge = this.cube.side(FaceColor.RED).face(Cube.TOP_MIDDLE);
        var distance = distanceAroundYellow(FaceColor.RED, edge);
        this.rotate(FaceColor.YELLOW, distance);
    }
}
