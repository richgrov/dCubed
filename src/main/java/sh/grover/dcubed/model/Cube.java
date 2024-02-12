package sh.grover.dcubed.model;

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
    private final long[] faces = new long[6];

    public void rotateClockwise(int face) {
        faces[face] = Long.rotateRight(faces[face], 16);

    }

    public void rotateCounterClockwise(int face) {
        faces[face] = Long.rotateLeft(faces[face], 16);
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
}
