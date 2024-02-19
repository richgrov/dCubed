package sh.grover.dcubed.controller;

import sh.grover.dcubed.controller.vision.ScannedSide;
import sh.grover.dcubed.model.FaceColor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SolveSession {

    private final HashMap<FaceColor, FaceColor[]> sides = new HashMap<>();

    public void addSide(ScannedSide side) {
        this.sides.put(side.sideColor(), side.faces());
    }

    public Map<FaceColor, FaceColor[]> sides() {
        return Collections.unmodifiableMap(this.sides);
    }
}