package sh.grover.dcubed.model;

public record Move(String side, boolean clockwise) {

    public Move(int sideColor, boolean clockwise) {
        this(FaceColor.toString(sideColor), clockwise);
    }
}
