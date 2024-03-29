package sh.grover.dcubed.controller.solve;

import org.junit.jupiter.api.Test;
import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Side;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolveTest {

    private static final int RAND_SEED = 0;
    private static final int SCRAMBLES = 100;
    private static final int MOVES = 20;

    @Test
    public void solve() {
        var random = new Random(RAND_SEED);

        for (var scramble = 0; scramble < SCRAMBLES; scramble++) {
            var cube = new Cube(
                    Side.all(FaceColor.WHITE),
                    Side.all(FaceColor.RED),
                    Side.all(FaceColor.ORANGE),
                    Side.all(FaceColor.YELLOW),
                    Side.all(FaceColor.GREEN),
                    Side.all(FaceColor.BLUE)
            );

            scramble(cube, random);
            unscramble(cube);
        }
    }

    private static void scramble(Cube cube, Random random) {
        for (var move = 0; move < MOVES; move++) {
            var side = random.nextInt(6);
            if (random.nextBoolean()) {
                cube.rotateClockwise(side);
            } else {
                cube.rotateCounterClockwise(side);
            }
        }
    }

    private static void unscramble(Cube cube) {
        new WhiteCrossStep(cube).solve();
        new WhiteCornersStep(cube).solve();
        new SecondLayerStep(cube).solve();
        new YellowCrossStep(cube).solve();
        new YellowEdgesStep(cube).solve();
        new PositionYellowCornersStep(cube).solve();
        new OrientYellowCornersStep(cube).solve();
        assertSolved(cube);
    }

    private static void assertSolved(Cube cube) {
        for (var color : FaceColor.values()) {
            assertEquals(Side.all(color), cube.side(color));
        }
    }
}
