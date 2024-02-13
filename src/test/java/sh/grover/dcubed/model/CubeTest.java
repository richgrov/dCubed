package sh.grover.dcubed.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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

    private static Side sideWithBottomColor(FaceColor sideColor, FaceColor bottomColor) {
        return new Side(
                sideColor, sideColor, sideColor, sideColor,
                bottomColor, bottomColor, bottomColor, sideColor
        );
    }
}