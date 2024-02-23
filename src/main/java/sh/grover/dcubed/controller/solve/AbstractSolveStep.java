package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.Move;

import java.util.Collections;
import java.util.List;

public abstract class AbstractSolveStep {

    /**
     * The distance of a side to another side. E.g. [RED][ORANGE] returns 2 because they are
     * opposite to each other. This only accounts for sides touching the white side. It can also
     * work for the yellow side indexing the faces backwards.
     */
    private static final int[][] SIDE_DISTANCES = new int[][] {
            { 99, 99, 99, 99, 99, 99 },
            { 99,  0,  2, 99,  1, -1 },
            { 99,  2,  0, 99, -1,  1 },
            { 99, 99, 99, 99, 99, 99 },
            { 99, -1,  1, 99,  0,  2 },
            { 99,  1, -1, 99,  2,  0 },
    };

    protected static int distanceAroundWhite(int fromSide, int toSide) {
        var distance = SIDE_DISTANCES[fromSide][toSide];
        if (distance == 99) {
            throw new IndexOutOfBoundsException("can't find distance between " + fromSide + " and " + toSide);
        }
        return distance;
    }

    protected static int distanceAroundYellow(int fromSide, int toSide) {
        return distanceAroundWhite(toSide, fromSide);
    }

    protected final Cube cube;
    private final List<Move> moves;

    public AbstractSolveStep(Cube cube, List<Move> moves) {
        this.cube = cube;
        this.moves = moves;
    }

    public abstract void solve();

    protected void clockwise(int color) {
        this.moves.add(new Move(color, true));
        this.cube.rotateClockwise(color);
    }

    protected void counterClockwise(int color) {
        this.moves.add(new Move(color, false));
        this.cube.rotateCounterClockwise(color);
    }

    /**
     * Rotates a side by a specified number of times and direction
     * @param color The color of the side to rotate
     * @param turns The number of times to rotate. If positive, clockwise. Otherwise, counter-clockwise
     */
    protected void rotate(int color, int turns) {
        for (var turn = 0; turn < Math.abs(turns); turn++) {
            if (turns < 0) {
                this.counterClockwise(color);
            } else {
                this.clockwise(color);
            }
        }
    }

    public List<Move> moves() {
        return Collections.unmodifiableList(this.moves);
    }
}