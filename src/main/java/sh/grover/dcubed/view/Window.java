package sh.grover.dcubed.view;

import sh.grover.dcubed.controller.App;
import sh.grover.dcubed.controller.IController;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class Window extends JFrame implements IView {

    private final JLabel image = new JLabel();
    private IController controller;

    public Window() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        this.getContentPane().add(this.image);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Window.this.controller.onViewClose();
            }
        });
    }

    @Override
    public void setController(App app) {
        this.controller = app;
    }

    @Override
    public void displayImage(BufferedImage image) {
        ensureEdt(() -> this.updateUiImage(image));
    }

    @Override
    public void close() {
        ensureEdt(() -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }

    private void updateUiImage(BufferedImage image) {
        this.image.setIcon(new ImageIcon(image));
        this.pack();
        this.revalidate();
    }

    private static void ensureEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
