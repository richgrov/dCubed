package sh.grover.dcubed.controller.vision;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import sh.grover.dcubed.controller.vision.segment.HttpCubeSegmenter;
import sh.grover.dcubed.controller.vision.segment.ICubeSegmenter;
import sh.grover.dcubed.model.vision.ColorScanException;
import sh.grover.dcubed.model.vision.StepDebugLevel;
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
    private final StepDebugLevel debugLevel;

    public PhotoColorIdentifier(StepDebugLevel debugLevel) {
        this.segmenter = new HttpCubeSegmenter(URI.create("http://localhost:5000"));
        this.debugLevel = debugLevel;
    }

    @Override
    public ScannedSide[] estimateColors(Mat image) throws ColorScanException {
        CubeSegmentation segmentation;
        try {
            segmentation = this.segmenter.segment(image);
        } catch (Exception e) {
            throw new ColorScanException("failed to segment image", e);
        }

        var top = segmentation.lowestY();
        var bottom = segmentation.highestY();
        var left = segmentation.lowestX();
        var right = segmentation.highestX();
        var cropped = image.submat((int) top, (int) bottom, (int) left, (int) right);
        var croppedSegmentation = segmentation.subtract(left, top);

        if (this.debugLevel == StepDebugLevel.ALL) {
            var annotatedCrop = new Mat();
            cropped.copyTo(annotatedCrop);

            point(annotatedCrop, croppedSegmentation.top());
            point(annotatedCrop, croppedSegmentation.topLeft());
            point(annotatedCrop, croppedSegmentation.bottomLeft());
            point(annotatedCrop, croppedSegmentation.bottom());
            point(annotatedCrop, croppedSegmentation.bottomRight());
            point(annotatedCrop, croppedSegmentation.topRight());
            DrawUtil.debugWrite(annotatedCrop, "cropped");
        }

        var verticalLines = new ArrayList<Integer>();
        var slopeDownLines = new ArrayList<Integer>();
        var slopeUpLines = new ArrayList<Integer>();
        var lines = this.findAxisLines(cropped, verticalLines, slopeDownLines, slopeUpLines);

        if (this.debugLevel == StepDebugLevel.ALL) {
            var annotatedCrop = new Mat();
            cropped.copyTo(annotatedCrop);

            drawLines(annotatedCrop, lines, verticalLines, MathUtil.GREEN);
            drawLines(annotatedCrop, lines, slopeDownLines, MathUtil.BLUE);
            drawLines(annotatedCrop, lines, slopeUpLines, MathUtil.RED);
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

        if (this.debugLevel == StepDebugLevel.ALL) {
            var annotatedCrop = new Mat();
            cropped.copyTo(annotatedCrop);

            drawLine(annotatedCrop, vertical.rho(), vertical.theta(), MathUtil.GREEN);
            drawLine(annotatedCrop, slopeDown.rho(), slopeDown.theta(), MathUtil.BLUE);
            drawLine(annotatedCrop, slopeUp.rho(), slopeUp.theta(), MathUtil.RED);
        }

        var twoPointVertical = MathUtil.polarToTwoPointLine(vertical.rho(), vertical.theta());
        var twoPointUp = MathUtil.polarToTwoPointLine(slopeUp.rho(), slopeUp.theta());

        var tl = MathUtil.lineIntersection(twoPointVertical.a(), twoPointVertical.b(), twoPointUp.a(), twoPointUp.b());
        var rightFace = warpedCrop(cropped, tl, croppedSegmentation.topRight(), croppedSegmentation.bottomRight(), croppedSegmentation.bottom());

        if (this.debugLevel == StepDebugLevel.ALL) {
        }

        if (this.debugLevel == StepDebugLevel.ALL || false /* failure */) {
            DrawUtil.debugWrite(image, "start");
        }

        return new ScannedSide[0];
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

        var canny = new Mat();
        Imgproc.Canny(blurred, canny, 24, 24*3);

        var dilated = new Mat();
        Imgproc.dilate(canny, dilated, DILATION_KERNEL);
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

    private static void point(Mat image, Point point) {
        var pixelCoordinates = new Point(point.x, point.y);
        Imgproc.circle(image, pixelCoordinates, 4, new Scalar(0, 0, 255), -1);
    }

    private static void drawLines(Mat image, Mat lines, List<Integer> indices, Scalar color) {
        for (var index : indices) {
            var rho = lines.get(index, 0)[0];
            var theta = lines.get(index, 0)[1];
            drawLine(image, rho, theta, color);
        }
    }

    private static void drawLine(Mat image, double rho, double theta, Scalar color) {
        var twoPointLine = MathUtil.polarToTwoPointLine(rho, theta);
        Imgproc.line(image, twoPointLine.a(), twoPointLine.b(), color, 3, Imgproc.LINE_AA, 0);
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
