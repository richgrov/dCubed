package sh.grover.dcubed.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Side {

    private long encoded = 0;

    public Side(int... colors) {
        if (colors.length != 8) {
            throw new IllegalArgumentException("side must comprise exactly 8 colors");
        }

        for (var iColor = 0; iColor < 8; iColor++) {
            FaceColor.requireValid(colors[iColor]);

            var color = (long) colors[iColor];
            this.encoded |= color << ((7 - iColor) * 8);
        }
    }

    public static Side from3x3(int[] faces) {
        return new Side(faces[0], faces[1], faces[2], faces[5], faces[8], faces[7], faces[6], faces[3]);
    }

    Side(long encoded) {
        this.encoded = encoded;
    }

    public int face(int faceIndex) {
        return (int) (this.encoded >>> ((7 - faceIndex) * 8)) & 0xFF;
    }

    public Side rotated(int amount) {
        var rotatedEncoded = Long.rotateRight(this.encoded, amount * 16);
        return new Side(rotatedEncoded);
    }

    public long encoded() {
        return this.encoded;
    }

    public static Side all(int color) {
        var faces = new int[8];
        Arrays.fill(faces, color);
        return new Side(faces);
    }

    public static Side checker(int corner, int side) {
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
        return IntStream.range(0, 8)
                .mapToObj(this::face)
                .map(FaceColor::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
