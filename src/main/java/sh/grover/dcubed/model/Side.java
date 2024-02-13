package sh.grover.dcubed.model;

import java.util.Arrays;

public class Side {

    public final FaceColor[] colors;

    public Side(FaceColor... colors) {
        if (colors.length != 8) {
            throw new IllegalArgumentException("side must comprise exactly 8 colors");
        }

        this.colors = colors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Side side = (Side) o;
        return Arrays.equals(colors, side.colors);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(colors);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.colors);
    }
}
