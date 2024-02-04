package sh.grover.dcubed.view;

import sh.grover.dcubed.controller.App;

import java.awt.image.BufferedImage;

public interface IView {

    void setController(App app);
    void displayImage(BufferedImage image);
    void close();
}
