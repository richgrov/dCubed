package sh.grover.dcubed.controller;

import sh.grover.dcubed.controller.vision.ScannedSide;

import java.util.Arrays;

public class SolveSession {

    private final int[][] sides = new int[6][];

    public void addSide(ScannedSide side) {
        this.sides[side.sideColor()] = side.faces();
    }

    public int[][] sides() {
        return Arrays.stream(sides)
                .map(faces -> {
                    if (faces == null) {
                        return null;
                    }

                    var copy = new int[faces.length];
                    System.arraycopy(faces, 0, copy, 0, faces.length);
                    return copy;
                })
                .toArray(int[][]::new);
    }
}