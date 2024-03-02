package sh.grover.dcubed.controller;

import sh.grover.dcubed.controller.solve.*;
import sh.grover.dcubed.model.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SolverSessions {

    private final ConcurrentHashMap<UUID, SolveSession> sessions = new ConcurrentHashMap<>();

    public ScanResult newSession(Side[] sides) {
        var session = new SolveSession();
        session.mergeSides(sides);

        var sessionId = UUID.randomUUID();
        this.sessions.put(sessionId, session);
        return new ScanResult(sessionId, session.sides());
    }

    public ScanResult addPhoto(UUID sessionId, Side[] sides) throws IllegalArgumentException {
        var session = this.sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("session does not exist");
        }

        session.mergeSides(sides);
        return new ScanResult(sessionId, session.sides());
    }

    public SolveInstructions solve(UUID sessionId) {
        var session = this.sessions.get(sessionId);
        var sides = session.sides();
        var cube = new Cube(
                sides[FaceColor.WHITE],
                sides[FaceColor.RED],
                sides[FaceColor.ORANGE],
                sides[FaceColor.YELLOW],
                sides[FaceColor.GREEN],
                sides[FaceColor.BLUE]
        );

        var instructions = new SolveInstructions();
        instructions.runStep(new WhiteCrossStep(cube));
        instructions.runStep(new WhiteCornersStep(cube));
        instructions.runStep(new SecondLayerStep(cube));
        instructions.runStep(new YellowCrossStep(cube));
        instructions.runStep(new YellowEdgesStep(cube));
        instructions.runStep(new PositionYellowCornersStep(cube));
        instructions.runStep(new OrientYellowCornersStep(cube));
        return instructions;
    }
}