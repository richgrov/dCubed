package sh.grover.dcubed.model;

public class FaceColor {

    public static final int WHITE = 0;
    public static final int RED = 1;
    public static final int ORANGE = 2;
    public static final int YELLOW = 3;
    public static final int GREEN = 4;
    public static final int BLUE = 5;
    
    public static void requireValid(int color) {
        if (color < WHITE || color > BLUE) {
            throw new IllegalArgumentException("invalid color " + color);
        }
    }

    public static int[] values() {
        return new int[] { WHITE, RED, ORANGE, YELLOW, GREEN, BLUE };
    }

    public static String toString(int color) {
        return switch (color) {
            case 0 -> "WHITE";
            case 1 -> "RED";
            case 2 -> "ORANGE";
            case 3 -> "YELLOW";
            case 4 -> "GREEN";
            case 5 -> "BLUE";
            default -> throw new IllegalArgumentException(color + "can't be converted to string");
        };
    }

    private FaceColor() {
    }
}
