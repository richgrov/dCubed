package sh.grover.dcubed.util;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class MathUtil {

    public static double median(List<Double> vals) {
        if (vals.size() % 2 == 0) {
            var a = vals.get(vals.size() / 2 - 1);
            var b = vals.get(vals.size() / 2);
            return (a + b) / 2;
        } else {
            return vals.get(vals.size() / 2);
        }
    }

    /**
     * Gets the distance from a point to a line defined by two points extended infinitely.
     * See <a href="https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line">Wikipedia</a>
     * @param point The point to check the distance from
     * @param lineA One point of the line
     * @param lineB Another point of the line
     * @return The distance from the point to the line
     */
    public static double distanceFromPointToLine(Point point, Point lineA, Point lineB) {
        var numerator = Math.abs((lineB.x - lineA.x)*(lineA.y - point.y) - (lineA.x - point.x)*(lineB.y - lineA.y));
        var denominator = Math.sqrt(Math.pow(lineB.x - lineA.x, 2) + Math.pow(lineB.y - lineA.y, 2));
        return numerator / denominator;
    }

    /**
     * Gets the point where two lines intersect.
     * See <a href="https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection#Given_two_points_on_each_line">Wikipedia</a>
     * @param a Line A
     * @param b Line B
     * @return The point where the two lines intersect
     */
    public static Point lineIntersection(TwoPointLine a, TwoPointLine b) {
        var x1 = a.a().x;
        var y1 = a.a().y;
        var x2 = a.b().x;
        var y2 = a.b().y;
        var x3 = b.a().x;
        var y3 = b.a().y;
        var x4 = b.b().x;
        var y4 = b.b().y;

        var denominator = (x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4);
        if (denominator == 0) {
            return null;
        }

        var numeratorX = (x1*y2 - y1*x2)*(x3 - x4) - (x1 - x2)*(x3*y4 - y3*x4);
        var numeratorY = (x1*y2 - y1*x2)*(y3 - y4) - (y1 - y2)*(x3*y4 - y3*x4);
        return new Point(numeratorX / denominator, numeratorY / denominator);
    }

    public static PolarLine medianLine(Mat lines, List<Integer> indices) {
        var rho = new ArrayList<Double>(indices.size());
        var theta = new ArrayList<Double>(indices.size());

        for (var index : indices) {
            rho.add(lines.get(index, 0)[0]);
            theta.add(lines.get(index, 0)[1]);
        }

        return new PolarLine(median(rho), median(theta));
    }

    /**
     * Converts a polar line to two points on that line. The exact distances
     * and positions have no guarantees, only that they lie somewhere on the
     * line.
     * See <a href="https://docs.opencv.org/3.4/d9/db0/tutorial_hough_lines.html">OpenCV</a>
     * @param rho The line's rho component
     * @param theta The line's theta component
     * @return An object holding both points on the line.
     */
    public static TwoPointLine polarToTwoPointLine(double rho, double theta) {
        var a = Math.cos(theta);
        var b = Math.sin(theta);
        var x0 = a*rho;
        var y0 = b*rho;

        return new TwoPointLine(
                new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a))),
                new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)))
        );
    }

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
