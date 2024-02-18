package sh.grover.dcubed.controller.vision;

import sh.grover.dcubed.model.FaceColor;

public record ScannedSide(FaceColor sideColor, FaceColor[] faces) {
}
