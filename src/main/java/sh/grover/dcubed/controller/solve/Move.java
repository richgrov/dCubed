package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.FaceColor;

public record Move(FaceColor side, boolean clockwise) {
}
