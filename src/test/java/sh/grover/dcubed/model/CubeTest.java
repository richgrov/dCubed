package sh.grover.dcubed.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CubeTest {

    @Test
    void getSides() {
        var expected = new Side[] {
                Side.checker(FaceColor.WHITE, FaceColor.YELLOW),
                Side.checker(FaceColor.RED, FaceColor.ORANGE),
                Side.checker(FaceColor.ORANGE, FaceColor.RED),
                Side.checker(FaceColor.YELLOW, FaceColor.WHITE),
                Side.checker(FaceColor.GREEN, FaceColor.BLUE),
                Side.checker(FaceColor.BLUE, FaceColor.GREEN),
        };
        var cube = new Cube(Arrays.copyOf(expected, expected.length));
        assertArrayEquals(expected, cube.getSides());
    }

    @Test
    void rotateClockwise() {
        var cube = new Cube(
                Side.checker(FaceColor.WHITE, FaceColor.YELLOW),
                Side.all(FaceColor.RED),
                Side.all(FaceColor.ORANGE),
                Side.checker(FaceColor.YELLOW, FaceColor.WHITE),
                Side.all(FaceColor.GREEN),
                Side.all(FaceColor.BLUE)
        );
        cube.rotateClockwise(FaceColor.WHITE);

        var expected = new Side[] {
                Side.checker(FaceColor.WHITE, FaceColor.YELLOW),
                sideWithBottomColor(FaceColor.RED, FaceColor.BLUE),
                sideWithBottomColor(FaceColor.ORANGE, FaceColor.GREEN),
                Side.checker(FaceColor.YELLOW, FaceColor.WHITE),
                sideWithBottomColor(FaceColor.GREEN, FaceColor.RED),
                sideWithBottomColor(FaceColor.BLUE, FaceColor.ORANGE),
        };

        assertArrayEquals(expected, cube.getSides());
    }

    @Test
    void rotateCounterClockwise() {
        var cube = new Cube(
                Side.checker(FaceColor.WHITE, FaceColor.YELLOW),
                Side.all(FaceColor.RED),
                Side.all(FaceColor.ORANGE),
                Side.checker(FaceColor.YELLOW, FaceColor.WHITE),
                Side.all(FaceColor.GREEN),
                Side.all(FaceColor.BLUE)
        );
        cube.rotateCounterClockwise(FaceColor.WHITE);

        var expected = new Side[] {
                Side.checker(FaceColor.WHITE, FaceColor.YELLOW),
                sideWithBottomColor(FaceColor.RED, FaceColor.GREEN),
                sideWithBottomColor(FaceColor.ORANGE, FaceColor.BLUE),
                Side.checker(FaceColor.YELLOW, FaceColor.WHITE),
                sideWithBottomColor(FaceColor.GREEN, FaceColor.ORANGE),
                sideWithBottomColor(FaceColor.BLUE, FaceColor.RED),
        };

        assertArrayEquals(expected, cube.getSides());
    }

    @Test
    public void testScrambleUnscramble() {
        var expectedSolved = new Side[] {
            Side.all(FaceColor.WHITE),
            Side.all(FaceColor.RED),
            Side.all(FaceColor.ORANGE),
            Side.all(FaceColor.YELLOW),
            Side.all(FaceColor.GREEN),
            Side.all(FaceColor.BLUE)
        };
        var cube = new Cube(expectedSolved);

        cube.rotateClockwise(FaceColor.WHITE);
        cube.rotateCounterClockwise(FaceColor.ORANGE);
        cube.rotateClockwise(FaceColor.BLUE);
        cube.rotateClockwise(FaceColor.RED);
        cube.rotateCounterClockwise(FaceColor.BLUE);
        cube.rotateClockwise(FaceColor.WHITE);
        cube.rotateCounterClockwise(FaceColor.YELLOW);
        cube.rotateCounterClockwise(FaceColor.GREEN);

        var expectedScrambled = new Side[] {
                new Side(FaceColor.WHITE, FaceColor.RED, FaceColor.ORANGE, FaceColor.WHITE, FaceColor.GREEN, FaceColor.GREEN, FaceColor.RED, FaceColor.WHITE),
                new Side(FaceColor.RED, FaceColor.WHITE, FaceColor.WHITE, FaceColor.BLUE, FaceColor.BLUE, FaceColor.ORANGE, FaceColor.WHITE, FaceColor.RED),
                new Side(FaceColor.GREEN, FaceColor.GREEN, FaceColor.YELLOW, FaceColor.GREEN, FaceColor.YELLOW, FaceColor.RED, FaceColor.WHITE, FaceColor.GREEN),
                new Side(FaceColor.YELLOW, FaceColor.BLUE, FaceColor.BLUE, FaceColor.YELLOW, FaceColor.YELLOW, FaceColor.ORANGE, FaceColor.GREEN, FaceColor.BLUE),
                new Side(FaceColor.ORANGE, FaceColor.YELLOW, FaceColor.RED, FaceColor.RED, FaceColor.BLUE, FaceColor.YELLOW, FaceColor.RED, FaceColor.YELLOW),
                new Side(FaceColor.ORANGE, FaceColor.ORANGE, FaceColor.BLUE, FaceColor.BLUE, FaceColor.GREEN, FaceColor.ORANGE, FaceColor.ORANGE, FaceColor.WHITE),
        };
        assertArrayEquals(expectedScrambled, cube.getSides());

        cube.rotateClockwise(FaceColor.GREEN);
        cube.rotateClockwise(FaceColor.YELLOW);
        cube.rotateCounterClockwise(FaceColor.WHITE);
        cube.rotateClockwise(FaceColor.BLUE);
        cube.rotateCounterClockwise(FaceColor.RED);
        cube.rotateCounterClockwise(FaceColor.BLUE);
        cube.rotateClockwise(FaceColor.ORANGE);
        cube.rotateCounterClockwise(FaceColor.WHITE);

        assertArrayEquals(expectedSolved, cube.getSides());
    }

    private static Side sideWithBottomColor(int sideColor, int bottomColor) {
        return new Side(
                sideColor, sideColor, sideColor, sideColor,
                bottomColor, bottomColor, bottomColor, sideColor
        );
    }

    @Test
    void getCornerPiece() {
        var cube = new Cube(
                new Side(FaceColor.WHITE, FaceColor.RED, FaceColor.ORANGE, FaceColor.WHITE, FaceColor.GREEN, FaceColor.GREEN, FaceColor.RED, FaceColor.WHITE),
                new Side(FaceColor.RED, FaceColor.WHITE, FaceColor.WHITE, FaceColor.BLUE, FaceColor.BLUE, FaceColor.ORANGE, FaceColor.WHITE, FaceColor.RED),
                new Side(FaceColor.GREEN, FaceColor.GREEN, FaceColor.YELLOW, FaceColor.GREEN, FaceColor.YELLOW, FaceColor.RED, FaceColor.WHITE, FaceColor.GREEN),
                new Side(FaceColor.YELLOW, FaceColor.BLUE, FaceColor.BLUE, FaceColor.YELLOW, FaceColor.YELLOW, FaceColor.ORANGE, FaceColor.GREEN, FaceColor.BLUE),
                new Side(FaceColor.ORANGE, FaceColor.YELLOW, FaceColor.RED, FaceColor.RED, FaceColor.BLUE, FaceColor.YELLOW, FaceColor.RED, FaceColor.YELLOW),
                new Side(FaceColor.ORANGE, FaceColor.ORANGE, FaceColor.BLUE, FaceColor.BLUE, FaceColor.GREEN, FaceColor.ORANGE, FaceColor.ORANGE, FaceColor.WHITE)
        );

        assertEquals(
                new CornerPiece(FaceColor.YELLOW, FaceColor.BLUE, FaceColor.RED),
                cube.getCornerPiece(FaceColor.YELLOW, Cube.TOP_LEFT)
        );

        assertEquals(
                new CornerPiece(FaceColor.BLUE, FaceColor.ORANGE, FaceColor.YELLOW),
                cube.getCornerPiece(FaceColor.YELLOW, Cube.TOP_RIGHT)
        );

        assertEquals(
                new CornerPiece(FaceColor.WHITE, FaceColor.ORANGE, FaceColor.BLUE),
                cube.getCornerPiece(FaceColor.ORANGE, Cube.BOTTOM_LEFT)
        );

        assertEquals(
                new CornerPiece(FaceColor.GREEN, FaceColor.ORANGE, FaceColor.YELLOW),
                cube.getCornerPiece(FaceColor.WHITE, Cube.BOTTOM_RIGHT)
        );
    }
}