package sh.grover.dcubed.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CubeTest {

    private static final Side[] SOLVED_SIDES = {
            sideOfAll(FaceColor.WHITE),
            sideOfAll(FaceColor.RED),
            sideOfAll(FaceColor.ORANGE),
            sideOfAll(FaceColor.YELLOW),
            sideOfAll(FaceColor.GREEN),
            sideOfAll(FaceColor.BLUE),
    };

    @Test
    void getSides() {
        var cube = new Cube(SOLVED_SIDES);
        assertArrayEquals(SOLVED_SIDES, cube.getSides());
    }

    private static Side sideOfAll(FaceColor color) {
        var faces = new FaceColor[8];
        Arrays.fill(faces, color);
        return new Side(faces);
    }
}