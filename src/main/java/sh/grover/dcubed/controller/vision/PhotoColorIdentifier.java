package sh.grover.dcubed.controller.vision;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import sh.grover.dcubed.controller.vision.segment.HttpCubeSegmenter;
import sh.grover.dcubed.controller.vision.segment.ICubeSegmenter;
import sh.grover.dcubed.model.Side;
import sh.grover.dcubed.model.vision.ColorScanException;
import sh.grover.dcubed.model.vision.FaceColorExtractor;
import sh.grover.dcubed.model.vision.segment.CubeSegmentation;
import sh.grover.dcubed.util.DrawUtil;
import sh.grover.dcubed.util.MathUtil;

import java.net.URI;

public class PhotoColorIdentifier implements IColorIdentifier {

    private final ICubeSegmenter segmenter;
    private final boolean debug;

    public PhotoColorIdentifier(boolean debug) {
        this.segmenter = new HttpCubeSegmenter(URI.create("http://localhost:5000"));
        this.debug = debug;
    }

    @Override
    public Side[] estimateColors(Mat image) throws ColorScanException {
        CubeSegmentation segmentation;
        try {
            segmentation = this.segmenter.segment(image);
        } catch (Exception e) {
            throw new ColorScanException("failed to segment image", e);
        }

        var cropFrom = new Point(segmentation.lowestX(), segmentation.lowestY());
        var cropTo = new Point(segmentation.highestX(), segmentation.highestY());

        if (this.debug) {
            var annotated = new Mat();
            image.copyTo(annotated);

            DrawUtil.segmentation(annotated, segmentation);
            Imgproc.rectangle(annotated, cropFrom, cropTo, new Scalar(255, 255, 255));
            DrawUtil.debugWrite(annotated, "points");
        }

        var range = new Rect(cropFrom, cropTo);
        var cropped = image.submat(range);
        var croppedSegmentation = segmentation.subtract(cropFrom.x, cropFrom.y);

        var leftFace = warpedCrop(cropped, croppedSegmentation.topLeft(), croppedSegmentation.center(), croppedSegmentation.bottom(), croppedSegmentation.bottomLeft());
        var rightFace = warpedCrop(cropped, croppedSegmentation.center(), croppedSegmentation.topRight(), croppedSegmentation.bottomRight(), croppedSegmentation.bottom());
        var topFace = warpedCrop(cropped, croppedSegmentation.top(), croppedSegmentation.topRight(), croppedSegmentation.center(), croppedSegmentation.topLeft());

        if (this.debug) {
            DrawUtil.debugWrite(leftFace, "left-face");
            DrawUtil.debugWrite(rightFace, "right-face");
            DrawUtil.debugWrite(topFace, "top-face");
        }

        return new FaceColorExtractor(this.debug, topFace, leftFace, rightFace).result();
    }

    private static Mat warpedCrop(Mat image, Point tl, Point tr, Point br, Point bl) {
        var width = MathUtil.greatestLength(br, bl, tr, tl);
        var height = MathUtil.greatestLength(tr, br, tl, bl);

        var perspectiveSrc = new MatOfPoint2f(tl, tr, br, bl);
        var perspectiveDst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(width - 1, 0),
                new Point(width - 1, height - 1),
                new Point(0, height - 1)
        );

        var matrix = Imgproc.getPerspectiveTransform(perspectiveSrc, perspectiveDst);
        var warped = new Mat();
        Imgproc.warpPerspective(image, warped, matrix, new Size(width, height));
        return warped;
    }
}
