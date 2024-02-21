package sh.grover.dcubed.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SideTest {

    private static final int[] COLORS = {
            FaceColor.WHITE, FaceColor.RED, FaceColor.ORANGE, FaceColor.YELLOW,
            FaceColor.GREEN, FaceColor.BLUE, FaceColor.WHITE, FaceColor.RED,
    };

    private static final long ENCODED = 0x0001020304050001L;

    @Test
    void encoded() {
        var side = new Side(COLORS);
        assertEquals(ENCODED, side.encoded());
    }

    @Test
    void toColors() {
        var side = new Side(ENCODED);
        assertArrayEquals(COLORS, side.toColors());
    }

    @Test
    void all() {
        for (var color : FaceColor.values()) {
            var all = Side.all(color);
            var manual = new Side(color, color, color, color, color, color, color, color);
            assertEquals(manual.encoded(), all.encoded());
        }
    }

    @Test
    void checker() {
        for (var color1 : FaceColor.values()) {
            for (var color2 : FaceColor.values()) {
                var checker = Side.checker(color1, color2);
                var manual = new Side(color1, color2, color1, color2, color1, color2, color1, color2);
                assertEquals(manual.encoded(), checker.encoded());
            }
        }
    }

    @Test
    void testEquals() {
        assertEquals(new Side(COLORS), new Side(ENCODED));
        assertNotEquals(new Side(COLORS), new Side(ENCODED + 1));
    }

    @Test
    void testHashCode() {
        assertEquals(new Side(COLORS).hashCode(), new Side(ENCODED).hashCode());
        assertNotEquals(new Side(COLORS).hashCode(), new Side(ENCODED + 1).hashCode());
    }
}