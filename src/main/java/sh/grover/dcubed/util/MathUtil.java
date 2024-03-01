package sh.grover.dcubed.util;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

public class MathUtil {

    public static final Scalar RED = new Scalar(0, 0, 255);
    public static final Scalar GREEN = new Scalar(0, 255, 0);
    public static final Scalar BLUE = new Scalar(255, 0, 0);

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

    public static Point lineIntersection(Point lineA1, Point lineA2, Point lineB1, Point lineB2) {
        var denominator = (lineA1.x - lineA2.x)*(lineB1.y - lineB2.y) - (lineA1.y - lineA2.y)*(lineB1.x - lineB2.y);
        if (denominator == 0) {
            return null;
        }

        var numeratorX = (lineA1.x*lineA2.y - lineA1.y*lineA2.x)*(lineB1.x - lineB2.x) - (lineA1.x - lineA2.x)*(lineB1.x*lineB2.y - lineB1.y*lineB2.x);
        var numeratorY = (lineA1.x*lineA2.y - lineA1.y*lineA2.x)*(lineB1.y - lineB2.y) - (lineA1.y - lineA2.y)*(lineB1.x*lineB2.y - lineB1.y*lineB2.x);

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
     * See <a href="https://docs.opencv.org/3.4/d9/db0/tutorial_hough_lines.html">OpenCV</a>
     * @param rho
     * @param theta
     * @return
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

    public static Point subtract(Point a, Point b) {
        return new Point(a.x - b.x, a.y - b.y);
    }
}
