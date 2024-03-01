package sh.grover.dcubed.controller;

import sh.grover.dcubed.model.Side;

public class SolveSession {

    private final Side[] sides = new Side[6];

    public void mergeSides(Side[] sides) {
        for (var sideColor = 0; sideColor < sides.length; sideColor++) {
            var side = sides[sideColor];
            if (side != null) {
                this.sides[sideColor] = side;
            }
        }
    }

    public Side[] sides() {
        var copy = new Side[this.sides.length];
        System.arraycopy(this.sides, 0, copy, 0, this.sides.length);
        return copy;
    }
}