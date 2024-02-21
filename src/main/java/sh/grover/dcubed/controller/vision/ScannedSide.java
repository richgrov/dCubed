package sh.grover.dcubed.controller.vision;

import sh.grover.dcubed.model.FaceColor;

public record ScannedSide(int sideColor, int[] faces) {

    public ScannedSide {
        FaceColor.requireValid(sideColor);
        for (var faceColor : faces) {
            FaceColor.requireValid(faceColor);
        }
    }
}
