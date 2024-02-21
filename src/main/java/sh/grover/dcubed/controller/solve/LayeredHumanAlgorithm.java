package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.Move;

import java.util.List;

public class LayeredHumanAlgorithm extends AbstractHumanAlgorithm {

    @Override
    public synchronized List<Move> solve(Cube cube) {
        this.moves.clear();
        this.cube = cube;
        this.whiteCross();
        return this.moves;
    }
}
