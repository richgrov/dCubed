package sh.grover.dcubed.util;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DrawUtil {

    private static final SimpleDateFormat DEBUG_IMAGE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public static void debugWrite(Mat image, String label) {
        var imageName = DEBUG_IMAGE_DATE_FORMAT.format(new Date()) + "-" + label + ".jpg";
        if (!Imgcodecs.imwrite(imageName, image)) {
            throw new RuntimeException("failed to save image");
        }
    }
}
