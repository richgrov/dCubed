package sh.grover.dcubed.controller.solve;

import sh.grover.dcubed.model.CornerPiece;
import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Move;
import sh.grover.dcubed.util.ArrayUtil;

import java.util.List;

/**
 * Positions and orients each of the white corners to the correct form. The
 * following steps are repeated four times, once for each connected side to
 * white:
 * 1. In this project, white is on bottom, so the "white corner" in this case
 * is the bottom right of the connected side. Following this logic, the 3 sides
 * of this corner are the white side, the current connected side, and the side
 * to the right of the connected side. The corner we need to find is now
 * identified. This is represented in the first few lines of the loop body in
 * {@link this#solve}
 *
 * 2. Check if the corner is solved. If not, find where it is, and bring it to
 * be "above" the spot it needs to be. In other words, put it on the same
 * column as where it should be, but on the yellow side. There are two
 * possibilities for how this plays out:
 * 2a. The corner is on the white side: Rotate the side it's on to bring it to
 * the top, rotate it to be "above" its target, and "unrotate" the side it was
 * on to prevent the white cross from being lost. However, unrotating may cause
 * the corner to no longer be above its target if it's still on the same side.
 * Therefore, the corner must be rotated off of this side, and after the white
 * cross is restored, the corner can also be restored.
 * 2b. The corner is on the yellow side: This is easy. Simply rotate it to be
 * above its target. {@link this#findAndAlignCornerAbove}
 *
 * 3. Now, the traditional corner rotation/insertion algorithm can be used up
 * to three times until the corner is in its proper place.
 * {@link this#insertCornerWithCorrectRotation}
 * See <a
 * href="https://ruwix.com/the-rubiks-cube/how-to-solve-the-rubiks-cube-beginners-method/step-2-first-layer-corners/"
 * >rotation algorithm explained</a>
 */
public class WhiteCornersStep extends AbstractSolveStep {

    /**
     * White corners of a connected side are on the bottom-right of that side.
     * This table holds, for each side connected to the white side, the face
     * index on the white side for that corresponding connected side corner.
     */
    private static final int[] WHITE_CORNER_COLORS = new int[] {
            -1, // white
            Cube.TOP_LEFT, // red
            Cube.BOTTOM_RIGHT, // orange
            -1, // yellow
            Cube.TOP_RIGHT, // green
            Cube.BOTTOM_LEFT, // blue
    };

    /**
     * Similar to the table above, but for the top-right of a connected face.
     * Used when scanning the yellow side.
     */
    private static final int[] YELLOW_CORNER_COLORS = new int[] {
            -1, // white
            Cube.BOTTOM_LEFT, // red
            Cube.TOP_RIGHT, // orange
            -1, // yellow
            Cube.BOTTOM_RIGHT, // green
            Cube.TOP_LEFT, // blue
    };

    public WhiteCornersStep(Cube cube, List<Move> moves) {
        super(cube, moves);
    }

    @Override
    public void solve() {
        var connections = Cube.getConnections(FaceColor.WHITE);
        for (var iConn = 0; iConn < connections.length; iConn++) {
            var leftSideOfCorner = connections[iConn].side();
            var rightSideOfCorner = ArrayUtil.loopedIndex(connections, iConn + 1).side();

            if (this.isCornerSolved(leftSideOfCorner, rightSideOfCorner)) {
                continue;
            }

            this.findAndAlignCornerAbove(leftSideOfCorner, rightSideOfCorner);
            this.insertCornerWithCorrectRotation(leftSideOfCorner);
        }
    }

    private void findAndAlignCornerAbove(int leftSideOfCorner, int rightSideOfCorner) {
        var originSide = this.getSideOfCorner(FaceColor.WHITE, leftSideOfCorner, rightSideOfCorner, WHITE_CORNER_COLORS);
        var needsRestore = false;
        if (originSide != -1) {
            this.counterClockwise(originSide);
            needsRestore = true;
        } else {
            originSide = this.getSideOfCorner(FaceColor.YELLOW, leftSideOfCorner, rightSideOfCorner, YELLOW_CORNER_COLORS);
            if (originSide == -1) {
                throw new IllegalStateException("couldn't find white corner");
            }
        }

        var distance = distanceAroundYellow(originSide, leftSideOfCorner);
        this.rotate(FaceColor.YELLOW, distance);
        if (needsRestore) {
            if (distance == 1) {
                this.clockwise(FaceColor.YELLOW);
                this.clockwise(originSide);
                this.counterClockwise(FaceColor.YELLOW);
            } else {
                if (distance == 0) {
                    this.clockwise(FaceColor.YELLOW);
                }
                this.clockwise(originSide);
            }
        }
    }

    private void insertCornerWithCorrectRotation(int leftSideOfCorner) {
        for (var i = 0; i < 3; i++) {
            this.counterClockwise(leftSideOfCorner);
            this.counterClockwise(FaceColor.YELLOW);
            this.clockwise(leftSideOfCorner);

            var whiteFaces = this.cube.getSides()[FaceColor.WHITE].toColors();
            var correctRotation = whiteFaces[WHITE_CORNER_COLORS[leftSideOfCorner]] == FaceColor.WHITE;
            if (correctRotation) {
                return;
            }

            this.counterClockwise(leftSideOfCorner);
            this.counterClockwise(FaceColor.YELLOW);
            this.clockwise(leftSideOfCorner);
            this.clockwise(FaceColor.YELLOW);
        }

        throw new IllegalStateException("couldn't rotate corner in under 3 moves");
    }

    private int getSideOfCorner(int baseSide, int leftSideOfCorner, int rightSideOfCorner, int[] cornerLookup) {
        var target = new CornerPiece(FaceColor.WHITE, leftSideOfCorner, rightSideOfCorner);
        for (var connection : Cube.getConnections(baseSide)) {
            var corner = this.cube.getCornerPiece(baseSide, cornerLookup[connection.side()]);
            if (corner.equals(target)) {
                return connection.side();
            }
        }

        return -1;
    }

    private boolean isCornerSolved(int leftSideOfCorner, int rightSideOfCorner) {
        var sides = this.cube.getSides();
        return sides[leftSideOfCorner].toColors()[Cube.BOTTOM_RIGHT] == leftSideOfCorner &&
                sides[rightSideOfCorner].toColors()[Cube.BOTTOM_LEFT] == rightSideOfCorner;
    }
}
