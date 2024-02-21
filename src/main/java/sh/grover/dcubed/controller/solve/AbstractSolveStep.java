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

    public List<Move> moves() {
        return Collections.unmodifiableList(this.moves);
    }
}