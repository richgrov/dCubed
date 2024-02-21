package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSolveStep {

    protected final Cube cube;
    private final List<Move> moves = new ArrayList<>();

    public AbstractSolveStep(Cube cube) {
        this.cube = cube;
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