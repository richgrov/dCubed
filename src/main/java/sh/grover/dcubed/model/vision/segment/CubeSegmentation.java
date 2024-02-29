package sh.grover.dcubed.model.vision.segment;

import org.opencv.core.Point;
import sh.grover.dcubed.util.MathUtil;

public record CubeSegmentation(
    Point top,
    Point topLeft,
    Point bottomLeft,
    Point bottom,
    Point bottomRight,
    Point topRight
) {
    public double highestX() {
        return Math.max(this.topLeft.x, this.bottomLeft.x);
    }

    public double lowestX() {
        return Math.min(this.topLeft.x, this.bottomLeft.x);
    }

    public double highestY() {
        return MathUtil.max(this.bottom.y, this.bottomLeft.y, this.bottomRight.y);
    }

    public double lowestY() {
        return MathUtil.min(this.top.y, this.topLeft.y, this.topRight.y);
    }

    public CubeSegmentation subtract(double x, double y) {
        var subtract = new Point(x, y);
        return new CubeSegmentation(
                MathUtil.subtract(this.top, subtract),
                MathUtil.subtract(this.topLeft, subtract),
                MathUtil.subtract(this.bottomLeft, subtract),
                MathUtil.subtract(this.bottom, subtract),
                MathUtil.subtract(this.bottomRight, subtract),
                MathUtil.subtract(this.topRight, subtract)
        );
    }

//    public CubeSegmentation multiply(double width, double height) {
//        var multiply = new Point(width, height);
//        return new CubeSegmentation(
//                PointUtil.multiply(this.top, multiply),
//                PointUtil.multiply(this.topLeft, multiply),
//                PointUtil.multiply(this.bottomLeft, multiply),
//                PointUtil.multiply(this.bottom, multiply),
//                PointUtil.multiply(this.bottomRight, multiply),
//                PointUtil.multiply(this.topRight, multiply)
//        );
//    }
}
