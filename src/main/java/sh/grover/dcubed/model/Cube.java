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

    private static final SideConnection[][] SIDE_CONNECTIONS = {
            // white
            {
                SideConnection.bottomOf(FaceColor.RED),
                SideConnection.bottomOf(FaceColor.GREEN),
                SideConnection.bottomOf(FaceColor.ORANGE),
                SideConnection.bottomOf(FaceColor.BLUE),
            },
            // red
            {
                SideConnection.leftOf(FaceColor.YELLOW),
                SideConnection.leftOf(FaceColor.GREEN),
                SideConnection.leftOf(FaceColor.WHITE),
                SideConnection.rightOf(FaceColor.BLUE),
            },
            // orange
            {
                SideConnection.rightOf(FaceColor.YELLOW),
                SideConnection.leftOf(FaceColor.BLUE),
                SideConnection.rightOf(FaceColor.WHITE),
                SideConnection.rightOf(FaceColor.GREEN),
            },
            // yellow
            {
                SideConnection.topOf(FaceColor.RED),
                SideConnection.topOf(FaceColor.BLUE),
                SideConnection.topOf(FaceColor.ORANGE),
                SideConnection.topOf(FaceColor.GREEN),
            },
            // green
            {
                SideConnection.bottomOf(FaceColor.YELLOW),
                SideConnection.leftOf(FaceColor.ORANGE),
                SideConnection.topOf(FaceColor.WHITE),
                SideConnection.rightOf(FaceColor.RED),
            },
            // blue
            {
                SideConnection.topOf(FaceColor.YELLOW),
                SideConnection.leftOf(FaceColor.RED),
                SideConnection.bottomOf(FaceColor.WHITE),
                SideConnection.rightOf(FaceColor.ORANGE),
            },
    };

    /**
     * Indices of all side pieces that connect to another side piece.
     * E.g. {@code ADJACENT_CONNECTIONS[WHITE][RED]} gets the index of the white face that touches
     * the red side. Indices for sides that do not touch are -1.
     */
    private static final int[][] EDGE_PIECE_CONNECTIONS = new int[6][6];

    static {
        for (var side : FaceColor.values()) {
            EDGE_PIECE_CONNECTIONS[side] = new int[]{-1, -1, -1, -1, -1, -1};

            for (var connection : SIDE_CONNECTIONS[side]) {
                var backConnection = getConnection(connection.side, side);
                //noinspection DataFlowIssue
                EDGE_PIECE_CONNECTIONS[side][connection.side] = backConnection.faces[1];
            }
        }
    }

    private static SideConnection getConnection(int side, int touching) {
        for (var connected : SIDE_CONNECTIONS[side]) {
            if (connected.side() == touching) {
                return connected;
            }
        }
        return null;
    }

    /**
     * Gets the side adjacent from an offset of another side relative to rotation around a base side. For example, with
     * base side YELLOW, connected side RED and offset 1, this would start at the RED side and rotate along the YELLOW
     * side clockwise once, because the offset is 1, and find the BLUE side.
     * @param baseSide The side to base the offset rotation around.
     * @param connectedSide The side connected to the base side to start at.
     * @param offset The rotation direction. If negative, assuming rotating counter-clockwise.
     * @return The {@link SideConnection} or {@code null} if none was found.
     */
    public static SideConnection getAdjacentSideFromConnectedSideWithOffset(int baseSide, int connectedSide, int offset) {
        FaceColor.requireValid(baseSide);
        FaceColor.requireValid(connectedSide);

        var connected = SIDE_CONNECTIONS[baseSide];
        for (var iConn = 0; iConn < connected.length; iConn++) {
            if (connected[iConn].side() == connectedSide) {
                return ArrayUtil.loopedIndex(connected, iConn + offset);
            }
        }
        throw new IllegalArgumentException(baseSide + " not connected to " + connectedSide);
    }

    public static SideConnection[] getConnections(int color) {
        FaceColor.requireValid(color);
        return SIDE_CONNECTIONS[color];
    }

    /** Order matches the order of specified side colors */
    private final long[] sides = new long[6];

    public Cube(Side... sides) {
        if (sides.length != 6) {
            throw new IllegalArgumentException("cube must be created with exactly 6 sides");
        }

        for (var iSide = 0; iSide < sides.length; iSide++) {
            this.sides[iSide] = sides[iSide].encoded();
        }
    }

    public void rotateClockwise(int side) {
        FaceColor.requireValid(side);

        sides[side] = Long.rotateRight(sides[side], 16);
        var touchingFaces = this.getTouchingFaces(side);
        this.applyColorsToTouchingFaces(side, 1, touchingFaces);
    }

    public void rotateCounterClockwise(int side) {
        FaceColor.requireValid(side);

        sides[side] = Long.rotateLeft(sides[side], 16);
        var touchingFaces = this.getTouchingFaces(side);
        this.applyColorsToTouchingFaces(side, -1, touchingFaces);
    }

    public Side[] getSides() {
        var outSides = new Side[6];

        for (var iSide = 0; iSide < 6; iSide++) {
            outSides[iSide] = new Side(this.sides[iSide]);
        }

        return outSides;
    }

    /**
     * Gets the edge piece color of a side relative to a connecting side.
     * @param side The side of the face color to sample.
     * @param adjacentSide The side that connects to {@code side}, which indicates exactly which edge piece to get
     * @return The color of that edge piece
     */
    public int getColorOfEdgePiece(int side, int adjacentSide) {
        FaceColor.requireValid(side);
        FaceColor.requireValid(adjacentSide);

        var faceColors = new Side(this.sides[side]).toColors();
        var adjacentIndex = EDGE_PIECE_CONNECTIONS[side][adjacentSide];
        return faceColors[adjacentIndex];
    }

    /**
     * Gets the 12 faces (4 sides * 3 faces per side) that touch a side of a Rubik's cube.
     * @return The color IDs of all the faces.
     */
    private byte[] getTouchingFaces(int side) {
        var touchingFaces = new byte[4 * 3];

        var connectingSides = SIDE_CONNECTIONS[side];
        for (var iSide = 0; iSide < connectingSides.length; iSide++) {
            var connection = connectingSides[iSide];
            touchingFaces[iSide * 3] = this.getFaceColor(connection.side(), connection.faces[0]);
            touchingFaces[iSide * 3 + 1] = this.getFaceColor(connection.side(), connection.faces[1]);
            touchingFaces[iSide * 3 + 2] = this.getFaceColor(connection.side(), connection.faces[2]);
        }

        return touchingFaces;
    }

    private void applyColorsToTouchingFaces(int side, int sideOrderOffset, byte[] faceColors) {
        var connectingSides = SIDE_CONNECTIONS[side];
        for (var iSide = 0; iSide < connectingSides.length; iSide++) {
            var connection = ArrayUtil.loopedIndex(connectingSides, iSide + sideOrderOffset);
            this.setFaceColor(connection.side(), connection.faces()[0], faceColors[iSide * 3]);
            this.setFaceColor(connection.side(), connection.faces()[1], faceColors[iSide * 3 + 1]);
            this.setFaceColor(connection.side(), connection.faces()[2], faceColors[iSide * 3 + 2]);
        }
    }

    private byte getFaceColor(int side, int faceIndex) {
        var shifted = this.sides[side] >>> ((7 - faceIndex) * 8);
        return (byte) (shifted & 0xFF);
    }

    private void setFaceColor(int side, int faceIndex, byte color) {
        var shiftAmount = (7 - faceIndex) * 8;
        var sideWithFaceCleared = this.sides[side] & ~(0xFFL << shiftAmount);
        this.sides[side] = sideWithFaceCleared | ((long) color << shiftAmount);
    }

    /**
     * @param side The side touching the side in question
     * @param faces The faces of that side, ordered as if the side in question
     *              was rotated clockwise
     */
    public record SideConnection(int side, int... faces) {

        public SideConnection {
            FaceColor.requireValid(side);
        }

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
}
