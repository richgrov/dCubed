package sh.grover.dcubed.controller.vision;

import org.opencv.core.Mat;
import sh.grover.dcubed.model.FaceColor;
import sh.grover.dcubed.model.Side;

public class MockColorIdentifier implements IColorIdentifier {

    @Override
    public Side[] estimateColors(Mat image) {
        return new Side[] {
                new Side(FaceColor.WHITE, FaceColor.RED, FaceColor.ORANGE, FaceColor.WHITE, FaceColor.GREEN, FaceColor.GREEN, FaceColor.RED, FaceColor.WHITE),
                new Side(FaceColor.RED, FaceColor.WHITE, FaceColor.WHITE, FaceColor.BLUE, FaceColor.BLUE, FaceColor.ORANGE, FaceColor.WHITE, FaceColor.RED),
                new Side(FaceColor.GREEN, FaceColor.GREEN, FaceColor.YELLOW, FaceColor.GREEN, FaceColor.YELLOW, FaceColor.RED, FaceColor.WHITE, FaceColor.GREEN),
                new Side(FaceColor.YELLOW, FaceColor.BLUE, FaceColor.BLUE, FaceColor.YELLOW, FaceColor.YELLOW, FaceColor.ORANGE, FaceColor.GREEN, FaceColor.BLUE),
                new Side(FaceColor.ORANGE, FaceColor.YELLOW, FaceColor.RED, FaceColor.RED, FaceColor.BLUE, FaceColor.YELLOW, FaceColor.RED, FaceColor.YELLOW),
                new Side(FaceColor.ORANGE, FaceColor.ORANGE, FaceColor.BLUE, FaceColor.BLUE, FaceColor.GREEN, FaceColor.ORANGE, FaceColor.ORANGE, FaceColor.WHITE),
        };
    }
}