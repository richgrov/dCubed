package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Move;

import java.util.List;

public class SecondLayerStep extends AbstractSolveStep {

    public SecondLayerStep(Cube cube, List<Move> moves) {
        super(cube, moves);
    }

    @Override
    public void solve() {
        for (var limit = 0; limit < 4; limit++) {
            var edge = this.findAndAlignEdgeOnYellow();
            if (edge == null) {
                // TODO: find on side
                continue;
            }

            this.insertEdge(edge);
        }

    }

    private PreparedEdge findAndAlignEdgeOnYellow() {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        for (Cube.SideConnection connection : connections) {
            var sides = this.cube.getSides();
            var connectedEdge = connection.faces()[1];

            var colorOnYellow = this.cube.getColorOfEdgePiece(FaceColor.YELLOW, connection.side());
            var colorOnConnected = sides[connection.side()].toColors()[connectedEdge];
            if (colorOnYellow == FaceColor.YELLOW || colorOnConnected == FaceColor.YELLOW) {
                continue;
            }

            var distance = distanceAroundYellow(connection.side(), colorOnConnected);
            this.rotate(FaceColor.YELLOW, distance);

            int otherSideRelative;
            if (Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, colorOnConnected, 1)
                    .side() == colorOnYellow) {
                otherSideRelative = 1;
            } else if (Cube.getAdjacentSideFromConnectedSideWithOffset(FaceColor.YELLOW, colorOnConnected, -1)
                    .side() == colorOnYellow) {
                otherSideRelative = -1;
            } else {
                throw new IllegalStateException("couldn't determine rotation direction");
            }

            return new PreparedEdge(colorOnConnected, otherSideRelative);
        }

        return null;
    }

    private void insertEdge(PreparedEdge edge) {
        var initialConnectedSide = edge.connectedSide();
        var otherConnectedSide = Cube.getAdjacentSideFromConnectedSideWithOffset(
                FaceColor.YELLOW, edge.connectedSide, edge.otherSideRelative
        ).side();

        this.rotate(FaceColor.YELLOW, -edge.otherSideRelative);
        this.rotate(otherConnectedSide, -edge.otherSideRelative);
        this.rotate(FaceColor.YELLOW, edge.otherSideRelative);
        this.rotate(otherConnectedSide, edge.otherSideRelative);
        this.rotate(FaceColor.YELLOW, edge.otherSideRelative);
        this.rotate(initialConnectedSide, edge.otherSideRelative);
        this.rotate(FaceColor.YELLOW, -edge.otherSideRelative);
        this.rotate(initialConnectedSide, -edge.otherSideRelative);
    }

    private record PreparedEdge(int connectedSide, int otherSideRelative) {}
}
