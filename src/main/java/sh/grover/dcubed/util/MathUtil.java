package sh.grover.dcubed.util;

import org.opencv.core.Point;

public class MathUtil {

    /**
     * Finds which line, A or B, has the longest length. The terms "starting"
     * and "ending" purely refer to opposite ends on the line. Order does not
     * matter.
     * @param a1 Starting point of line A
     * @param a2 Ending point of line A
     * @param b1 Starting point of line B
     * @param b2 Ending point of line B
     * @return The length of whichever line is longer
     */
    public static int greatestLength(Point a1, Point a2, Point b1, Point b2) {
        var len1 = Math.sqrt(Math.pow(a1.x - a2.x, 2) + Math.pow(a1.y - a2.y, 2));
        var len2 = Math.sqrt(Math.pow(b1.x - b2.x, 2) + Math.pow(b1.y - b2.y, 2));
        return Math.max((int) len1, (int) len2);
    }

    public static Point subtract(Point a, Point b) {
        return new Point(a.x - b.x, a.y - b.y);
    }
}
