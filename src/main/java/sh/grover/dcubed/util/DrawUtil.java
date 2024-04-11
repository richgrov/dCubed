package sh.grover.dcubed.util;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sh.grover.dcubed.model.vision.segment.CubeSegmentation;

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

    public static void segmentation(Mat image, CubeSegmentation segmentation) {
        var points = new MatOfPoint(
                segmentation.top(),
                segmentation.topLeft(),
                segmentation.bottomLeft(),
                segmentation.bottom(),
                segmentation.bottomRight(),
                segmentation.topRight()
        );
        Imgproc.polylines(image, List.of(points), true, GREEN);

        point(image, segmentation.top(), MAGENTA);
        point(image, segmentation.topLeft(), RED);
        point(image, segmentation.bottomLeft(), YELLOW);
        point(image, segmentation.bottom(), GREEN);
        point(image, segmentation.bottomRight(), CYAN);
        point(image, segmentation.topRight(), BLUE);
    }

    public static void debugWrite(Mat image, String label) {
        var imageName = DEBUG_IMAGE_DATE_FORMAT.format(new Date()) + "-" + label + ".jpg";
        if (!Imgcodecs.imwrite(imageName, image)) {
            throw new RuntimeException("failed to save image");
        }
    }
}
