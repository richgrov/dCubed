package sh.grover.dcubed.controller.vision;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sh.grover.dcubed.controller.vision.segment.HttpCubeSegmenter;
import sh.grover.dcubed.controller.vision.segment.ICubeSegmenter;
import sh.grover.dcubed.model.vision.ColorScanException;
import sh.grover.dcubed.model.vision.segment.CubeSegmentation;

import java.net.URI;

public class PhotoColorIdentifier implements IColorIdentifier {

    private final ICubeSegmenter segmenter;

    public PhotoColorIdentifier() {
        this.segmenter = new HttpCubeSegmenter(URI.create("http://localhost:5000"));
    }

    @Override
    public ScannedSide[] estimateColors(Mat image) throws ColorScanException {
        CubeSegmentation segmentation;
        try {
            segmentation = this.segmenter.segment(image);
        } catch (Exception e) {
            throw new ColorScanException("failed to segment image", e);
        }

        var annotated = new Mat();
        image.copyTo(annotated);
        point(annotated, segmentation.top);
        point(annotated, segmentation.topLeft);
        point(annotated, segmentation.bottomLeft);
        point(annotated, segmentation.bottom);
        point(annotated, segmentation.bottomRight);
        point(annotated, segmentation.topRight);
        Imgcodecs.imwrite("photo.jpg", annotated);

        return new ScannedSide[0];
    }

    private static void point(Mat image, Point point) {
        var pixelCoordinates = new Point(point.x * image.width(), point.y * image.height());
        Imgproc.circle(image, pixelCoordinates, 4, new Scalar(0, 0, 255), -1);
    }
}
