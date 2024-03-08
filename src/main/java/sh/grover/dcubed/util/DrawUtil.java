package sh.grover.dcubed.util;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DrawUtil {

    private static final SimpleDateFormat DEBUG_IMAGE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public static final Scalar RED = new Scalar(0, 0, 255);
    public static final Scalar YELLOW = new Scalar(0, 255, 255);
    public static final Scalar GREEN = new Scalar(0, 255, 0);
    public static final Scalar CYAN = new Scalar(255, 255, 0);
    public static final Scalar BLUE = new Scalar(255, 0, 0);
    public static final Scalar MAGENTA = new Scalar(255, 0, 255);

    public static void point(Mat image, Point point, Scalar color) {
        Imgproc.circle(image, point, 4, color, -1);
    }

    public static void point(Mat image, Point point) {
        Imgproc.circle(image, point, 4, RED, -1);
    }

    public static void line(Mat image, double rho, double theta, Scalar color) {
        var twoPointLine = MathUtil.polarToTwoPointLine(rho, theta);
        Imgproc.line(image, twoPointLine.a(), twoPointLine.b(), color, 1, Imgproc.LINE_AA, 0);
    }

    public static void lines(Mat image, Mat lines, List<Integer> indices, Scalar color) {
        for (var index : indices) {
            var rho = lines.get(index, 0)[0];
            var theta = lines.get(index, 0)[1];
            line(image, rho, theta, color);
        }
    }

    public static void debugWrite(Mat image, String label) {
        var imageName = DEBUG_IMAGE_DATE_FORMAT.format(new Date()) + "-" + label + ".jpg";
        if (!Imgcodecs.imwrite(imageName, image)) {
            throw new RuntimeException("failed to save image");
        }
    }
}
