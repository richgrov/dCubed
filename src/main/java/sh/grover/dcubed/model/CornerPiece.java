package sh.grover.dcubed.model;

public record CornerPiece(int color1, int color2, int color3) {

    public CornerPiece {
        FaceColor.requireValid(color1);
        FaceColor.requireValid(color2);
        FaceColor.requireValid(color3);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CornerPiece other)) {
            return false;
        }

        var compare1 = (1 << this.color1) | (1 << this.color2) | (1 << this.color3);
        var compare2 = (1 << other.color1) | (1 << other.color2) | (1 << other.color3);
        return compare1 == compare2;
    }
}