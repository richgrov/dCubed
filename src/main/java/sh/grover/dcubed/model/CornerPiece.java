package sh.grover.dcubed.model;

public record CornerPiece(int color1, int color2, int color3) {

    public CornerPiece {
        FaceColor.requireValid(color1);
        FaceColor.requireValid(color2);
        FaceColor.requireValid(color3);
    }
}