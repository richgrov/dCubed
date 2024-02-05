package sh.grover.dcubed.controller;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import sh.grover.dcubed.view.IView;
import sh.grover.dcubed.view.WebcamCapture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class App implements IController {

    private final IView view;
    private final WebcamCapture capture = new WebcamCapture(1);
    private final MatOfByte imageEncodeTarget = new MatOfByte();
    private final Mat matTarget = new Mat();
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final CubeSegmenter cubeSegmenter = new CubeSegmenter();

    public App(IView view) {
        this.view = view;
    }

    public void run() {
        var mat = new Mat();
        while (this.running.get()) {
            if (!this.capture.capture(mat)) {
                break;
            }

            this.cubeSegmenter.segmentFaces(mat, this.matTarget);

            try {
                this.view.displayImage(toBufferedImage(this.matTarget));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onViewClose() {
        this.running.set(false);
    }

    public void dispose() {
        this.view.close();
        this.capture.dispose();
    }

    private BufferedImage toBufferedImage(Mat image) throws IOException {
        if (!Imgcodecs.imencode(".jpg", image, this.imageEncodeTarget)) {
            throw new IllegalArgumentException("couldn't encode image as jpg");
        }

        return ImageIO.read(new ByteArrayInputStream(this.imageEncodeTarget.toArray()));
    }
}
