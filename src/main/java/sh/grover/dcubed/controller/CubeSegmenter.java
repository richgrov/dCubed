package sh.grover.dcubed.controller;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CubeSegmenter {

    private static final Size GAUSSIAN_KERNEL_SIZE = new Size(5, 3);
    private static final Scalar CONTOUR_COLOR1 = new Scalar(255, 255, 255);

    private final Mat hsv = new Mat();
    private final Mat foregroundSegment = new Mat();
    private final Mat blurredForeground = new Mat();
    private final Mat hierarchyUnused = new Mat();
    private final List<MatOfPoint> contourPoints = new ArrayList<>();
    private final List<MatOfPoint> filteredContours = new ArrayList<>();

    public void segmentFaces(Mat in, Mat out) {
        var img = this.segmentCube(in);
        img.copyTo(out);
    }

    private Mat segmentCube(Mat input) {
        var foreground = this.segmentForeground(input);
        this.contour(foreground);
        Imgproc.drawContours(input, this.filteredContours, -1, CONTOUR_COLOR1, 3);
        return input;
    }

    private Mat segmentForeground(Mat input) {
        Imgproc.cvtColor(input, this.hsv, Imgproc.COLOR_BGR2HSV);
        Core.inRange(this.hsv, new Scalar(0, 0, 50), new Scalar(255, 255, 255), this.foregroundSegment);
        Imgproc.medianBlur(this.foregroundSegment, this.blurredForeground, 11);
        return this.blurredForeground;
    }

    private void contour(Mat input) {
        this.contourPoints.clear();
        Imgproc.findContours(input, this.contourPoints, this.hierarchyUnused, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        this.filteredContours.clear();
        for (var contour : this.contourPoints) {
            var points = contour.toArray();
            if (points.length < 6) {
                continue;
            }

            var approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, 15, true);

            var area = Imgproc.contourArea(contour);
            if (area < 10000) {
                continue;
            }

            var smoothPoints = approx.toArray();
            if (smoothPoints.length != 6) {
                continue;
            }

            this.filteredContours.add(new MatOfPoint(smoothPoints));
        }
    }
}
