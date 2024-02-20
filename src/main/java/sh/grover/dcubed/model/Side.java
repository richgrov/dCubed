package sh.grover.dcubed.model;

import java.util.Arrays;
import java.util.Objects;

public class Side {

    private long encoded = 0;

    public Side(FaceColor... colors) {
        if (colors.length != 8) {
            throw new IllegalArgumentException("side must comprise exactly 8 colors");
        }

        for (var iColor = 0; iColor < 8; iColor++) {
            var color = (long) colors[iColor].ordinal();
            this.encoded |= color << ((7 - iColor) * 8);
        }
    }

    Side(long encoded) {
        this.encoded = encoded;
    }

    public FaceColor[] toColors() {
        var faceColors = new FaceColor[8];
        for (var iFace = 0; iFace < 8; iFace++) {
            var colorIndex = (this.encoded >>> ((7 - iFace) * 8)) & 0xFF;
            faceColors[iFace] = FaceColor.values()[(int) colorIndex];
        }
        return faceColors;
    }

    public long encoded() {
        return this.encoded;
    }

    public static Side all(FaceColor color) {
        var faces = new FaceColor[8];
        Arrays.fill(faces, color);
        return new Side(faces);
    }

    public static Side checker(FaceColor corner, FaceColor side) {
        return new Side(corner, side, corner, side, corner, side, corner, side);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Side side) {
            return side.encoded == this.encoded;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(encoded);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.toColors());
    }
}
