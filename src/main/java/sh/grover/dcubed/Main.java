package sh.grover.dcubed;

import nu.pattern.OpenCV;
import sh.grover.dcubed.controller.SolverSessions;
import sh.grover.dcubed.controller.vision.PhotoColorIdentifier;
import sh.grover.dcubed.model.vision.StepDebugLevel;
import sh.grover.dcubed.view.WebServer;

public class Main {
    public static void main(String[] args) {
        OpenCV.loadLocally();
        var solverSessions = new SolverSessions();
        var colorIdentifier = new PhotoColorIdentifier(StepDebugLevel.ALL);
        new WebServer(solverSessions, colorIdentifier);
    }
}