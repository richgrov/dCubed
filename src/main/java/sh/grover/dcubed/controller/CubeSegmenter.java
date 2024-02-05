package sh.grover.dcubed.controller;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CubeSegmenter {

    private static final Size GAUSSIAN_KERNEL_SIZE = new Size(5, 3);
    private static final Scalar CONTOUR_COLOR1 = new Scalar(255, 255, 255);

    private final Mat intermediate = new Mat();
    private final Mat intermediateCanny = new Mat();
    private final Mat hierarchyUnused = new Mat();
    private final Mat maskOutput = new Mat();
    private final List<MatOfPoint> contourPoints = new ArrayList<>();
    private final List<MatOfPoint> filteredContours = new ArrayList<>();

    public void segmentFaces(Mat in, Mat out) {
        var img = this.segmentCube(in);
        img.copyTo(out);
    }

    private Mat segmentCube(Mat input) {
        var outlined = this.outlineFrame(input);
        var contours = this.findMajorContours(outlined);
        if (contours.isEmpty()) {
            System.out.println("empty");
            return outlined;
        }

        var masked = this.maskFromContours(input, contours);

        var kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15));
        Imgproc.erode(masked, masked, kernel);
        Imgproc.cvtColor(masked, masked, Imgproc.COLOR_BGR2GRAY);
        var contours2 = this.findMajorContours(masked);
        var approxCurves = new ArrayList<MatOfPoint>();
        for (var contour : contours2) {
            var approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, 15, true);

            var points = approx.toArray();
            if (points.length == 6) {
                approxCurves.add(new MatOfPoint(points));
            }
        }

        if (approxCurves.size() == 1) {
            Imgproc.drawContours(input, approxCurves, -1, CONTOUR_COLOR1, 3);
        }

        return input;
    }

    private Mat outlineFrame(Mat input) {
        Imgproc.medianBlur(input, this.intermediate, 15);
        Imgproc.Canny(this.intermediate, this.intermediateCanny, 20, 30);
        var kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15));
        Imgproc.dilate(this.intermediateCanny, this.intermediateCanny, kernel);
        return this.intermediateCanny;
    }

    private List<MatOfPoint> findMajorContours(Mat input) {
        this.contourPoints.clear();
        Imgproc.findContours(input, this.contourPoints, this.hierarchyUnused, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        this.filteredContours.clear();
        for (var contour : this.contourPoints) {
            if (isValidContour(contour)) {
                this.filteredContours.add(contour);
            }
        }

        return this.filteredContours;
    }

    private static boolean isValidContour(MatOfPoint contour) {
        var points = contour.toArray();
        if (points.length < 6) {
            return false;
        }

        var area = Imgproc.contourArea(contour);
        return area > 5000;
    }

    private Mat maskFromContours(Mat image, List<MatOfPoint> contours) {
        var mask = Mat.zeros(image.size(), CvType.CV_8UC(image.channels()));
        Imgproc.drawContours(mask, contours, -1, CONTOUR_COLOR1, -1);
        Core.bitwise_and(image, mask, this.maskOutput);
        return this.maskOutput;
    }
}
