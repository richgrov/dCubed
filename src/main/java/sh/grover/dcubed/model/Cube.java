package sh.grover.dcubed.model;

import sh.grover.dcubed.util.ArrayUtil;

/**
 * A Rubik's cube contains 6 sides. Each side comprises 9 faces: 8 side
 * faces and one center face. Since there can only be one of each color for
 * evey center face, they do not need to be stored, and instead inferred
 * from the index of the face within the cube. Since we are now only
 * representing 8 faces, they can perfectly fit as one byte each in a `long`
 * data type. The layout of which can be seen below:
 * <br>
 * {@code 012}
 * {@code 7 3}
 * {@code 654}
 * <br>
 * Each number represents the index of the byte within the long. For example,
 * `1` is 8 bits into the long, `3` is 24 bits, etc.
 * <br>
 * The cube is treated as having the yellow side up, white side down, and all
 * other sides on the side. The indices of the side faces align as if one is
 * looking directly at them, so `012` is the top-most row and `654` is the
 * bottom. The top (yellow) side has `012` touching the blue face and `654`
 * touching the green side. The bottom (white) side has `012` touching the
 * green side and `654` touching the blue side. This effectively means the
 * white and yellow faces are aligned to the green face, with the order
 * corresponding to how they appear as if looking at them with green facing the
 * observer's direction.
 * <br>
 * Storing sides this way allows for efficient rotation through the user of
 * {@link Long#rotateLeft} and {@link Long#rotateRight}. For rotating the
 * connecting sides, an array of {@link SideConnection}s are stored, which
 * tracks- for each side -the touching sides and the faces of that side that
 * are adjacent.
 */
public class Cube {

    private static final int WHITE = 0;
    private static final int RED = 1;
    private static final int ORANGE = 2;
    private static final int YELLOW = 3;
    private static final int GREEN = 4;
    private static final int BLUE = 5;

    private static final SideConnection[][] SIDE_CONNECTIONS = {
            // white
            {
                    SideConnection.bottomOf(RED),
                    SideConnection.bottomOf(GREEN),
                    SideConnection.bottomOf(ORANGE),
                    SideConnection.bottomOf(BLUE),
            },
            // red
            createSideFaceSideConnections(BLUE, GREEN),
            // orange
            createSideFaceSideConnections(GREEN, BLUE),
            // yellow
            {
                    SideConnection.topOf(RED),
                    SideConnection.topOf(BLUE),
                    SideConnection.topOf(ORANGE),
                    SideConnection.topOf(GREEN),
            },
            // green
            createSideFaceSideConnections(RED, ORANGE),
            // blue
            createSideFaceSideConnections(ORANGE, RED),
    };

    /** Order matches the order of specified side colors */
    private final long[] sides = new long[6];

    public Cube(Side[] sides) {
        if (sides.length != 6) {
            throw new IllegalArgumentException("cube must be created with exactly 6 sides");
        }

        for (var iSide = 0; iSide < sides.length; iSide++) {
            this.sides[iSide] = packSide(sides[iSide].colors);
        }
    }

    public void rotateClockwise(int side) {
        sides[side] = Long.rotateRight(sides[side], 16);
        this.rotateTouchingSides(side, 1);
    }

    public void rotateCounterClockwise(int side) {
        sides[side] = Long.rotateLeft(sides[side], 16);
        this.rotateTouchingSides(side, -1);
    }

    public Side[] getSides() {
        var outSides = new Side[6];

        for (var iSide = 0; iSide < 6; iSide++) {
            var unpacked = unpackSide(this.sides[iSide]);
            outSides[iSide] = new Side(unpacked);
        }

        return outSides;
    }

    private void rotateTouchingSides(int side, int rotateDirection) {
        var touchingFaces = new byte[4 * 3];

        var connectingSides = SIDE_CONNECTIONS[side];
        for (var iSide = 0; iSide < connectingSides.length; iSide++) {
            var connection = connectingSides[iSide];
            touchingFaces[iSide * 4] = this.getFaceColor(connection.side(), connection.faces[0]);
            touchingFaces[iSide * 4 + 1] = this.getFaceColor(connection.side(), connection.faces[1]);
            touchingFaces[iSide * 4 + 2] = this.getFaceColor(connection.side(), connection.faces[2]);
        }

        for (var iSide = 0; iSide < connectingSides.length; iSide++) {
            var connection = ArrayUtil.loopedIndex(connectingSides, iSide + rotateDirection);
            this.setFaceColor(connection.side(), connection.faces()[0], touchingFaces[iSide * 4]);
            this.setFaceColor(connection.side(), connection.faces()[1], touchingFaces[iSide * 4 + 1]);
            this.setFaceColor(connection.side(), connection.faces()[2], touchingFaces[iSide * 4 + 2]);
        }
    }

    private byte getFaceColor(int side, int faceIndex) {
        var shifted = this.sides[side] >>> ((7 - faceIndex) * 8);
        return (byte) (shifted & 0xFF);
    }

    private void setFaceColor(int side, int faceIndex, byte color) {
        var shiftAmount = (7 - faceIndex) * 8;
        var sideWithFaceCleared = this.sides[side] & ~(0xFFL << shiftAmount);
        this.sides[side] = sideWithFaceCleared & (long) color << shiftAmount;
    }

    /**
     * @param side The side touching the side in question
     * @param faces The faces of that side, ordered as if the side in question
     *              was rotated clockwise
     */
    private record SideConnection(int side, int... faces) {
        static SideConnection bottomOf(int side) {
            return new SideConnection(side, 6, 5, 4);
        }

        static SideConnection leftOf(int side) {
            return new SideConnection(side, 0, 7, 6);
        }

        static SideConnection topOf(int side) {
            return new SideConnection(side, 2, 1, 0);
        }

        static SideConnection rightOf(int side) {
            return new SideConnection(side, 4, 3, 2);
        }
    }

    private static SideConnection[] createSideFaceSideConnections(int leftSide, int rightSide) {
        return new SideConnection[]{
                SideConnection.bottomOf(YELLOW),
                SideConnection.leftOf(rightSide),
                SideConnection.topOf(WHITE),
                SideConnection.rightOf(leftSide),
        };
    }

    private static long packSide(FaceColor[] colors) {
        var encodedSide = 0L;
        for (var iColor = 0; iColor < 8; iColor++) {
            var color = (long) colors[iColor].ordinal();
            encodedSide |= color << ((7 - iColor) * 8);
        }

        return encodedSide;
    }

    private static FaceColor[] unpackSide(long side) {
        var faceColors = new FaceColor[8];
        for (var iFace = 0; iFace < 8; iFace++) {
            var colorIndex = (side >>> ((7 - iFace) * 8)) & 0xFF;
            faceColors[iFace] = FaceColor.values()[(int) colorIndex];
        }
        return faceColors;
    }
}
