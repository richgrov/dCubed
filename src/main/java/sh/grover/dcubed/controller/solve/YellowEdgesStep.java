package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.util.ArrayUtil;

public class YellowEdgesStep extends AbstractSolveStep {

    public YellowEdgesStep(Cube cube) {
        super(cube);
    }

    @Override
    public void solve() {
        this.rotateYellowBest();

        if (this.oppositeSidesMismatched()) {
            this.swapEdges();
            this.rotateYellowBest();
        }

        this.swapEdges();

        for (var connection : Cube.getConnections(FaceColor.YELLOW)) {
            if (this.distanceFromSideToYellowEdge(connection) != 0) {
                throw new IllegalStateException("couldn't align yellow edges");
            }
        }
    }

    @Override
    public String stepId() {
        return "yellowEdges";
    }

    private void rotateYellowBest() {
        var distanceVote = new int[4];

        for (var connection : Cube.getConnections(FaceColor.YELLOW)) {
            var distance = this.distanceFromSideToYellowEdge(connection);
            distanceVote[distance + 1]++; // distances are [-1, 2], so +1 to normalize that to [0, 3]
        }

        var bestMove = ArrayUtil.indexOfHighest(distanceVote);
        if (bestMove != -1) {
            this.addMarker("yellowCrossBest");
            this.rotate(FaceColor.YELLOW, bestMove - 1); // -1 to undo the normalization above
        }
    }

    private boolean oppositeSidesMismatched() {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        for (var iConn = 0; iConn < connections.length; iConn++) {
            if (this.distanceFromSideToYellowEdge(connections[iConn]) != 0) {
                var oppositeConnection = ArrayUtil.loopedIndex(connections, iConn + 2);
                return this.distanceFromSideToYellowEdge(oppositeConnection) != 0;
            }
        }

        return false;
    }

    private void swapEdges() {
        int rightSolvedSide = -1;
        var connections = Cube.getConnections(FaceColor.YELLOW);
        for (var iConn = 0; iConn < connections.length; iConn++) {
            if (this.distanceFromSideToYellowEdge(connections[iConn]) == 0) {
                var sideToLeft = ArrayUtil.loopedIndex(connections, iConn + 1);
                if (this.distanceFromSideToYellowEdge(sideToLeft) != 0) {
                    rightSolvedSide = connections[iConn].side();
                    break;
                }
            }
        }

        if (rightSolvedSide == -1) {
            return;
        }

        this.addMarker("yellowSwap");
        this.clockwise(rightSolvedSide);
        this.clockwise(FaceColor.YELLOW);
        this.counterClockwise(rightSolvedSide);
        this.clockwise(FaceColor.YELLOW);
        this.clockwise(rightSolvedSide);
        this.rotate(FaceColor.YELLOW, 2);
        this.counterClockwise(rightSolvedSide);
        this.clockwise(FaceColor.YELLOW);
    }
    
    private int distanceFromSideToYellowEdge(Cube.SideConnection connection) {
        var edgeIndex = connection.faces()[1];
        var edgeColor = this.cube.side(connection.side()).face(edgeIndex);
        return distanceAroundYellow(connection.side(), edgeColor);
    }
}
