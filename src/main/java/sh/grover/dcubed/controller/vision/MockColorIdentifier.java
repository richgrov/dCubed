package sh.grover.dcubed.controller.vision;

import org.opencv.core.Mat;
import sh.grover.dcubed.model.FaceColor;

public class MockColorIdentifier implements IColorIdentifier {

    private int scan = 0;

    @Override
    public ScannedSide[] estimateColors(Mat image) {
        if (this.scan++ % 2 == 0) {
            return new ScannedSide[] {
                    new ScannedSide(FaceColor.WHITE, new int[]{
                            FaceColor.WHITE, FaceColor.RED, FaceColor.ORANGE, FaceColor.WHITE, FaceColor.GREEN, FaceColor.GREEN, FaceColor.RED, FaceColor.WHITE
                    }),
                    new ScannedSide(FaceColor.ORANGE, new int[]{
                            FaceColor.GREEN, FaceColor.GREEN, FaceColor.YELLOW, FaceColor.GREEN, FaceColor.YELLOW, FaceColor.RED, FaceColor.WHITE, FaceColor.GREEN
                    }),
                    new ScannedSide(FaceColor.GREEN, new int[]{
                            FaceColor.ORANGE, FaceColor.YELLOW, FaceColor.RED, FaceColor.RED, FaceColor.BLUE, FaceColor.YELLOW, FaceColor.RED, FaceColor.YELLOW
                    }),
            };
        }

        return new ScannedSide[] {
                new ScannedSide(FaceColor.YELLOW, new int[] {
                        FaceColor.YELLOW, FaceColor.BLUE, FaceColor.BLUE, FaceColor.YELLOW, FaceColor.YELLOW, FaceColor.ORANGE, FaceColor.GREEN, FaceColor.BLUE
                }),
                new ScannedSide(FaceColor.BLUE, new int[] {
                        FaceColor.ORANGE, FaceColor.ORANGE, FaceColor.BLUE, FaceColor.BLUE, FaceColor.GREEN, FaceColor.ORANGE, FaceColor.ORANGE, FaceColor.WHITE
                }),
                new ScannedSide(FaceColor.RED, new int[] {
                        FaceColor.RED, FaceColor.WHITE, FaceColor.WHITE, FaceColor.BLUE, FaceColor.BLUE, FaceColor.ORANGE, FaceColor.WHITE, FaceColor.RED
                }),
        };
    }
}