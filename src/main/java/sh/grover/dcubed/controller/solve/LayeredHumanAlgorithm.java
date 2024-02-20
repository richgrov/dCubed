package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;

import java.util.List;

public class LayeredHumanAlgorithm extends AbstractHumanAlgorithm {

    @Override
    public List<Move> solve(Cube cube) {
        synchronized (this) {
            this.cube = cube;
            this.whiteCross();
        }
        return null;
    }
}
