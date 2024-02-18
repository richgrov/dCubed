package sh.grover.dcubed.controller;

import sh.grover.dcubed.controller.vision.ScannedSide;
import sh.grover.dcubed.model.FaceColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SolveSession {

    private final HashMap<FaceColor, FaceColor[]> sides = new HashMap<>();

    public void addSide(ScannedSide side) {
        this.sides.put(side.sideColor(), side.faces());
    }

    public List<FaceColor> unscannedSides() {
        var unscanned = new ArrayList<FaceColor>(6);

        for (var faceColor : FaceColor.values()) {
            if (!this.sides.containsKey(faceColor)) {
                unscanned.add(faceColor);
            }
        }

        return unscanned;
    }
}