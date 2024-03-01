package sh.grover.dcubed.controller;

import sh.grover.dcubed.controller.solve.*;
import sh.grover.dcubed.model.*;

import java.util.ArrayList;
import java.util.List;
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

    public List<Move> solve(UUID sessionId) {
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

        var moves = new ArrayList<Move>(64);
        new WhiteCrossStep(cube, moves).solve();
        new WhiteCornersStep(cube, moves).solve();
        new SecondLayerStep(cube, moves).solve();
        new YellowCrossStep(cube, moves).solve();
        new YellowEdgesStep(cube, moves).solve();
        new PositionYellowCornersStep(cube, moves).solve();
        new OrientYellowCornersStep(cube, moves).solve();
        return moves;
    }
}