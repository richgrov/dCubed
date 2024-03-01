package sh.grover.dcubed.model.vision;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import sh.grover.dcubed.model.Cube;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Side;
import sh.grover.dcubed.util.DrawUtil;

public class FaceColorExtractor {

    private static final int KMEANS_CLUSTERS = 2;
    private static final TermCriteria KMEANS_CRITERIA = new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, 200, 0.1);

    private static final int ANNOTATION_CIRCLE_RADIUS = 10;
    private static final int COLOR_NOT_FOUND = -1;

    private static final Scalar[] FACE_COLORS_TO_BGR = new Scalar[] {
            new Scalar(255, 255, 255), // white
            new Scalar(0, 0, 255), // red
            new Scalar(0, 125, 255), // orange
            new Scalar(0, 255, 255), // yellow
            new Scalar(0, 255, 0), // green
            new Scalar(255, 0, 0), // blue
    };

    private final boolean debug;
    private final Side[] result = new Side[6];
    private Mat currentAnnotation;

    public FaceColorExtractor(boolean debug, Mat top, Mat left, Mat right) {
        this.debug = debug;
        var topColors = this.extractColors(top, "top");
        var leftColors = this.extractColors(left, "left");
        var rightColors = this.extractColors(right, "right");

        this.processSide(topColors, leftColors, 2);
        this.processSide(leftColors, topColors, 0);
        this.processSide(rightColors, topColors, 0);
    }

    public Side[] result() {
        return this.result;
    }

    private int[] extractColors(Mat image, String debugName) {
        System.out.println("----" + debugName + "-----");
        if (this.debug) {
            this.currentAnnotation = new Mat();
            image.copyTo(this.currentAnnotation);
        }

        var converted = new Mat();
        image.convertTo(converted, CvType.CV_32F);

        var faceWidth = image.width() / 3;
        var faceHeight = image.height() / 3;
        var cropped = new Mat();

        var colors = new int[9];
        for (var x = 0; x < 3; x++) {
            for (var y = 0; y < 3; y++) {
                var range = new Rect(x * faceWidth, y * faceHeight, faceWidth, faceHeight);

                converted.submat(range).copyTo(cropped);
                var annotationPos = new Point(range.x + ANNOTATION_CIRCLE_RADIUS, range.y + ANNOTATION_CIRCLE_RADIUS);
                var color = this.getDominantFaceColor(cropped, annotationPos);
                colors[y * 3 + x] = color;

                if (this.currentAnnotation != null) {
                    Imgproc.circle(this.currentAnnotation, new Point(range.br().x - ANNOTATION_CIRCLE_RADIUS, range.br().y - ANNOTATION_CIRCLE_RADIUS), 10, FACE_COLORS_TO_BGR[color], -1);
                }
            }
        }

        if (this.currentAnnotation != null) {
            DrawUtil.debugWrite(this.currentAnnotation, debugName);
        }
        return colors;
    }

    private int getDominantFaceColor(Mat image, Point annotationCirclePos) {
        var color = image.get(image.width() / 2, image.height() / 2);
        var estimate = closestFaceColor(color);
        if (estimate == -1) {
            estimate = FaceColor.WHITE;
        }
        if (this.currentAnnotation != null) {
            Imgproc.circle(this.currentAnnotation, annotationCirclePos, ANNOTATION_CIRCLE_RADIUS, new Scalar(color), -1);
        }
        return estimate;
    }
    /*private int getDominantFaceColor(Mat image, Point annotationCirclePos) {
        var pixels = image.reshape(0, image.width() * image.height());

        var labels = new Mat();
        var centers = new Mat();
        Core.kmeans(pixels, KMEANS_CLUSTERS, labels, KMEANS_CRITERIA, 10, Core.KMEANS_RANDOM_CENTERS, centers);
        System.out.println(centers.width() + " * " + centers.height());

        var estimatedColor = COLOR_NOT_FOUND;

        for (var center = 0; center < centers.width(); center++) {
            //var index = (int) labels.get(iLabel, 0)[0];
            var color = new double[] {
                    centers.get(center, 0)[0],
                    centers.get(center, 1)[0],
                    centers.get(center, 2)[0],
            };
            if (this.currentAnnotation != null) {
                Imgproc.circle(this.currentAnnotation, new Point(annotationCirclePos.x + ANNOTATION_CIRCLE_RADIUS * center, annotationCirclePos.y), ANNOTATION_CIRCLE_RADIUS, new Scalar(color), -1);
            }
            estimatedColor = closestFaceColor(color);
            if (estimatedColor != COLOR_NOT_FOUND) {
                break;
            }
        }

        if (estimatedColor == COLOR_NOT_FOUND) {
            estimatedColor = FaceColor.WHITE;
        }
        return estimatedColor;
    }*/

    private void processSide(int[] scanned, int[] adjacentScanned, int adjacentConnectionIndex) {
        var centerColor = scanned[4];
        var adjacentCenterColor = adjacentScanned[4];
        var connectionIndex = -1;
        var connections = Cube.getConnections(centerColor);
        for (var iConn = 0; iConn < connections.length; iConn++) {
            if (connections[iConn].side() == adjacentCenterColor) {
                connectionIndex = iConn;
            }
        }

        if (connectionIndex == -1) {
            throw new IllegalArgumentException("no connection");
        }
//        var expectedCenterColor = Cube.getConnections(centerColor)[adjacentConnectionIndex].side();
//        var distance = Cube.getDistanceAround(centerColor, adjacentCenterColor, expectedCenterColor);
        System.out.println("index of " + adjacentCenterColor + " on " + centerColor + " is " + connectionIndex);
        this.result[centerColor] = Side.from3x3(scanned).rotated(connectionIndex + 2);
    }

    private static int closestFaceColor(double[] color) {
        var hsv = bgrToHsv(color);
        var hue = hsv[0] / 2;
        var saturation = hsv[1];

        if (saturation < 0.3) {
            return FaceColor.WHITE;
        }

        if (hue < 7 || hue > 165) {
            return FaceColor.RED;
        }

        if (hue < 18) {
            return FaceColor.ORANGE;
        }

        if (hue < 36) {
            return FaceColor.YELLOW;
        }

        if (hue < 80) {
            return FaceColor.GREEN;
        }

        if (hue < 125) {
            return FaceColor.BLUE;
        }

        return COLOR_NOT_FOUND;
    }

    private static double[] bgrToHsv(double[] color) {
        var dst = new Mat();
        var src = new Mat(1, 1, CvType.CV_32FC3);
        src.put(0, 0, color);
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2HSV);
        return dst.get(0, 0);
    }
}
