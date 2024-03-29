package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.util.ArrayUtil;

public class SecondLayerStep extends AbstractSolveStep {

    public SecondLayerStep(Cube cube) {
        super(cube);
    }

    @Override
    public void solve() {
        for (var limit = 0; limit < 4; limit++) {
            var edge = this.findAndAlignEdgeOnYellow();
            if (edge == null) {
                edge = this.findAndAlignEdgeOnSide();
                if (edge == null) {
                    return;
                }
            }

            this.insertEdge(edge);
        }

    }

    @Override
    public String stepId() {
        return "secondLayer";
    }

    private PreparedEdge findAndAlignEdgeOnYellow() {
        for (Cube.SideConnection connection : Cube.getConnections(FaceColor.YELLOW)) {
            var connectedEdge = connection.faces()[1];

            var colorOnYellow = this.cube.getColorOfEdgePiece(FaceColor.YELLOW, connection.side());
            var colorOnConnected = this.cube.side(connection.side()).face(connectedEdge);
            if (colorOnYellow == FaceColor.YELLOW || colorOnConnected == FaceColor.YELLOW) {
                continue;
            }

            var distance = distanceAroundYellow(connection.side(), colorOnConnected);
            if (distance != 0) {
                this.addMarker("secondEdgeMove", colorOnYellow, colorOnConnected);
            }
            this.rotate(FaceColor.YELLOW, distance);

            var otherSideRelative = distanceAroundYellow(colorOnConnected, colorOnYellow);
            return new PreparedEdge(colorOnConnected, otherSideRelative);
        }

        return null;
    }

    private PreparedEdge findAndAlignEdgeOnSide() {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        for (var iConn = 0; iConn < connections.length; iConn++) {
            var connection = connections[iConn];

            var rightEdge = this.cube.side(connection.side()).face(Cube.MIDDLE_RIGHT);
            var nextSide = ArrayUtil.loopedIndex(connections, iConn - 1).side();
            var leftEdgeOfNext = this.cube.side(nextSide).face(Cube.MIDDLE_LEFT);
            if (rightEdge == FaceColor.YELLOW || leftEdgeOfNext == FaceColor.YELLOW) {
                continue;
            }

            var alreadySolved = rightEdge == connection.side() && leftEdgeOfNext == nextSide;
            if (alreadySolved) {
                continue;
            }

            this.addMarker("secondEdgeMove", rightEdge, leftEdgeOfNext);
            this.moveSideEdgeToYellowAndClockwise(connection.side(), nextSide);

            var newSide = ArrayUtil.loopedIndex(connections, iConn + 1).side();
            var distance = distanceAroundYellow(newSide, rightEdge);
            this.rotate(FaceColor.YELLOW, distance);

            return new PreparedEdge(rightEdge, distanceAroundYellow(rightEdge, leftEdgeOfNext));
        }

        return null;
    }

    private void moveSideEdgeToYellowAndClockwise(int leftSide, int rightSide) {
        this.counterClockwise(leftSide);
        this.counterClockwise(FaceColor.YELLOW);
        this.clockwise(leftSide);
        this.clockwise(FaceColor.YELLOW);
        this.clockwise(rightSide);
        this.clockwise(FaceColor.YELLOW);
        this.counterClockwise(rightSide);
    }

    private void insertEdge(PreparedEdge edge) {
        var initialConnectedSide = edge.connectedSide();
        var otherConnectedSide = Cube.getAdjacentSideFromConnectedSideWithOffset(
                FaceColor.YELLOW, edge.connectedSide, edge.otherSideRelative
        ).side();

        this.addMarker("secondEdgeInsert", initialConnectedSide, otherConnectedSide);
        this.rotate(FaceColor.YELLOW, -edge.otherSideRelative);
        this.rotate(otherConnectedSide, -edge.otherSideRelative);
        this.rotate(FaceColor.YELLOW, edge.otherSideRelative);
        this.rotate(otherConnectedSide, edge.otherSideRelative);
        this.rotate(FaceColor.YELLOW, edge.otherSideRelative);
        this.rotate(initialConnectedSide, edge.otherSideRelative);
        this.rotate(FaceColor.YELLOW, -edge.otherSideRelative);
        this.rotate(initialConnectedSide, -edge.otherSideRelative);
    }

    private record PreparedEdge(int connectedSide, int otherSideRelative) {

        public PreparedEdge {
            if (Math.abs(otherSideRelative) != 1) {
                throw new IllegalArgumentException("otherSideRelative must be -1 or 1");
            }
        }
    }
}
