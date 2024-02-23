package sh.grover.dcubed.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CornerPieceTest {

    @Test
    void equals() {
        var p1 = new CornerPiece(FaceColor.WHITE, FaceColor.RED, FaceColor.GREEN);
        var p2 = new CornerPiece(FaceColor.RED, FaceColor.GREEN, FaceColor.WHITE);
        assertEquals(p1, p2);

        var p3 = new CornerPiece(FaceColor.WHITE, FaceColor.ORANGE, FaceColor.GREEN);
        assertNotEquals(p1, p3);
    }
}