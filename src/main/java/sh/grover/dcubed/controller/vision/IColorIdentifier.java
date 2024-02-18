package sh.grover.dcubed.controller.vision;

import org.opencv.core.Mat;

public interface IColorIdentifier {

    ScannedSide[] estimateColors(Mat image);
}
