package sh.grover.dcubed.model;

import sh.grover.dcubed.controller.solve.AbstractSolveStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SolveInstructions(
        List<Move> moves,
        Map<String, Integer> stageIndices,
        Map<Integer, MoveMarker> markers
) {
    public SolveInstructions() {
        this(new ArrayList<>(128), new HashMap<>(), new HashMap<>(4));
    }

    public void runStep(AbstractSolveStep step) {
        this.stageIndices.put(step.stepId(), this.moves.size());
        step.solve();

        for (var entry : step.markers().entrySet()) {
            this.markers.put(entry.getKey() + this.moves.size(), entry.getValue());
        }

        this.moves.addAll(step.moves());
    }
}
