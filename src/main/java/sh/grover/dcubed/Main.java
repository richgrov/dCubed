package sh.grover.dcubed;

import nu.pattern.OpenCV;
import sh.grover.dcubed.controller.SolverSessions;
import sh.grover.dcubed.controller.vision.MockColorIdentifier;
import sh.grover.dcubed.router.WebServer;

public class Main {
    public static void main(String[] args) {
        OpenCV.loadLocally();
        var colorIdentifier = new MockColorIdentifier();
        var solverSessions = new SolverSessions(colorIdentifier);
        new WebServer(solverSessions);
    }
}