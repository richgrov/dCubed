package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHumanAlgorithm implements ISolvingAlgorithm {

    private static Cube.SideConnection getConnectingInfo(FaceColor side, FaceColor touching) {
        for (var connected : Cube.getConnections(side)) {
            if (connected.side() == touching.ordinal()) {
                return connected;
            }
        }
        return null;
    }

    protected Cube cube;
    private final List<Move> moves = new ArrayList<>();

    public void whiteCross() {
        this.rotateWhiteSide();
    }

    private void rotateWhiteSide() {
        var sides = this.cube.getSides();
        var touching = Cube.getConnections(FaceColor.WHITE);

        for (var touch : touching) {
            var indexOfAdjacentSi = touch.faces()[1];
            var adjacentColor = sides[touch.side()].toColors()[indexOfAdjacentSi];
            System.out.println(adjacentColor + " connected to " + cube.getColorOfEdgePiece(FaceColor.WHITE, FaceColor.values()[touch.side()]));
        }
    }
}
