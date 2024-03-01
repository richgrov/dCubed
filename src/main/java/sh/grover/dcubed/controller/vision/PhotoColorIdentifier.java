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
import sh.grover.dcubed.util.PolarLine;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhotoColorIdentifier implements IColorIdentifier {

    private static final Size GAUSSIAN_BLUR_KSIZE = new Size(3, 3);
    private static final Mat DILATION_KERNEL = Imgproc.getStructuringElement(1, new Size(3, 3));
    private static final int HOUGH_LINE_THRESHOLD = 150;
    private static final double LINE_SEGMENTATION_DEVIATION = 20;
    private static final int NEAR_LINE_DISTANCE = 20;

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
            DrawUtil.point(annotated, segmentation.top());
            DrawUtil.point(annotated, segmentation.topLeft());
            DrawUtil.point(annotated, segmentation.bottomLeft());
            DrawUtil.point(annotated, segmentation.bottom());
            DrawUtil.point(annotated, segmentation.bottomRight());
            DrawUtil.point(annotated, segmentation.topRight());

            Imgproc.rectangle(annotated, cropFrom, cropTo, new Scalar(255, 255, 255));
            DrawUtil.debugWrite(annotated, "points");
        }

        var range = new Rect(cropFrom, cropTo);
        var cropped = image.submat(range);
        var croppedSegmentation = segmentation.subtract(cropFrom.x, cropFrom.y);

        if (this.debug) {
            var annotatedCrop = new Mat();
            cropped.copyTo(annotatedCrop);

            DrawUtil.point(annotatedCrop, croppedSegmentation.top());
            DrawUtil.point(annotatedCrop, croppedSegmentation.topLeft());
            DrawUtil.point(annotatedCrop, croppedSegmentation.bottomLeft());
            DrawUtil.point(annotatedCrop, croppedSegmentation.bottom());
            DrawUtil.point(annotatedCrop, croppedSegmentation.bottomRight());
            DrawUtil.point(annotatedCrop, croppedSegmentation.topRight());
            DrawUtil.debugWrite(annotatedCrop, "cropped");
        }

        var verticalLines = new ArrayList<Integer>();
        var slopeDownLines = new ArrayList<Integer>();
        var slopeUpLines = new ArrayList<Integer>();
        var lines = this.findAxisLines(cropped, verticalLines, slopeDownLines, slopeUpLines);

        if (this.debug) {
            var annotatedCrop = new Mat();
            cropped.copyTo(annotatedCrop);

            DrawUtil.lines(annotatedCrop, lines, verticalLines, DrawUtil.GREEN);
            DrawUtil.lines(annotatedCrop, lines, slopeDownLines, DrawUtil.BLUE);
            DrawUtil.lines(annotatedCrop, lines, slopeUpLines, DrawUtil.RED);
            DrawUtil.debugWrite(annotatedCrop, "lines");
        }

        var vertical = medianLineClosestToPoint(lines, verticalLines, croppedSegmentation.bottom());
        var slopeDown = medianLineClosestToPoint(lines, slopeDownLines, croppedSegmentation.topLeft());
        var slopeUp = medianLineClosestToPoint(lines, slopeUpLines, croppedSegmentation.topRight());

        try {
            Objects.requireNonNull(vertical, "median vertical was null");
            Objects.requireNonNull(slopeDown, "median down was null");
            Objects.requireNonNull(slopeUp, "median up was null");
        } catch (NullPointerException cause) {
            throw new ColorScanException(cause);
        }

        var twoPointVertical = MathUtil.polarToTwoPointLine(vertical.rho(), vertical.theta());
        var twoPointDown = MathUtil.polarToTwoPointLine(slopeDown.rho(), slopeDown.theta());
        var twoPointUp = MathUtil.polarToTwoPointLine(slopeUp.rho(), slopeUp.theta());

        var verticalDownIntersect = MathUtil.lineIntersection(twoPointVertical, twoPointDown);
        var verticalUpIntersect = MathUtil.lineIntersection(twoPointVertical, twoPointUp);
        var upDownIntersect = MathUtil.lineIntersection(twoPointUp, twoPointDown);

        if (this.debug) {
            var annotatedCrop = new Mat();
            cropped.copyTo(annotatedCrop);

            DrawUtil.line(annotatedCrop, vertical.rho(), vertical.theta(), DrawUtil.GREEN);
            DrawUtil.line(annotatedCrop, slopeDown.rho(), slopeDown.theta(), DrawUtil.BLUE);
            DrawUtil.line(annotatedCrop, slopeUp.rho(), slopeUp.theta(), DrawUtil.RED);

            if (verticalDownIntersect != null) {
                DrawUtil.point(annotatedCrop, verticalDownIntersect, new Scalar(255, 255, 0));
            }

            if (verticalUpIntersect != null) {
                DrawUtil.point(annotatedCrop, verticalUpIntersect, new Scalar(0, 255, 255));
            }

            if (upDownIntersect != null) {
                DrawUtil.point(annotatedCrop, upDownIntersect, new Scalar(255, 0, 255));
            }

            DrawUtil.debugWrite(annotatedCrop, "filtered");
        }

        var leftFace = warpedCrop(cropped, croppedSegmentation.topLeft(), verticalDownIntersect, croppedSegmentation.bottom(), croppedSegmentation.bottomLeft());
        var rightFace = warpedCrop(cropped, verticalUpIntersect, croppedSegmentation.topRight(), croppedSegmentation.bottomRight(), croppedSegmentation.bottom());
        var topFace = warpedCrop(cropped, croppedSegmentation.top(), croppedSegmentation.topRight(), upDownIntersect, croppedSegmentation.topLeft());

        if (this.debug) {
            DrawUtil.debugWrite(leftFace, "left-face");
            DrawUtil.debugWrite(rightFace, "right-face");
            DrawUtil.debugWrite(topFace, "top-face");
        }

        return new FaceColorExtractor(this.debug, topFace, leftFace, rightFace).result();
    }

    private Mat findAxisLines(Mat image, List<Integer> vertical, List<Integer> slopeDown, List<Integer> slopeUp) {
        var optimized = this.optimizeImageForLineDetection(image);

        var lines = new Mat();
        Imgproc.HoughLines(optimized, lines, 1, Math.PI / 180, HOUGH_LINE_THRESHOLD);

        for (var iLine = 0; iLine < lines.rows(); iLine++) {
            var theta = lines.get(iLine, 0)[1];

            if (isVertical(theta)) {
                vertical.add(iLine);
            } else if (slopesDown(theta)) {
                slopeDown.add(iLine);
            } else if (slopeUp(theta)) {
                slopeUp.add(iLine);
            }
        }

        return lines;
    }

    private Mat optimizeImageForLineDetection(Mat image) {
        var blurred = new Mat();
        Imgproc.GaussianBlur(image, blurred, GAUSSIAN_BLUR_KSIZE, 0);
        if (this.debug) {
            DrawUtil.debugWrite(blurred, "blurred");
        }

        var canny = new Mat();
        Imgproc.Canny(blurred, canny, 24, 24*3);
        if (this.debug) {
            DrawUtil.debugWrite(canny, "canny");
        }

        var dilated = new Mat();
        Imgproc.dilate(canny, dilated, DILATION_KERNEL);
        if (this.debug) {
            DrawUtil.debugWrite(dilated, "dilated");
        }
        return dilated;
    }

    private PolarLine medianLineClosestToPoint(Mat lines, List<Integer> indices, Point point) {
        var nearby = new ArrayList<Integer>();
        for (var index : indices) {
            var rho = lines.get(index, 0)[0];
            var theta = lines.get(index, 0)[1];
            var twoPointLine = MathUtil.polarToTwoPointLine(rho, theta);

            var distance = MathUtil.distanceFromPointToLine(point, twoPointLine.a(), twoPointLine.b());
            if (distance <= NEAR_LINE_DISTANCE) {
                nearby.add(index);
            }
        }

        if (nearby.isEmpty()) {
            return null;
        }

        return MathUtil.medianLine(lines, nearby);
    }

    private static boolean isVertical(double theta) {
        return theta < Math.toRadians(LINE_SEGMENTATION_DEVIATION)
                || theta > Math.toRadians(180 - LINE_SEGMENTATION_DEVIATION);
    }

    private static boolean slopesDown(double theta) {
        return theta > Math.toRadians(120 - LINE_SEGMENTATION_DEVIATION)
                && theta < Math.toRadians(120 + LINE_SEGMENTATION_DEVIATION);
    }

    private static boolean slopeUp(double theta) {
        return theta > Math.toRadians(60 - LINE_SEGMENTATION_DEVIATION)
                && theta < Math.toRadians(60 + LINE_SEGMENTATION_DEVIATION);
    }

    private static Mat warpedCrop(Mat image, Point tl, Point tr, Point br, Point bl) {
        var width1 = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        var width2 = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));
        var width = Math.max((int) width1, (int) width2);

        var height1 = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        var height2 = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));
        var height = Math.max((int) height1, (int) height2);

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
