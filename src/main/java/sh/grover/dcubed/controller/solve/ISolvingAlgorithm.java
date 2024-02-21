package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.Move;

import java.util.List;

public interface ISolvingAlgorithm {

    List<Move> solve(Cube cube);
}
