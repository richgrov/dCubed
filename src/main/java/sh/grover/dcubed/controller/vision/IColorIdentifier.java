package sh.grover.dcubed.controller.vision;

import org.opencv.core.Mat;
import sh.grover.dcubed.model.vision.ColorScanException;

public interface IColorIdentifier {

    ScannedSide[] estimateColors(Mat image) throws ColorScanException;
}
