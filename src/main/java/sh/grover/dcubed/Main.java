package sh.grover.dcubed;

import nu.pattern.OpenCV;
import sh.grover.dcubed.controller.App;
import sh.grover.dcubed.view.Window;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        OpenCV.loadLocally();
        SwingUtilities.invokeLater(Main::start);
    }

    private static void start() {
        var view = new Window();

        new Thread(() -> {
            var controller = new App(view);
            view.setController(controller);
            try {
                controller.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            controller.dispose();
        }).start();
    }
}