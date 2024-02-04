package sh.grover.dcubed.view;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class WebcamCapture {

    private final VideoCapture capture;

    public WebcamCapture(int videoIndex) {
        this.capture = new VideoCapture();
        if (!capture.open(videoIndex)) {
            throw new IllegalArgumentException("video index " + videoIndex + " couldn't be opened");
        }
    }

    public boolean capture(Mat mat) {
        return capture.read(mat);
    }

    public void dispose() {
        this.capture.release();
    }
}
