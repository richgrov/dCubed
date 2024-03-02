package sh.grover.dcubed.model;

import sh.grover.dcubed.controller.solve.AbstractSolveStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SolveInstructions(
        List<Move> moves,
        Map<String, Integer> stageIndices
) {
    public SolveInstructions() {
        this(new ArrayList<>(128), new HashMap<>());
    }

    public void runStep(AbstractSolveStep step) {
        this.stageIndices.put(step.stepId(), this.moves.size());
        step.solve();
        this.moves.addAll(step.moves());
    }
}
