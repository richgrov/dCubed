package sh.grover.dcubed.controller.vision.segment;

import org.opencv.core.Mat;
import sh.grover.dcubed.model.vision.segment.CubeSegmentation;

public interface ICubeSegmenter {

    CubeSegmentation segment(Mat image) throws Exception;
}
