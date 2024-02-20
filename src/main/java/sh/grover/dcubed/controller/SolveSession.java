package sh.grover.dcubed.controller;

import sh.grover.dcubed.controller.vision.ScannedSide;
import sh.grover.dcubed.model.FaceColor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class SolveSession {

    private final Map<FaceColor, FaceColor[]> sides = new EnumMap<>(FaceColor.class);

    public void addSide(ScannedSide side) {
        this.sides.put(side.sideColor(), side.faces());
    }

    public Map<FaceColor, FaceColor[]> sides() {
        return Collections.unmodifiableMap(this.sides);
    }
}