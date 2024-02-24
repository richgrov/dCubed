package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Move;
import sh.grover.dcubed.util.ArrayUtil;

import java.util.List;

public class OrientYellowCornersStep extends AbstractSolveStep {

    public OrientYellowCornersStep(Cube cube, List<Move> moves) {
        super(cube, moves);
    }

    @Override
    public void solve() {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        var startingConnection = this.findLeadingUnsolvedCorner();
        System.out.println("starting on " + startingConnection);

        if (startingConnection != -1) {
            for (var limit = 0; limit < 4; limit++) {
                this.orient(startingConnection);
                var nextUnsolvedConnection = this.findLeadingUnsolvedCorner();
                if (nextUnsolvedConnection == -1) {
                    break;
                }
                System.out.println("found unsolved on " + nextUnsolvedConnection);

                var distance = distanceAroundYellow(connections[nextUnsolvedConnection].side(), connections[startingConnection].side());
                System.out.println("rotating " + distance);
                this.rotate(FaceColor.YELLOW, distance);
            }
        }

        this.finalYellowRotation();
    }

    private int findLeadingUnsolvedCorner() {
        var connections = Cube.getConnections(FaceColor.YELLOW);
        var sides = this.cube.getSides();
        var foundUnsolved = false;

        for (int iConn = 0; iConn < connections.length; iConn++) {
            var side = sides[connections[iConn].side()].toColors();
            if (side[Cube.TOP_LEFT] != side[Cube.TOP_MIDDLE]) {
                foundUnsolved = true;
                var nextSide = sides[ArrayUtil.loopedIndex(connections, iConn + 1).side()].toColors();
                if (nextSide[Cube.TOP_LEFT] == nextSide[Cube.TOP_MIDDLE]) {
                    System.out.println(side[Cube.TOP_LEFT] + " != " + side[Cube.TOP_MIDDLE] + " && " + nextSide[Cube.TOP_LEFT] + " == " + nextSide[Cube.TOP_MIDDLE]);
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
            System.out.println("orienting " + connectedSideIndex);
            var side = connections[connectedSideIndex].side();
            this.counterClockwise(side);
            this.clockwise(FaceColor.WHITE);
            this.clockwise(side);
            this.counterClockwise(FaceColor.WHITE);

            var sides = this.cube.getSides();
            var colors = sides[side].toColors();
            var thisCornerMatch = colors[Cube.TOP_LEFT] == colors[Cube.TOP_MIDDLE];
            System.out.println("colors " + colors[Cube.TOP_LEFT] + " " + colors[Cube.TOP_MIDDLE]);

            var nextColors = sides[ArrayUtil.loopedIndex(connections, connectedSideIndex + 1).side()].toColors();
            System.out.println("next colors " + nextColors[Cube.TOP_RIGHT] + " " + nextColors[Cube.TOP_MIDDLE]);
            var nextCornerMatch = nextColors[Cube.TOP_RIGHT] == nextColors[Cube.TOP_MIDDLE];
            if (thisCornerMatch && nextCornerMatch) {
                return;
            }
        }
        throw new IllegalStateException("couldn't orient corner");
    }

    private void finalYellowRotation() {
        var edge = this.cube.getSides()[FaceColor.RED].toColors()[Cube.TOP_MIDDLE];
        var distance = distanceAroundYellow(FaceColor.RED, edge);
        this.rotate(FaceColor.YELLOW, distance);
    }
}